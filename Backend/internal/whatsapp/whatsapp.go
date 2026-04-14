package whatsapp

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"time"
)

// Config holds WhatsApp Business API configuration.
type Config struct {
	PhoneNumberID string // The phone number ID from Meta developer portal
	AccessToken   string // Permanent access token from Meta
	FromName      string // Business name shown to recipients
}

// Message represents a WhatsApp message to send.
type Message struct {
	To        string // Recipient phone number (with country code, e.g. +18095551234)
	Body      string // Message text
	Template  string // Optional template name (for templated messages)
	MediaURL  string // Optional media attachment URL
}

// Client handles sending WhatsApp messages via the Meta WhatsApp Cloud API.
type Client struct {
	config    Config
	httpClient *http.Client
	enabled   bool
}

// NewClient creates a new WhatsApp API client.
func NewClient(cfg Config) *Client {
	if cfg.AccessToken == "" || cfg.PhoneNumberID == "" {
		log.Println("[WhatsApp] Not configured - using mock mode")
		return &Client{enabled: false}
	}

	return &Client{
		config: cfg,
		httpClient: &http.Client{
			Timeout: 10 * time.Second,
		},
		enabled: true,
	}
}

// SendTextMessage sends a simple text WhatsApp message.
func (c *Client) SendTextMessage(ctx context.Context, msg Message) error {
	if !c.enabled {
		log.Printf("[WhatsApp MOCK] Sending to %s: %s", msg.To, msg.Body)
		return nil
	}

	payload := map[string]interface{}{
		"messaging_product": "whatsapp",
		"to":               msg.To,
		"type":             "text",
		"text": map[string]string{
			"preview_url": "false",
			"body":        msg.Body,
		},
	}

	return c.send(ctx, payload)
}

// SendTemplateMessage sends a templated WhatsApp message.
func (c *Client) SendTemplateMessage(ctx context.Context, msg Message) error {
	if !c.enabled {
		log.Printf("[WhatsApp MOCK] Sending template %s to %s", msg.Template, msg.To)
		return nil
	}

	payload := map[string]interface{}{
		"messaging_product": "whatsapp",
		"to":               msg.To,
		"type":             "template",
		"template": map[string]interface{}{
			"name": msg.Template,
			"language": map[string]string{
				"code": "es_ES", // Spanish (Dominican Republic)
			},
		},
	}

	return c.send(ctx, payload)
}

// SendEventStatusNotification sends a formatted event status update.
func (c *Client) SendEventStatusNotification(ctx context.Context, to, eventName, status string) error {
	var body string
	switch status {
	case "confirmed":
		body = fmt.Sprintf(
			"🎉 *¡%s ha sido confirmado!*\n\n"+
				"Tu evento está confirmado y listo. El equipo de RosaFiesta se pondrá en contacto pronto.\n\n"+
				"¿Preguntas? Responde este mensaje o escríbenos.", eventName)
	case "paid":
		body = fmt.Sprintf(
			"✅ *¡Pago recibido!*\n\n"+
				"Gracias por confiar en RosaFiesta. Tu evento %s está completamente pagado.\n\n"+
				"Nos vemos pronto 🎈", eventName)
	case "completed":
		body = fmt.Sprintf(
			"🎊 *¡%s ha finalizado!*\n\n"+
				"Fue un placer servirte. "+
				"¿Cómo te pareció nuestro servicio? Nos encantaría conocer tu opinión.", eventName)
	case "cancelled":
		body = fmt.Sprintf(
			"⚠️ *Evento cancelado*\n\n"+
				"Lamentamos informarte que %s ha sido cancelado. "+
				"Si tienes alguna pregunta, estamos aquí para ayudarte.", eventName)
	default:
		body = fmt.Sprintf(
			"📋 *Actualización de %s*\n\n"+
				"Estado: %s\n\n"+
				"¿Necesitas algo? Responde este mensaje.", eventName, status)
	}

	return c.SendTextMessage(ctx, Message{To: to, Body: body})
}

// SendQuoteApprovedNotification notifies client that their quote was approved.
func (c *Client) SendQuoteApprovedNotification(ctx context.Context, to, eventName string, totalAmount float64) error {
	body := fmt.Sprintf(
		"💰 *¡Cotización aprobada!*\n\n"+
			"Hola! La cotización para *%s* ha sido aprobada.\n"+
			"Monto total: *RD$ %.2f*\n\n"+
			"Te contactaremos pronto para coordinar el pago. "+
			"¿Tienes alguna pregunta? Estamos aquí 😊", eventName, totalAmount)

	return c.SendTextMessage(ctx, Message{To: to, Body: body})
}

// SendPaymentReminder sends a friendly payment reminder.
func (c *Client) SendPaymentReminder(ctx context.Context, to, eventName string, amountDue float64) error {
	body := fmt.Sprintf(
		"⏰ *Recordatorio de pago*\n\n"+
			"Hola! Solo un recordatoriofriendly reminder about the pending payment for *%s*.\n"+
			"Monto pendiente: *RD$ %.2f*\n\n"+
			"¿Ya realizaste el pago? Avísanos si necesitas ayuda.", eventName, amountDue)

	return c.SendTextMessage(ctx, Message{To: to, Body: body})
}

func (c *Client) send(ctx context.Context, payload map[string]interface{}) error {
	url := fmt.Sprintf(
		"https://graph.facebook.com/v21.0/%s/messages",
		c.config.PhoneNumberID,
	)

	bodyBytes, err := json.Marshal(payload)
	if err != nil {
		return fmt.Errorf("marshal payload: %w", err)
	}

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, bytes.NewReader(bodyBytes))
	if err != nil {
		return fmt.Errorf("create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+c.config.AccessToken)
	req.Header.Set("Content-Type", "application/json")

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 400 {
		var errResp map[string]interface{}
		json.NewDecoder(resp.Body).Decode(&errResp)
		log.Printf("[WhatsApp] API error: %v", errResp)
		return fmt.Errorf("WhatsApp API error: status %d", resp.StatusCode)
	}

	var result map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return fmt.Errorf("decode response: %w", err)
	}

	log.Printf("[WhatsApp] Message sent successfully: %v", result["messages"])
	return nil
}
