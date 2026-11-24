package app

import (
	"context"
	"strings"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/events/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	realtimedomain "github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

type AddGuestCommand struct {
	FullName     string
	RSVPStatus   string
	TableLabel   string
	DietaryNotes string
}

type UpdateGuestCommand struct {
	FullName     *string
	RSVPStatus   *string
	TableLabel   *string
	DietaryNotes *string
}

type AddScheduleBlockCommand struct {
	TimeRange string
	Title     string
	OwnerName string
	Status    string
	SortOrder int
}

type PostChatMessageCommand struct {
	EventID        string
	EventTitle     string
	RecipientLabel string
	Body           string
	AttachmentName string
	AttachmentURL  string
}

func (s *Service) ListGuests(ctx context.Context, eventID string, caller platformhttp.Principal) ([]domain.EventGuest, error) {
	if _, err := s.GetEventForCaller(ctx, eventID, caller, true); err != nil {
		return nil, err
	}
	s.syncGuestsFromStore(ctx, eventID)
	s.mu.RLock()
	defer s.mu.RUnlock()
	list := s.guests[eventID]
	out := make([]domain.EventGuest, len(list))
	copy(out, list)
	return out, nil
}

func (s *Service) AddGuest(ctx context.Context, eventID string, cmd AddGuestCommand, caller platformhttp.Principal) (domain.EventGuest, error) {
	if _, err := s.GetEventForCaller(ctx, eventID, caller, true); err != nil {
		return domain.EventGuest{}, err
	}
	fullName := strings.TrimSpace(cmd.FullName)
	if fullName == "" {
		return domain.EventGuest{}, domain.ErrInvalidEventInput
	}
	rsvp := strings.TrimSpace(cmd.RSVPStatus)
	if rsvp == "" {
		rsvp = "pending"
	}
	if !domain.IsValidRSVPStatus(rsvp) {
		return domain.EventGuest{}, domain.ErrInvalidEventInput
	}
	suffix := uuid.NewString()[:8]
	guest := domain.EventGuest{
		ID:           "gst-" + suffix,
		EventID:      eventID,
		FullName:     fullName,
		RSVPStatus:   rsvp,
		TableLabel:   strings.TrimSpace(cmd.TableLabel),
		DietaryNotes: strings.TrimSpace(cmd.DietaryNotes),
		CreatedAt:    s.clock.Now(),
	}
	s.syncGuestsFromStore(ctx, eventID)
	s.mu.Lock()
	s.guests[eventID] = append(s.guests[eventID], guest)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertGuest(ctx, guest); err != nil {
				return domain.EventGuest{}, err
			}
		}
	}
	return guest, nil
}

func (s *Service) UpdateGuest(
	ctx context.Context,
	eventID string,
	guestID string,
	cmd UpdateGuestCommand,
	caller platformhttp.Principal,
) (domain.EventGuest, error) {
	if _, err := s.GetEventForCaller(ctx, eventID, caller, true); err != nil {
		return domain.EventGuest{}, err
	}
	s.syncGuestsFromStore(ctx, eventID)
	s.mu.Lock()
	list := s.guests[eventID]
	idx := -1
	for i, existing := range list {
		if existing.ID == guestID {
			idx = i
			break
		}
	}
	if idx < 0 {
		s.mu.Unlock()
		return domain.EventGuest{}, domain.ErrGuestNotFound
	}
	updated, err := applyGuestPatch(list[idx], cmd)
	if err != nil {
		s.mu.Unlock()
		return domain.EventGuest{}, err
	}
	list[idx] = updated
	s.guests[eventID] = list
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertGuest(ctx, updated); err != nil {
				return domain.EventGuest{}, err
			}
		}
	}
	return updated, nil
}

// DeleteGuest elimina un invitado de la lista del evento.
func (s *Service) DeleteGuest(
	ctx context.Context,
	eventID string,
	guestID string,
	caller platformhttp.Principal,
) error {
	if _, err := s.GetEventForCaller(ctx, eventID, caller, true); err != nil {
		return err
	}
	s.syncGuestsFromStore(ctx, eventID)
	s.mu.Lock()
	list := s.guests[eventID]
	idx := -1
	for i, existing := range list {
		if existing.ID == guestID {
			idx = i
			break
		}
	}
	if idx < 0 {
		s.mu.Unlock()
		return domain.ErrGuestNotFound
	}
	s.guests[eventID] = append(list[:idx], list[idx+1:]...)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			return store.DeleteGuest(ctx, eventID, guestID)
		}
	}
	return nil
}

