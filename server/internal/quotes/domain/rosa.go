package domain

import (
	"fmt"
	"strings"
)

type CatalogSKU struct {
	ID             string
	Code           string
	Name           string
	CategoryID     string
	UnitPriceRD    int
	AvailableStock int
}

type RosaLayoutPlan struct {
	Tables            int     `json:"tables"`
	Chairs            int     `json:"chairs"`
	Arches            int     `json:"arches"`
	SweetTables       int     `json:"sweetTables"`
	Centerpieces      int     `json:"centerpieces"`
	LightingStrands   int     `json:"lightingStrands"`
	CanvasWidthMeters float64 `json:"canvasWidthMeters"`
	CanvasDepthMeters float64 `json:"canvasDepthMeters"`
}

type RosaProposalRequest struct {
	EventType          string
	Guests             int
	BudgetRD           int
	Prompt             string
	ExcludedCategories []string
}

type RosaMoodboardProposal struct {
	Step               int            `json:"step"`
	EventType          string         `json:"eventType"`
	StyleTitle         string         `json:"styleTitle"`
	ColorPalette       []string       `json:"colorPalette"`
	Guests             int            `json:"guests"`
	BudgetRD           int            `json:"budgetRD"`
	WithinBudget       bool           `json:"withinBudget"`
	ExcludedCategories []string       `json:"excludedCategories,omitempty"`
	SuggestedLines     []QuoteLine    `json:"suggestedLines"`
	Layout             RosaLayoutPlan `json:"layout"`
	EstimatedTotalRD   int            `json:"estimatedTotalRD"`
	DepositRequiredRD  int            `json:"depositRequiredRD"`
	AssistantReply     string         `json:"assistantReply"`
}

type RosaAdminSnapshot struct {
	PendingQuotesCount int
	SignedQuotesCount  int
	LowStockItems      []string
}

type RosaChatResponse struct {
	Reply      string                 `json:"reply"`
	Intent     string                 `json:"intent"`
	ActionCode string                 `json:"actionCode,omitempty"`
	Proposal   *RosaMoodboardProposal `json:"proposal,omitempty"`
}

func DefaultCatalogSKUs() []CatalogSKU {
	return []CatalogSKU{
		{ID: "silla-tiffany", Code: "RF-001", Name: "Silla Tiffany dorada", CategoryID: "mobiliario", UnitPriceRD: 4, AvailableStock: 120},
		{ID: "mesa-plegable", Code: "RF-002", Name: "Mesa rectangular plegable", CategoryID: "mobiliario", UnitPriceRD: 20, AvailableStock: 18},
		{ID: "mesa-redonda", Code: "RF-003", Name: "Mesa redonda 8 personas", CategoryID: "mobiliario", UnitPriceRD: 25, AvailableStock: 12},
		{ID: "centro-floral", Code: "RF-004", Name: "Centro de mesa floral", CategoryID: "floral", UnitPriceRD: 35, AvailableStock: 15},
		{ID: "arco-rosa", Code: "RF-005", Name: "Arco orgánico rosa", CategoryID: "arcos", UnitPriceRD: 95, AvailableStock: 3},
		{ID: "mesa-helados", Code: "RF-006", Name: "Mesa de dulces / helados", CategoryID: "dulces", UnitPriceRD: 150, AvailableStock: 2},
		{ID: "luces-bistro", Code: "RF-007", Name: "Guirnalda luces bistró cálidas", CategoryID: "iluminacion", UnitPriceRD: 45, AvailableStock: 20},
		{ID: "manteleria-lino", Code: "RF-008", Name: "Mantelería lino imperial", CategoryID: "manteleria", UnitPriceRD: 18, AvailableStock: 60},
	}
}

func BuildRosaProposal(eventType string, guests int, budgetRD int) RosaMoodboardProposal {
	req := RosaProposalRequest{
		EventType: eventType,
		Guests:    guests,
		BudgetRD:  budgetRD,
	}
	return BuildRosaProposalFromCatalog(req, nil)
}

