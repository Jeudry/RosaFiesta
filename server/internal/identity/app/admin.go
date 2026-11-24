package app

import (
	"context"
	"log/slog"
	"strings"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
)

type UpdateClientCommand struct {
	FullName *string
	Email    *string
	Phone    *string
	Notes    *string
}

// InviteStaffCommand contiene los datos para invitar a un colaborador al equipo operativo.
type InviteStaffCommand struct {
	FullName        string
	Email           string
	Phone           string
	RoleLabel       string
	Role            string
	Permissions     []string
	Password        string
	InitialPassword string
}

type UpdateStaffCommand struct {
	FullName    *string
	RoleLabel   *string
	Role        *string
	Permissions *[]string
	Active      *bool
}

func (s *Service) Clients(ctx context.Context) []domain.AdminClient {
	s.syncAdminRecordsFromStore(ctx)
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.AdminClient, len(s.clients))
	copy(out, s.clients)
	return out
}

func (s *Service) UpdateClient(ctx context.Context, clientID string, cmd UpdateClientCommand) (domain.AdminClient, error) {
	s.syncAdminRecordsFromStore(ctx)
	trimmedID := strings.TrimSpace(clientID)
	s.mu.Lock()
	defer s.mu.Unlock()

	idx := -1
	for i, existing := range s.clients {
		if existing.ID == trimmedID {
			idx = i
			break
		}
	}
	if idx < 0 {
		return domain.AdminClient{}, domain.ErrUserNotFound
	}
	now := s.clock.Now()
	updated, err := s.clients[idx].ApplyPatch(cmd.FullName, cmd.Email, cmd.Phone, cmd.Notes, now)
	if err != nil {
		return domain.AdminClient{}, err
	}
	s.clients[idx] = updated
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.UpsertAdminClient(ctx, updated)
		}
	}
	return updated, nil
}

func (s *Service) Staff(ctx context.Context) []domain.StaffRecord {
	s.syncAdminRecordsFromStore(ctx)
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.StaffRecord, len(s.staff))
	copy(out, s.staff)
	return out
}

func (s *Service) InviteStaff(ctx context.Context, cmd InviteStaffCommand) (domain.StaffRecord, error) {
	fullName := strings.TrimSpace(cmd.FullName)
	if fullName == "" {
		return domain.StaffRecord{}, domain.ErrInvalidInput
	}
	normalizedEmail, err := domain.NormalizeEmail(cmd.Email)
	if err != nil {
		return domain.StaffRecord{}, err
	}
	normalizedPerms, err := domain.NormalizeStaffPermissions(cmd.Permissions)
	if err != nil {
		return domain.StaffRecord{}, err
	}
	resolvedRole := resolveInvitedStaffRole(cmd.Role)
	if resolvedRole == "" {
		return domain.StaffRecord{}, domain.ErrInvalidInput
	}
	rawPass := resolveInviteInitialPassword(cmd)
	passHash, err := domain.HashPassword(rawPass)
	if err != nil {
		return domain.StaffRecord{}, domain.ErrInvalidInput
	}
	staffRec, err := s.persistInvitedStaff(ctx, fullName, normalizedEmail, cmd, resolvedRole, normalizedPerms, passHash)
	if err != nil {
		return domain.StaffRecord{}, err
	}
	s.sendStaffInviteEmailIfConfigured(ctx, staffRec, rawPass)
	return staffRec, nil
}

func resolveInviteInitialPassword(cmd InviteStaffCommand) string {
	if initial := strings.TrimSpace(cmd.InitialPassword); initial != "" {
		return initial
	}
	if legacy := strings.TrimSpace(cmd.Password); legacy != "" {
		return legacy
	}
	generatedPass, _ := domain.GenerateRandomHex(12)
	return generatedPass
}

func (s *Service) sendStaffInviteEmailIfConfigured(ctx context.Context, staffRec domain.StaffRecord, tempPassword string) {
	sender := s.opts.EmailSender
	hasSender := sender != nil
	if hasSender {
		hasSender = sender.IsConfigured()
	}
	if !hasSender {
		return
	}
	rawTemplate, err := emailTemplates.ReadFile("templates/staff_invite_email.txt")
	if err != nil {
		return
	}
	body := strings.ReplaceAll(string(rawTemplate), "{{NAME}}", staffRec.FullName)
	body = strings.ReplaceAll(body, "{{ROLE_LABEL}}", staffRec.RoleLabel)
	body = strings.ReplaceAll(body, "{{EMAIL}}", staffRec.Email)
	body = strings.ReplaceAll(body, "{{TEMP_PASSWORD}}", tempPassword)
	if mailErr := sender.SendEmail(ctx, staffRec.Email, "Bienvenido al equipo RosaFiesta", body); mailErr != nil {
		slog.WarnContext(ctx, "staff invite welcome email failed", slog.String("staff_id", staffRec.ID), slog.Any("error", mailErr))
	}
}