// AddTeamTaskCommand encapsula los datos para crear una tarea dentro de un evento.
type AddTeamTaskCommand struct {
	Title        string
	AssigneeName string
	Done         bool
}

// UpdateTeamTaskCommand encapsula cambios parciales sobre una tarea del evento.
type UpdateTeamTaskCommand struct {
	Title        *string
	AssigneeName *string
	Done         *bool
}

// ListTeamTasks lista las tareas colaborativas del evento.
func (s *Service) ListTeamTasks(ctx context.Context, eventID string, caller platformhttp.Principal) ([]domain.EventTask, error) {
	if _, err := s.GetEventForCaller(ctx, eventID, caller, true); err != nil {
		return nil, err
	}
	s.syncTeamTasksFromStore(ctx, eventID)
	s.mu.RLock()
	defer s.mu.RUnlock()
	list := s.tasks[eventID]
	out := make([]domain.EventTask, len(list))
	copy(out, list)
	return out, nil
}

// AddTeamTask agrega una nueva tarea colaborativa al evento y la persiste.
func (s *Service) AddTeamTask(
	ctx context.Context,
	eventID string,
	cmd AddTeamTaskCommand,
	caller platformhttp.Principal,
) (domain.EventTask, error) {
	if _, err := s.GetEventForCaller(ctx, eventID, caller, true); err != nil {
		return domain.EventTask{}, err
	}
	cleanTitle := strings.TrimSpace(cmd.Title)
	if cleanTitle == "" {
		return domain.EventTask{}, domain.ErrInvalidEventInput
	}
	task := domain.EventTask{
		ID:           "tsk-" + uuid.NewString()[:8],
		EventID:      eventID,
		Title:        cleanTitle,
		AssigneeName: strings.TrimSpace(cmd.AssigneeName),
		Done:         cmd.Done,
		CreatedAt:    s.clock.Now(),
	}
	s.syncTeamTasksFromStore(ctx, eventID)
	s.mu.Lock()
	s.tasks[eventID] = append(s.tasks[eventID], task)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertTeamTask(ctx, task); err != nil {
				return domain.EventTask{}, err
			}
		}
	}
	return task, nil
}

// UpdateTeamTask actualiza el título, responsable o estado de una tarea del evento.
func (s *Service) UpdateTeamTask(
	ctx context.Context,
	eventID string,
	taskID string,
	cmd UpdateTeamTaskCommand,
	caller platformhttp.Principal,
) (domain.EventTask, error) {
	if _, err := s.GetEventForCaller(ctx, eventID, caller, true); err != nil {
		return domain.EventTask{}, err
	}
	s.syncTeamTasksFromStore(ctx, eventID)
	s.mu.Lock()
	list := s.tasks[eventID]
	idx := findTaskIndex(list, taskID)
	if idx < 0 {
		s.mu.Unlock()
		return domain.EventTask{}, domain.ErrTeamTaskNotFound
	}
	updated, patchErr := applyTeamTaskPatch(list[idx], cmd)
	if patchErr != nil {
		s.mu.Unlock()
		return domain.EventTask{}, patchErr
	}
	list[idx] = updated
	s.tasks[eventID] = list
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertTeamTask(ctx, updated); err != nil {
				return domain.EventTask{}, err
			}
		}
	}
	return updated, nil
}

// DeleteTeamTask elimina una tarea colaborativa del evento.
func (s *Service) DeleteTeamTask(
	ctx context.Context,
	eventID string,
	taskID string,
	caller platformhttp.Principal,
) error {
	if _, err := s.GetEventForCaller(ctx, eventID, caller, true); err != nil {
		return err
	}
	s.syncTeamTasksFromStore(ctx, eventID)
	s.mu.Lock()
	list := s.tasks[eventID]
	idx := findTaskIndex(list, taskID)
	if idx < 0 {
		s.mu.Unlock()
		return domain.ErrTeamTaskNotFound
	}
	s.tasks[eventID] = append(list[:idx], list[idx+1:]...)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			return store.DeleteTeamTask(ctx, eventID, taskID)
		}
	}
	return nil
}