func BuildRosaProposalFromCatalog(req RosaProposalRequest, skus []CatalogSKU) RosaMoodboardProposal {
	catalog := skus
	if len(catalog) == 0 {
		catalog = DefaultCatalogSKUs()
	}
	guests := req.Guests
	if guests <= 0 {
		guests = 80
	}
	excluded := buildExcludedSet(req.ExcludedCategories, req.Prompt)
	lines, layout := composeProposalLines(guests, req.BudgetRD, req.Prompt, catalog, excluded)
	if req.BudgetRD > 0 {
		lines, layout = trimOptionalLinesToBudget(lines, layout, req.BudgetRD)
	}
	total, deposit, _, _ := CalculateQuoteTotals(lines, 0, 0)
	withinBudget := true
	if req.BudgetRD > 0 {
		withinBudget = total <= req.BudgetRD
	}
	normalizedType := strings.TrimSpace(req.EventType)
	if normalizedType == "" {
		normalizedType = "Boda Real Rosa & Dorado"
	}
	styleTitle, palette := resolveStyleAndPalette(normalizedType)
	excludedSlice := sortedKeysFromSet(excluded)
	reply := buildProposalReply(normalizedType, guests, total, withinBudget, req.Prompt)
	return RosaMoodboardProposal{
		Step:               7,
		EventType:          normalizedType,
		StyleTitle:         styleTitle,
		ColorPalette:       palette,
		Guests:             guests,
		BudgetRD:           req.BudgetRD,
		WithinBudget:       withinBudget,
		ExcludedCategories: excludedSlice,
		SuggestedLines:     lines,
		Layout:             layout,
		EstimatedTotalRD:   total,
		DepositRequiredRD:  deposit,
		AssistantReply:     reply,
	}
}

func BuildRosaChatReply(
	message string,
	isAdmin bool,
	req RosaProposalRequest,
	skus []CatalogSKU,
	adminSnap RosaAdminSnapshot,
) RosaChatResponse {
	trimmed := strings.TrimSpace(message)
	lower := strings.ToLower(trimmed)
	if isAdmin {
		if adminReply, matched := evaluateAdminCommand(lower, adminSnap); matched {
			return adminReply
		}
	}
	req.Prompt = trimmed
	proposal := BuildRosaProposalFromCatalog(req, skus)
	return RosaChatResponse{
		Reply:      proposal.AssistantReply,
		Intent:     "design_proposal",
		ActionCode: "APPLY_ROSA_PROPOSAL",
		Proposal:   &proposal,
	}
}

func evaluateAdminCommand(lower string, snap RosaAdminSnapshot) (RosaChatResponse, bool) {
	if strings.Contains(lower, "pendiente") {
		reply := fmt.Sprintf("Tienes %d cotizaciones pendientes de revisión y %d contratos firmados activos.", snap.PendingQuotesCount, snap.SignedQuotesCount)
		return RosaChatResponse{
			Reply:      reply,
			Intent:     "admin_pending_quotes",
			ActionCode: "OPEN_PENDING_QUOTES",
		}, true
	}
	if strings.Contains(lower, "stock") {
		joined := strings.Join(snap.LowStockItems, ", ")
		if joined == "" {
			joined = "sin alertas críticas"
		}
		reply := fmt.Sprintf("Estado de inventario verificado. Artículos en umbral bajo: %s.", joined)
		return RosaChatResponse{
			Reply:      reply,
			Intent:     "admin_inventory_alert",
			ActionCode: "OPEN_INVENTORY_ALERTS",
		}, true
	}
	if strings.Contains(lower, "resumen") {
		reply := fmt.Sprintf("Resumen operativo: %d solicitudes nuevas, %d contratos firmados y %d alertas de stock.", snap.PendingQuotesCount, snap.SignedQuotesCount, len(snap.LowStockItems))
		return RosaChatResponse{
			Reply:      reply,
			Intent:     "admin_operational_summary",
			ActionCode: "OPEN_ADMIN_DASHBOARD",
		}, true
	}
	return RosaChatResponse{}, false
}