func (s *Service) persistInvitedStaff(
	ctx context.Context,
	fullName string,
	normalizedEmail string,
	cmd InviteStaffCommand,
	resolvedRole string,
	normalizedPerms []string,
	passHash string,
) (domain.StaffRecord, error) {
	s.syncUsersFromStore(ctx)
	s.syncAdminRecordsFromStore(ctx)
	s.mu.Lock()
	defer s.mu.Unlock()

	if err := s.ensureUniqueEmailAndUsernameLocked(uuid.Nil, normalizedEmail, ""); err != nil {
		return domain.StaffRecord{}, err
	}
	now := s.clock.Now()
	emailParts := strings.Split(normalizedEmail, "@")
	userID := uuid.New()
	staffUser := domain.User{
		ID:           userID,
		Username:     emailParts[0],
		FullName:     fullName,
		Email:        normalizedEmail,
		Phone:        strings.TrimSpace(cmd.Phone),
		Active:       true,
		Role:         domain.Role(resolvedRole),
		Permissions:  normalizedPerms,
		ActiveView:   domain.ViewAdmin,
		CreatedAt:    now,
		PasswordHash: passHash,
	}
	roleLabel := strings.TrimSpace(cmd.RoleLabel)
	if roleLabel == "" {
		roleLabel = "Team Member"
	}
	randomSuffix, _ := domain.GenerateRandomHex(4)
	staffRec := domain.StaffRecord{
		ID:          "staff-" + randomSuffix,
		UserID:      userID,
		FullName:    fullName,
		Email:       normalizedEmail,
		Phone:       strings.TrimSpace(cmd.Phone),
		RoleLabel:   roleLabel,
		Role:        resolvedRole,
		Permissions: normalizedPerms,
		Active:      true,
		CreatedAt:   now,
	}
	s.users[userID] = staffUser
	s.staff = append(s.staff, staffRec)
	s.persistUserOnlyLocked(ctx, staffUser)
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.UpsertStaffMember(ctx, staffRec)
		}
	}
	return staffRec, nil
}

func (s *Service) UpdateStaff(ctx context.Context, staffID string, cmd UpdateStaffCommand) (domain.StaffRecord, error) {
	s.syncUsersFromStore(ctx)
	s.syncAdminRecordsFromStore(ctx)
	trimmedID := strings.TrimSpace(staffID)

	s.mu.Lock()
	defer s.mu.Unlock()

	idx := -1
	for i, existing := range s.staff {
		if existing.ID == trimmedID {
			idx = i
			break
		}
	}
	if idx < 0 {
		return domain.StaffRecord{}, domain.ErrUserNotFound
	}
	updated, err := s.staff[idx].ApplyPatch(cmd.FullName, cmd.RoleLabel, cmd.Role, cmd.Permissions, cmd.Active)
	if err != nil {
		return domain.StaffRecord{}, err
	}
	if s.wouldLeaveWithoutActiveAdminLocked(s.staff[idx], updated) {
		return domain.StaffRecord{}, domain.ErrCannotDeleteLastAdmin
	}
	s.staff[idx] = updated
	s.syncStaffToLinkedUserLocked(ctx, updated)
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.UpsertStaffMember(ctx, updated)
		}
	}
	return updated, nil
}

func (s *Service) syncStaffToLinkedUserLocked(ctx context.Context, rec domain.StaffRecord) {
	for id, user := range s.users {
		matchesID := rec.UserID != uuid.Nil
		if matchesID {
			matchesID = id == rec.UserID
		}
		matchesEmail := false
		if rec.Email != "" {
			matchesEmail = strings.EqualFold(user.Email, rec.Email)
		}
		if !matchesID {
			if !matchesEmail {
				continue
			}
		}
		user.FullName = rec.FullName
		user.Role = domain.Role(rec.Role)
		user.Active = rec.Active
		permsCopy := make([]string, len(rec.Permissions))
		copy(permsCopy, rec.Permissions)
		user.Permissions = permsCopy
		s.users[id] = user
		s.persistUserOnlyLocked(ctx, user)
		if !rec.Active {
			s.revokeUserAccessLocked(ctx, id)
		}
		return
	}
}