func findTaskIndex(list []domain.EventTask, taskID string) int {
	for i, existing := range list {
		if existing.ID == taskID {
			return i
		}
	}
	return -1
}

func applyTeamTaskPatch(existing domain.EventTask, cmd UpdateTeamTaskCommand) (domain.EventTask, error) {
	updated := existing
	if cmd.Title != nil {
		cleanTitle := strings.TrimSpace(*cmd.Title)
		if cleanTitle == "" {
			return domain.EventTask{}, domain.ErrInvalidEventInput
		}
		updated.Title = cleanTitle
	}
	if cmd.AssigneeName != nil {
		updated.AssigneeName = strings.TrimSpace(*cmd.AssigneeName)
	}
	if cmd.Done != nil {
		updated.Done = *cmd.Done
	}
	return updated, nil
}

func (s *Service) syncTeamTasksFromStore(ctx context.Context, eventID string) {
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	if store == nil {
		return
	}
	if !store.Enabled() {
		return
	}
	rows, err := store.ListTeamTasks(ctx, eventID)
	if err != nil {
		return
	}
	s.mu.Lock()
	s.tasks[eventID] = rows
	s.mu.Unlock()
}

func (s *Service) ListSchedule(ctx context.Context, eventID string, caller platformhttp.Principal) ([]domain.ScheduleBlock, error) {
	if _, err := s.GetEventForCaller(ctx, eventID, caller, true); err != nil {
		return nil, err
	}
	s.syncScheduleFromStore(ctx, eventID)
	s.mu.RLock()
	defer s.mu.RUnlock()
	list := s.schedules[eventID]
	out := make([]domain.ScheduleBlock, len(list))
	copy(out, list)
	return out, nil
}

func (s *Service) AddScheduleBlock(
	ctx context.Context,
	eventID string,
	cmd AddScheduleBlockCommand,
	caller platformhttp.Principal,
) (domain.ScheduleBlock, error) {
	if _, err := s.GetEventForCaller(ctx, eventID, caller, true); err != nil {
		return domain.ScheduleBlock{}, err
	}
	title := strings.TrimSpace(cmd.Title)
	timeRange := strings.TrimSpace(cmd.TimeRange)
	if title == "" {
		return domain.ScheduleBlock{}, domain.ErrInvalidEventInput
	}
	if timeRange == "" {
		return domain.ScheduleBlock{}, domain.ErrInvalidEventInput
	}
	status := strings.TrimSpace(cmd.Status)
	if status == "" {
		status = "pending"
	}
	s.syncScheduleFromStore(ctx, eventID)
	suffix := uuid.NewString()[:8]
	s.mu.Lock()
	sortOrder := cmd.SortOrder
	if sortOrder <= 0 {
		sortOrder = len(s.schedules[eventID]) + 1
	}
	block := domain.ScheduleBlock{
		ID:        "sch-" + suffix,
		EventID:   eventID,
		TimeRange: timeRange,
		Title:     title,
		OwnerName: strings.TrimSpace(cmd.OwnerName),
		Status:    status,
		SortOrder: sortOrder,
	}
	s.schedules[eventID] = append(s.schedules[eventID], block)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertScheduleBlock(ctx, block); err != nil {
				return domain.ScheduleBlock{}, err
			}
		}
	}
	return block, nil
}

func (s *Service) DeleteScheduleBlock(
	ctx context.Context,
	eventID string,
	blockID string,
	caller platformhttp.Principal,
) error {
	if _, err := s.GetEventForCaller(ctx, eventID, caller, true); err != nil {
		return err
	}
	s.syncScheduleFromStore(ctx, eventID)
	s.mu.Lock()
	list := s.schedules[eventID]
	filtered := make([]domain.ScheduleBlock, 0, len(list))
	found := false
	for _, existing := range list {
		if existing.ID == blockID {
			found = true
			continue
		}
		filtered = append(filtered, existing)
	}
	if !found {
		s.mu.Unlock()
		return domain.ErrScheduleBlockNotFound
	}
	s.schedules[eventID] = filtered
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			return store.DeleteScheduleBlock(ctx, eventID, blockID)
		}
	}
	return nil
}