func composeProposalLines(
	guests int,
	budgetRD int,
	prompt string,
	catalog []CatalogSKU,
	excluded map[string]bool,
) ([]QuoteLine, RosaLayoutPlan) {
	tables := (guests + 7) / 8
	lowerPrompt := strings.ToLower(prompt)
	lines := make([]QuoteLine, 0, 6)
	layout := RosaLayoutPlan{
		CanvasWidthMeters: 12.0,
		CanvasDepthMeters: 10.0,
	}
	if !excluded["mobiliario"] {
		chairSKU := findSKU(catalog, "silla-tiffany", "RF-001", "Silla Tiffany dorada", 4)
		lines = append(lines, QuoteLine{ArticleCode: chairSKU.Code, Name: chairSKU.Name, Quantity: guests, UnitPriceRD: chairSKU.UnitPriceRD})
		layout.Chairs = guests
		tableSKU := selectTableSKU(catalog, lowerPrompt)
		lines = append(lines, QuoteLine{ArticleCode: tableSKU.Code, Name: tableSKU.Name, Quantity: tables, UnitPriceRD: tableSKU.UnitPriceRD})
		layout.Tables = tables
	}
	if !excluded["floral"] {
		floralSKU := findSKU(catalog, "centro-floral", "RF-004", "Centro de mesa floral", 35)
		lines = append(lines, QuoteLine{ArticleCode: floralSKU.Code, Name: floralSKU.Name, Quantity: tables, UnitPriceRD: floralSKU.UnitPriceRD})
		layout.Centerpieces = tables
	}
	if !excluded["arcos"] {
		archSKU := findSKU(catalog, "arco-rosa", "RF-005", "Arco orgánico rosa", 95)
		lines = append(lines, QuoteLine{ArticleCode: archSKU.Code, Name: archSKU.Name, Quantity: 1, UnitPriceRD: archSKU.UnitPriceRD})
		layout.Arches = 1
	}
	lines, layout = appendOptionalPromptAndBudgetSKUs(lines, layout, tables, budgetRD, lowerPrompt, catalog, excluded)
	return lines, layout
}

func appendOptionalPromptAndBudgetSKUs(
	lines []QuoteLine,
	layout RosaLayoutPlan,
	tables int,
	budgetRD int,
	lowerPrompt string,
	catalog []CatalogSKU,
	excluded map[string]bool,
) ([]QuoteLine, RosaLayoutPlan) {
	wantsSweet := budgetRD >= 1200
	if !wantsSweet {
		wantsSweet = strings.Contains(lowerPrompt, "dulce")
	}
	if !wantsSweet {
		wantsSweet = strings.Contains(lowerPrompt, "helado")
	}
	if wantsSweet {
		if !excluded["dulces"] {
			sweetSKU := findSKU(catalog, "mesa-helados", "RF-006", "Mesa de dulces / helados", 150)
			lines = append(lines, QuoteLine{ArticleCode: sweetSKU.Code, Name: sweetSKU.Name, Quantity: 1, UnitPriceRD: sweetSKU.UnitPriceRD})
			layout.SweetTables = 1
		}
	}
	if strings.Contains(lowerPrompt, "luce") {
		if !excluded["iluminacion"] {
			lightSKU := findSKU(catalog, "luces-bistro", "RF-007", "Guirnalda luces bistró cálidas", 45)
			lines = append(lines, QuoteLine{ArticleCode: lightSKU.Code, Name: lightSKU.Name, Quantity: 2, UnitPriceRD: lightSKU.UnitPriceRD})
			layout.LightingStrands = 2
		}
	}
	if strings.Contains(lowerPrompt, "mantel") {
		if !excluded["manteleria"] {
			linenSKU := findSKU(catalog, "manteleria-lino", "RF-008", "Mantelería lino imperial", 18)
			lines = append(lines, QuoteLine{ArticleCode: linenSKU.Code, Name: linenSKU.Name, Quantity: tables, UnitPriceRD: linenSKU.UnitPriceRD})
		}
	}
	if len(lines) == 0 {
		fallbackSKU := findSKU(catalog, "arco-rosa", "RF-005", "Arco orgánico rosa", 95)
		lines = append(lines, QuoteLine{ArticleCode: fallbackSKU.Code, Name: fallbackSKU.Name, Quantity: 1, UnitPriceRD: fallbackSKU.UnitPriceRD})
		layout.Arches = 1
	}
	return lines, layout
}