func (s *Service) revokeUserAccessLocked(ctx context.Context, userID uuid.UUID) {
	s.revokeUserRefreshTokensLocked(ctx, userID, s.clock.Now())
	for hash, entry := range s.sessionHashes {
		if entry.UserID == userID {
			delete(s.sessionHashes, hash)
			s.revokedHashes[hash] = struct{}{}
		}
	}
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.DeleteSessionsByUser(ctx, userID)
		}
	}
}

// DeleteStaff desactiva la cuenta vinculada, revoca sesiones y elimina el registro operativo.
func (s *Service) DeleteStaff(ctx context.Context, staffID string) error {
	s.syncUsersFromStore(ctx)
	s.syncAdminRecordsFromStore(ctx)
	trimmedID := strings.TrimSpace(staffID)
	s.mu.Lock()
	defer s.mu.Unlock()
	idx := -1
	for i, existing := range s.staff {
		if existing.ID == trimmedID {
			idx = i
			break
		}
	}
	if idx < 0 {
		return domain.ErrUserNotFound
	}
	target := s.staff[idx]
	if isProtectedOwnerAdmin(target) {
		return domain.ErrCannotDeleteLastAdmin
	}
	if s.isLastActiveAdminLocked(target) {
		return domain.ErrCannotDeleteLastAdmin
	}
	removed := target
	removed.Active = false
	s.syncStaffToLinkedUserLocked(ctx, removed)
	s.staff = append(s.staff[:idx], s.staff[idx+1:]...)
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.DeleteStaffMember(ctx, removed.ID)
		}
	}
	return nil
}

var protectedOwnerAdminID = uuid.MustParse("22222222-2222-2222-2222-222222222222")

func isProtectedOwnerAdmin(rec domain.StaffRecord) bool {
	if rec.ID == "staff-maria" {
		return true
	}
	return rec.UserID == protectedOwnerAdminID
}

func (s *Service) isLastActiveAdminLocked(rec domain.StaffRecord) bool {
	if rec.Role != string(domain.RoleAdmin) {
		return false
	}
	if !rec.Active {
		return false
	}
	return s.countActiveAdminsLocked() <= 1
}

func (s *Service) wouldLeaveWithoutActiveAdminLocked(before, after domain.StaffRecord) bool {
	if isProtectedOwnerAdmin(before) {
		if after.Role != string(domain.RoleAdmin) {
			return true
		}
		if !after.Active {
			return true
		}
	}
	if !s.isLastActiveAdminLocked(before) {
		return false
	}
	if after.Role != string(domain.RoleAdmin) {
		return true
	}
	return !after.Active
}

func (s *Service) countActiveAdminsLocked() int {
	count := 0
	for _, member := range s.staff {
		if member.Role != string(domain.RoleAdmin) {
			continue
		}
		if member.Active {
			count++
		}
	}
	return count
}

func (s *Service) ensureAdminClientForUserLocked(ctx context.Context, user domain.User) {
	if user.Role != domain.RoleClient {
		return
	}
	for _, existing := range s.clients {
		if strings.EqualFold(existing.Email, user.Email) {
			return
		}
	}
	cli := domain.AdminClient{
		ID:        "cli-" + user.ID.String()[:8],
		FullName:  user.FullName,
		Email:     user.Email,
		Phone:     user.Phone,
		UpdatedAt: s.clock.Now(),
	}
	s.clients = append(s.clients, cli)
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.UpsertAdminClient(ctx, cli)
		}
	}
}

func (s *Service) syncAdminRecordsFromStore(ctx context.Context) {
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	if store == nil {
		return
	}
	if !store.Enabled() {
		return
	}
	dbClients, clientErr := store.ListAdminClients(ctx)
	dbStaff, staffErr := store.ListStaffMembers(ctx)
	s.mu.Lock()
	if clientErr == nil {
		s.clients = dbClients
	}
	if staffErr == nil {
		s.staff = dbStaff
	}
	s.mu.Unlock()
}

func resolveInvitedStaffRole(rawRole string) string {
	trimmed := strings.TrimSpace(rawRole)
	lower := strings.ToLower(trimmed)
	if lower == "" {
		return string(domain.RoleStaff)
	}
	if lower == string(domain.RoleStaff) {
		return lower
	}
	if lower == string(domain.RoleAdmin) {
		return lower
	}
	return ""
}