func (s *Service) ListChatThreads(ctx context.Context, caller platformhttp.Principal) []domain.ChatThread {
	s.syncChatsFromStore(ctx)
	allowedEvents := s.allowedEventIDsForCaller(ctx, caller)
	s.mu.RLock()
	snapshot := make([]domain.ChatMessage, len(s.messages))
	copy(snapshot, s.messages)
	s.mu.RUnlock()

	byThread := make(map[string]domain.ChatThread)
	order := make([]string, 0)
	for _, msg := range snapshot {
		if caller.IsClient() {
			if !isChatAllowedForClient(msg, caller, allowedEvents) {
				continue
			}
		}
		thread, exists := byThread[msg.ThreadID]
		if !exists {
			order = append(order, msg.ThreadID)
			thread = domain.ChatThread{
				ThreadID:        msg.ThreadID,
				EventID:         msg.EventID,
				EventTitle:      msg.EventTitle,
				ParticipantName: msg.SenderName,
			}
		}
		thread.LastMessage = msg.Body
		thread.UpdatedAt = msg.CreatedAt
		if msg.Unread {
			thread.UnreadCount++
		}
		byThread[msg.ThreadID] = thread
	}
	out := make([]domain.ChatThread, 0, len(order))
	for _, tid := range order {
		out = append(out, byThread[tid])
	}
	return out
}

func (s *Service) ListChatMessages(ctx context.Context, threadID string, caller platformhttp.Principal) ([]domain.ChatMessage, error) {
	s.syncChatsFromStore(ctx)
	allowedEvents := s.allowedEventIDsForCaller(ctx, caller)
	s.mu.RLock()
	snapshot := make([]domain.ChatMessage, len(s.messages))
	copy(snapshot, s.messages)
	s.mu.RUnlock()

	out := make([]domain.ChatMessage, 0)
	for _, msg := range snapshot {
		if msg.ThreadID != threadID {
			continue
		}
		if caller.IsClient() {
			if !isChatAllowedForClient(msg, caller, allowedEvents) {
				return nil, domain.ErrEventForbidden
			}
		}
		out = append(out, msg)
	}
	return out, nil
}

func (s *Service) PostChatMessage(
	ctx context.Context,
	threadID string,
	cmd PostChatMessageCommand,
	caller platformhttp.Principal,
) (domain.ChatMessage, error) {
	body := strings.TrimSpace(cmd.Body)
	if body == "" {
		return domain.ChatMessage{}, domain.ErrInvalidEventInput
	}
	cleanThread := strings.TrimSpace(threadID)
	if cleanThread == "" {
		return domain.ChatMessage{}, domain.ErrInvalidEventInput
	}
	eventID := strings.TrimSpace(cmd.EventID)
	if eventID == "" {
		eventID = strings.TrimPrefix(cleanThread, "thr-")
	}
	if caller.IsClient() {
		if eventID != "" {
			if _, err := s.GetEventForCaller(ctx, eventID, caller, true); err != nil {
				return domain.ChatMessage{}, err
			}
		}
	}
	suffix := uuid.NewString()[:8]
	senderName := caller.FullName
	if senderName == "" {
		senderName = "RosaFiesta"
	}
	msg := domain.ChatMessage{
		ID:             "msg-" + suffix,
		ThreadID:       cleanThread,
		EventID:        eventID,
		EventTitle:     strings.TrimSpace(cmd.EventTitle),
		SenderID:       caller.ID.String(),
		SenderName:     senderName,
		SenderRole:     caller.Role,
		RecipientLabel: strings.TrimSpace(cmd.RecipientLabel),
		Body:           body,
		AttachmentName: strings.TrimSpace(cmd.AttachmentName),
		AttachmentURL:  strings.TrimSpace(cmd.AttachmentURL),
		Unread:         false,
		CreatedAt:      s.clock.Now(),
	}
	s.syncChatsFromStore(ctx)
	s.mu.Lock()
	s.messages = append(s.messages, msg)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertChatMessage(ctx, msg); err != nil {
				return domain.ChatMessage{}, err
			}
		}
	}
	s.publishChatMessage(ctx, msg)
	return msg, nil
}