func trimOptionalLinesToBudget(lines []QuoteLine, layout RosaLayoutPlan, budgetRD int) ([]QuoteLine, RosaLayoutPlan) {
	total, _, _, _ := CalculateQuoteTotals(lines, 0, 0)
	if total <= budgetRD {
		return lines, layout
	}
	if len(lines) <= 2 {
		return lines, layout
	}
	trimmed := lines[:len(lines)-1]
	removed := lines[len(lines)-1]
	if removed.ArticleCode == "RF-006" {
		layout.SweetTables = 0
	}
	if removed.ArticleCode == "RF-007" {
		layout.LightingStrands = 0
	}
	return trimmed, layout
}

func selectTableSKU(catalog []CatalogSKU, lowerPrompt string) CatalogSKU {
	if strings.Contains(lowerPrompt, "plegable") {
		return findSKU(catalog, "mesa-plegable", "RF-002", "Mesa rectangular plegable", 20)
	}
	return findSKU(catalog, "mesa-redonda", "RF-003", "Mesa redonda 8 personas", 25)
}

func findSKU(catalog []CatalogSKU, id, fallbackCode, fallbackName string, fallbackPrice int) CatalogSKU {
	for _, sku := range catalog {
		if sku.ID == id {
			code := sku.Code
			if code == "" {
				code = fallbackCode
			}
			return CatalogSKU{
				ID:          sku.ID,
				Code:        code,
				Name:        sku.Name,
				CategoryID:  sku.CategoryID,
				UnitPriceRD: sku.UnitPriceRD,
			}
		}
	}
	return CatalogSKU{
		ID:          id,
		Code:        fallbackCode,
		Name:        fallbackName,
		UnitPriceRD: fallbackPrice,
	}
}

func buildExcludedSet(raw []string, prompt string) map[string]bool {
	excluded := make(map[string]bool)
	for _, cat := range raw {
		trimmed := strings.TrimSpace(cat)
		lower := strings.ToLower(trimmed)
		if lower != "" {
			excluded[lower] = true
		}
	}
	lowerPrompt := strings.ToLower(prompt)
	if strings.Contains(lowerPrompt, "sin flor") {
		excluded["floral"] = true
	}
	if strings.Contains(lowerPrompt, "sin dulce") {
		excluded["dulces"] = true
	}
	if strings.Contains(lowerPrompt, "sin arco") {
		excluded["arcos"] = true
	}
	return excluded
}

func sortedKeysFromSet(set map[string]bool) []string {
	if len(set) == 0 {
		return nil
	}
	out := make([]string, 0, len(set))
	for _, candidate := range []string{"mobiliario", "floral", "arcos", "dulces", "iluminacion", "manteleria"} {
		if set[candidate] {
			out = append(out, candidate)
		}
	}
	return out
}

func resolveStyleAndPalette(eventType string) (string, []string) {
	lower := strings.ToLower(eventType)
	if strings.Contains(lower, "quince") {
		return "Cristal Violeta & Rosa Imperial", []string{"#EA4EA9", "#945AE1", "#FDE6F2", "#1A1A2E"}
	}
	if strings.Contains(lower, "cumple") {
		return "Fiesta Tropical & Sol Cálido", []string{"#FFD45C", "#B9D84A", "#2FB8C8", "#EA4EA9"}
	}
	return "Rosa Empolvado, Dorado Cálido & Cristal", []string{"#EA4EA9", "#FFD45C", "#FDE6F2", "#1A1A2E"}
}

func buildProposalReply(eventType string, guests int, totalRD int, withinBudget bool, prompt string) string {
	trimmedPrompt := strings.TrimSpace(prompt)
	budgetNote := "dentro de tu presupuesto"
	if !withinBudget {
		budgetNote = "ajustando las cantidades esenciales para tus invitados"
	}
	if trimmedPrompt != "" {
		return fmt.Sprintf("He diseñado tu propuesta para %s (%d invitados, RD$ %d) aplicando tu indicación %q %s.", eventType, guests, totalRD, trimmedPrompt, budgetNote)
	}
	return fmt.Sprintf("He preparado tu moodboard de %s con cantidades exactas para %d invitados (RD$ %d) %s.", eventType, guests, totalRD, budgetNote)
}