func (s *Service) publishChatMessage(ctx context.Context, msg domain.ChatMessage) {
	s.mu.RLock()
	pub := s.realtimePub
	s.mu.RUnlock()
	if pub == nil {
		return
	}
	clientID := ""
	audience := realtimedomain.AudienceAdmin
	if msg.EventID != "" {
		if evt, found := s.findEventByID(ctx, msg.EventID); found {
			if evt.ClientID != uuid.Nil {
				clientID = evt.ClientID.String()
				audience = realtimedomain.AudienceClient
			}
		}
	}
	pub.Publish(realtimedomain.RealtimeEvent{
		Type:     realtimedomain.EventChatMessage,
		Audience: audience,
		ClientID: clientID,
		EntityID: msg.ThreadID,
		Payload: map[string]any{
			"messageId":     msg.ID,
			"threadId":      msg.ThreadID,
			"eventId":       msg.EventID,
			"senderRole":    msg.SenderRole,
			"attachmentUrl": msg.AttachmentURL,
		},
	})
}

func applyGuestPatch(existing domain.EventGuest, cmd UpdateGuestCommand) (domain.EventGuest, error) {
	updated := existing
	if cmd.FullName != nil {
		trimmed := strings.TrimSpace(*cmd.FullName)
		if trimmed == "" {
			return domain.EventGuest{}, domain.ErrInvalidEventInput
		}
		updated.FullName = trimmed
	}
	if cmd.RSVPStatus != nil {
		status := strings.TrimSpace(*cmd.RSVPStatus)
		if !domain.IsValidRSVPStatus(status) {
			return domain.EventGuest{}, domain.ErrInvalidEventInput
		}
		updated.RSVPStatus = status
	}
	if cmd.TableLabel != nil {
		updated.TableLabel = strings.TrimSpace(*cmd.TableLabel)
	}
	if cmd.DietaryNotes != nil {
		updated.DietaryNotes = strings.TrimSpace(*cmd.DietaryNotes)
	}
	return updated, nil
}

func (s *Service) allowedEventIDsForCaller(ctx context.Context, caller platformhttp.Principal) map[string]bool {
	events := s.ListEventsForCaller(ctx, "", caller, true)
	allowed := make(map[string]bool, len(events))
	for _, evt := range events {
		allowed[evt.ID] = true
	}
	return allowed
}

func isChatAllowedForClient(msg domain.ChatMessage, caller platformhttp.Principal, allowedEvents map[string]bool) bool {
	if msg.SenderID == caller.ID.String() {
		return true
	}
	if msg.EventID != "" {
		if allowedEvents[msg.EventID] {
			return true
		}
	}
	return false
}

func (s *Service) syncGuestsFromStore(ctx context.Context, eventID string) {
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	if store == nil {
		return
	}
	if !store.Enabled() {
		return
	}
	rows, err := store.ListGuests(ctx, eventID)
	if err != nil {
		return
	}
	if len(rows) > 0 {
		s.mu.Lock()
		s.guests[eventID] = rows
		s.mu.Unlock()
	}
}

func (s *Service) syncScheduleFromStore(ctx context.Context, eventID string) {
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	if store == nil {
		return
	}
	if !store.Enabled() {
		return
	}
	rows, err := store.ListScheduleBlocks(ctx, eventID)
	if err != nil {
		return
	}
	s.mu.Lock()
	s.schedules[eventID] = rows
	s.mu.Unlock()
}

func (s *Service) syncChatsFromStore(ctx context.Context) {
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	if store == nil {
		return
	}
	if !store.Enabled() {
		return
	}
	rows, err := store.ListAllChatMessages(ctx)
	if err != nil {
		return
	}
	if len(rows) > 0 {
		s.mu.Lock()
		s.messages = rows
		s.mu.Unlock()
	}
}
