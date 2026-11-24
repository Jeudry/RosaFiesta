package httpapi

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	"github.com/Jeudry/rosafiesta/server/internal/events/app"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type Module struct {
	Service *app.Service
	Auth    platformhttp.PrincipalResolver
}

func (m Module) Register(api huma.API) {
	m.registerReadRoutes(api)
	m.registerMutationRoutes(api)
	m.registerGuestAndScheduleRoutes(api)
	m.registerChatRoutes(api)
}

func (m Module) authMiddlewares(api huma.API) huma.Middlewares {
	if m.Auth == nil {
		return nil
	}
	return huma.Middlewares{platformhttp.RequireAuth(api, m.Auth)}
}

func (m Module) closeoutMiddlewares(api huma.API) huma.Middlewares {
	if m.Auth == nil {
		return nil
	}
	if permAuth, ok := m.Auth.(platformhttp.PermissionAuthorizer); ok {
		return huma.Middlewares{platformhttp.RequirePermission(api, permAuth, "eventos")}
	}
	return huma.Middlewares{platformhttp.RequireAuth(api, m.Auth)}
}

func (m Module) registerReadRoutes(api huma.API) {
	mw := m.authMiddlewares(api)

	huma.Register(api, huma.Operation{
		OperationID: "events-list-templates",
		Method:      http.MethodGet,
		Path:        "/v1/events/templates",
		Summary:     "List canonical event type templates",
		Tags:        []string{"Events"},
	}, m.handleListTemplates)

	huma.Register(api, huma.Operation{
		OperationID: "events-list",
		Method:      http.MethodGet,
		Path:        "/v1/events",
		Summary:     "List events scoped by caller role",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleListEvents)

	huma.Register(api, huma.Operation{
		OperationID: "events-get-by-id",
		Method:      http.MethodGet,
		Path:        "/v1/events/{id}",
		Summary:     "Get event detail and pending balance",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleGetEvent)
}

func (m Module) registerMutationRoutes(api huma.API) {
	mw := m.authMiddlewares(api)

	huma.Register(api, huma.Operation{
		OperationID: "events-create",
		Method:      http.MethodPost,
		Path:        "/v1/events",
		Summary:     "Create a new event reservation",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleCreateEvent)

	huma.Register(api, huma.Operation{
		OperationID: "events-update",
		Method:      http.MethodPatch,
		Path:        "/v1/events/{id}",
		Summary:     "Update event details, items, or sketch layout",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleUpdateEvent)

	huma.Register(api, huma.Operation{
		OperationID: "events-duplicate",
		Method:      http.MethodPost,
		Path:        "/v1/events/{id}/duplicate",
		Summary:     "Duplicate an existing event into a draft copy",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleDuplicateEvent)

	huma.Register(api, huma.Operation{
		OperationID: "events-request-date-change",
		Method:      http.MethodPost,
		Path:        "/v1/events/{id}/date-change",
		Summary:     "Request an event date change under contract policy",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleDateChange)

	huma.Register(api, huma.Operation{
		OperationID: "events-cancel",
		Method:      http.MethodPost,
		Path:        "/v1/events/{id}/cancel",
		Summary:     "Cancel an event, persist reason, and record refund",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleCancelEvent)

	m.registerCloseoutRoute(api)
}

func (m Module) registerCloseoutRoute(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "events-closeout",
		Method:      http.MethodPost,
		Path:        "/v1/events/{id}/closeout",
		Summary:     "Complete administrative closeout and release reserved inventory",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: m.closeoutMiddlewares(api),
	}, m.handleCloseoutEvent)

	huma.Register(api, huma.Operation{
		OperationID: "events-payment-reminder",
		Method:      http.MethodPost,
		Path:        "/v1/events/{id}/payment-reminder",
		Summary:     "Dispatch and audit a client payment reminder for an event",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: m.closeoutMiddlewares(api),
	}, m.handlePaymentReminder)
}

func (m Module) registerGuestAndScheduleRoutes(api huma.API) {
	m.registerGuestRoutes(api)
	m.registerScheduleRoutes(api)
}

func (m Module) registerGuestRoutes(api huma.API) {
	mw := m.authMiddlewares(api)

	huma.Register(api, huma.Operation{
		OperationID: "events-list-guests",
		Method:      http.MethodGet,
		Path:        "/v1/events/{id}/guests",
		Summary:     "List event guests and RSVP statuses",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleListGuests)

	huma.Register(api, huma.Operation{
		OperationID: "events-add-guest",
		Method:      http.MethodPost,
		Path:        "/v1/events/{id}/guests",
		Summary:     "Add an event guest",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleAddGuest)

	huma.Register(api, huma.Operation{
		OperationID: "events-update-guest",
		Method:      http.MethodPatch,
		Path:        "/v1/events/{id}/guests/{guestId}",
		Summary:     "Update guest RSVP status, table, or dietary notes",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleUpdateGuest)

	huma.Register(api, huma.Operation{
		OperationID: "events-delete-guest",
		Method:      http.MethodDelete,
		Path:        "/v1/events/{id}/guests/{guestId}",
		Summary:     "Remove a guest from an event",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleDeleteGuest)

	m.registerTeamTaskRoutes(api, mw)
}

func (m Module) registerTeamTaskRoutes(api huma.API, mw huma.Middlewares) {
	huma.Register(api, huma.Operation{
		OperationID: "events-list-tasks",
		Method:      http.MethodGet,
		Path:        "/v1/events/{id}/tasks",
		Summary:     "List event collaborative tasks",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleListTeamTasks)

	huma.Register(api, huma.Operation{
		OperationID: "events-add-task",
		Method:      http.MethodPost,
		Path:        "/v1/events/{id}/tasks",
		Summary:     "Create an event collaborative task",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleAddTeamTask)

	huma.Register(api, huma.Operation{
		OperationID: "events-update-task",
		Method:      http.MethodPatch,
		Path:        "/v1/events/{id}/tasks/{taskId}",
		Summary:     "Update an event collaborative task",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleUpdateTeamTask)

	huma.Register(api, huma.Operation{
		OperationID: "events-delete-task",
		Method:      http.MethodDelete,
		Path:        "/v1/events/{id}/tasks/{taskId}",
		Summary:     "Delete an event collaborative task",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleDeleteTeamTask)
}

func (m Module) registerScheduleRoutes(api huma.API) {
	mw := m.authMiddlewares(api)

	huma.Register(api, huma.Operation{
		OperationID: "events-list-schedule",
		Method:      http.MethodGet,
		Path:        "/v1/events/{id}/schedule",
		Summary:     "List event schedule blocks",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleListSchedule)

	huma.Register(api, huma.Operation{
		OperationID: "events-add-schedule",
		Method:      http.MethodPost,
		Path:        "/v1/events/{id}/schedule",
		Summary:     "Add a schedule block to an event",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleAddSchedule)

	huma.Register(api, huma.Operation{
		OperationID: "events-delete-schedule",
		Method:      http.MethodDelete,
		Path:        "/v1/events/{id}/schedule/{blockId}",
		Summary:     "Remove a schedule block from an event",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleDeleteSchedule)
}

func (m Module) registerChatRoutes(api huma.API) {
	mw := m.authMiddlewares(api)

	huma.Register(api, huma.Operation{
		OperationID: "chats-list-threads",
		Method:      http.MethodGet,
		Path:        "/v1/chats",
		Summary:     "List event chat threads",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleListChatThreads)

	huma.Register(api, huma.Operation{
		OperationID: "chats-list-messages",
		Method:      http.MethodGet,
		Path:        "/v1/chats/{threadId}/messages",
		Summary:     "List persisted messages in a chat thread",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleListChatMessages)

	huma.Register(api, huma.Operation{
		OperationID: "chats-post-message",
		Method:      http.MethodPost,
		Path:        "/v1/chats/{threadId}/messages",
		Summary:     "Persist a new message in a chat thread",
		Tags:        []string{"Events"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handlePostChatMessage)
}

func (m Module) handleListTemplates(_ context.Context, _ *struct{}) (*TemplatesOutput, error) {
	out := &TemplatesOutput{}
	out.Body.Templates = m.Service.ListTemplates()
	return out, nil
}

func (m Module) handleListEvents(ctx context.Context, in *EventsListInput) (*EventsListOutput, error) {
	caller, authenticated := platformhttp.CurrentPrincipal(ctx)
	out := &EventsListOutput{}
	out.Body.Events = m.Service.ListEventsForCaller(ctx, in.Status, caller, authenticated)
	return out, nil
}

func (m Module) handleGetEvent(ctx context.Context, in *EventByIDInput) (*EventDetailOutput, error) {
	caller, authenticated := platformhttp.CurrentPrincipal(ctx)
	evt, err := m.Service.GetEventForCaller(ctx, in.ID, caller, authenticated)
	if err != nil {
		return nil, mapEventError(err, "GET /v1/events/"+in.ID, "EVENT_NOT_FOUND", "EventMustExist", http.StatusNotFound)
	}
	return newEventDetailOutput(evt), nil
}

func (m Module) handleCreateEvent(ctx context.Context, in *CreateEventInput) (*EventDetailOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	cmd := in.ToCommand()
	evt, err := m.Service.CreateEvent(ctx, cmd, caller)
	if err != nil {
		return nil, mapEventError(err, "POST /v1/events", "EVENT_INVALID_INPUT", "ValidateEventCreation", http.StatusBadRequest)
	}
	return newEventDetailOutput(evt), nil
}

func (m Module) handleUpdateEvent(ctx context.Context, in *UpdateEventInput) (*EventDetailOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	cmd := in.ToCommand()
	evt, err := m.Service.UpdateEvent(ctx, in.ID, cmd, caller)
	if err != nil {
		return nil, mapEventError(err, "PATCH /v1/events/"+in.ID, "EVENT_UPDATE_REJECTED", "ValidateEventUpdate", http.StatusBadRequest)
	}
	return newEventDetailOutput(evt), nil
}

func (m Module) handleDuplicateEvent(ctx context.Context, in *EventByIDInput) (*EventDetailOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	evt, err := m.Service.DuplicateEvent(ctx, in.ID, caller)
	if err != nil {
		return nil, mapEventError(err, "POST /v1/events/"+in.ID+"/duplicate", "EVENT_DUPLICATE_FAILED", "DuplicateEvent", http.StatusBadRequest)
	}
	return newEventDetailOutput(evt), nil
}

func (m Module) handleDateChange(ctx context.Context, in *DateChangeInput) (*DateChangeOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	evt, eval, err := m.Service.RequestDateChangeForCaller(ctx, in.ID, in.Body.NewDateLabel, in.Body.NewDaysUntilEvent, caller)
	if err != nil {
		return nil, mapEventError(err, "POST /v1/events/"+in.ID+"/date-change", "EVENT_DATE_CHANGE_REJECTED", "DateChangePolicy", http.StatusUnprocessableEntity)
	}
	out := &DateChangeOutput{}
	out.Body.Event = evt
	out.Body.Evaluation = eval
	return out, nil
}

func (m Module) handleCancelEvent(ctx context.Context, in *CancelEventInput) (*CancelEventOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	reason := in.Reason()
	evt, refund, err := m.Service.CancelEventForCaller(ctx, in.ID, reason, caller)
	if err != nil {
		return nil, mapEventError(err, "POST /v1/events/"+in.ID+"/cancel", "EVENT_CANCEL_REJECTED", "RefundCancellationPolicy", http.StatusUnprocessableEntity)
	}
	out := &CancelEventOutput{}
	out.Body.Event = evt
	out.Body.Refund = refund
	return out, nil
}

func (m Module) handleCloseoutEvent(ctx context.Context, in *CloseoutEventInput) (*EventDetailOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	evt, err := m.Service.CompleteCloseoutForCaller(ctx, in.ID, in.Body.Notes, caller)
	if err != nil {
		return nil, mapEventError(err, "POST /v1/events/"+in.ID+"/closeout", "EVENT_CLOSEOUT_REJECTED", "CompleteEventCloseout", http.StatusConflict)
	}
	return newEventDetailOutput(evt), nil
}

func (m Module) handlePaymentReminder(ctx context.Context, in *PaymentReminderInput) (*PaymentReminderOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	channel := in.ChannelOrDefault()
	evt, sent, err := m.Service.SendPaymentReminderForCaller(ctx, in.ID, channel, in.CustomMessage(), caller)
	if err != nil {
		return nil, mapEventError(err, "POST /v1/events/"+in.ID+"/payment-reminder", "EVENT_REMINDER_FAILED", "DispatchPaymentReminder", http.StatusBadRequest)
	}
	out := &PaymentReminderOutput{}
	out.Body.Event = evt
	out.Body.Sent = sent
	out.Body.Channel = channel
	return out, nil
}

func (m Module) handleListGuests(ctx context.Context, in *EventByIDInput) (*GuestsListOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	guests, err := m.Service.ListGuests(ctx, in.ID, caller)
	if err != nil {
		return nil, mapEventError(err, "GET /v1/events/"+in.ID+"/guests", "EVENT_GUESTS_FAILED", "ListEventGuests", http.StatusBadRequest)
	}
	out := &GuestsListOutput{}
	out.Body.Guests = guests
	return out, nil
}

func (m Module) handleAddGuest(ctx context.Context, in *AddGuestInput) (*GuestOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	cmd := in.ToCommand()
	guest, err := m.Service.AddGuest(ctx, in.ID, cmd, caller)
	if err != nil {
		return nil, mapEventError(err, "POST /v1/events/"+in.ID+"/guests", "EVENT_GUEST_INVALID", "AddEventGuest", http.StatusBadRequest)
	}
	out := &GuestOutput{}
	out.Body.Guest = guest
	return out, nil
}

func (m Module) handleUpdateGuest(ctx context.Context, in *UpdateGuestInput) (*GuestOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	cmd := in.ToCommand()
	guest, err := m.Service.UpdateGuest(ctx, in.ID, in.GuestID, cmd, caller)
	if err != nil {
		return nil, mapEventError(err, "PATCH /v1/events/"+in.ID+"/guests/"+in.GuestID, "EVENT_GUEST_UPDATE_FAILED", "UpdateEventGuest", http.StatusBadRequest)
	}
	out := &GuestOutput{}
	out.Body.Guest = guest
	return out, nil
}

func (m Module) handleListSchedule(ctx context.Context, in *EventByIDInput) (*ScheduleListOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	blocks, err := m.Service.ListSchedule(ctx, in.ID, caller)
	if err != nil {
		return nil, mapEventError(err, "GET /v1/events/"+in.ID+"/schedule", "EVENT_SCHEDULE_FAILED", "ListEventSchedule", http.StatusBadRequest)
	}
	out := &ScheduleListOutput{}
	out.Body.Blocks = blocks
	return out, nil
}

func (m Module) handleAddSchedule(ctx context.Context, in *AddScheduleBlockInput) (*ScheduleBlockOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	cmd := in.ToCommand()
	block, err := m.Service.AddScheduleBlock(ctx, in.ID, cmd, caller)
	if err != nil {
		return nil, mapEventError(err, "POST /v1/events/"+in.ID+"/schedule", "EVENT_SCHEDULE_INVALID", "AddScheduleBlock", http.StatusBadRequest)
	}
	out := &ScheduleBlockOutput{}
	out.Body.Block = block
	return out, nil
}

func (m Module) handleDeleteSchedule(ctx context.Context, in *DeleteScheduleBlockInput) (*DeleteScheduleBlockOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	err := m.Service.DeleteScheduleBlock(ctx, in.ID, in.BlockID, caller)
	if err != nil {
		return nil, mapEventError(err, "DELETE /v1/events/"+in.ID+"/schedule/"+in.BlockID, "EVENT_SCHEDULE_DELETE_FAILED", "DeleteScheduleBlock", http.StatusBadRequest)
	}
	out := &DeleteScheduleBlockOutput{}
	out.Body.Deleted = true
	return out, nil
}

func (m Module) handleListChatThreads(ctx context.Context, _ *struct{}) (*ChatThreadsOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	out := &ChatThreadsOutput{}
	out.Body.Threads = m.Service.ListChatThreads(ctx, caller)
	return out, nil
}

func (m Module) handleListChatMessages(ctx context.Context, in *ChatThreadInput) (*ChatMessagesOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	messages, err := m.Service.ListChatMessages(ctx, in.ThreadID, caller)
	if err != nil {
		return nil, mapEventError(err, "GET /v1/chats/"+in.ThreadID+"/messages", "CHAT_MESSAGES_FAILED", "ListChatMessages", http.StatusBadRequest)
	}
	out := &ChatMessagesOutput{}
	out.Body.Messages = messages
	return out, nil
}

func (m Module) handlePostChatMessage(ctx context.Context, in *PostChatMessageInput) (*ChatMessageOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	cmd := in.ToCommand()
	msg, err := m.Service.PostChatMessage(ctx, in.ThreadID, cmd, caller)
	if err != nil {
		return nil, mapEventError(err, "POST /v1/chats/"+in.ThreadID+"/messages", "CHAT_POST_FAILED", "PostChatMessage", http.StatusBadRequest)
	}
	out := &ChatMessageOutput{}
	out.Body.Message = msg
	return out, nil
}

func (m Module) handleDeleteGuest(ctx context.Context, in *DeleteGuestInput) (*DeleteGuestOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	if err := m.Service.DeleteGuest(ctx, in.ID, in.GuestID, caller); err != nil {
		return nil, mapEventError(err, "DELETE /v1/events/"+in.ID+"/guests/"+in.GuestID, "EVENT_GUEST_DELETE_FAILED", "DeleteEventGuest", http.StatusBadRequest)
	}
	out := &DeleteGuestOutput{}
	out.Body.Deleted = true
	return out, nil
}

func (m Module) handleListTeamTasks(ctx context.Context, in *EventByIDInput) (*TeamTasksListOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	tasks, err := m.Service.ListTeamTasks(ctx, in.ID, caller)
	if err != nil {
		return nil, mapEventError(err, "GET /v1/events/"+in.ID+"/tasks", "EVENT_TASKS_FAILED", "ListEventTasks", http.StatusBadRequest)
	}
	out := &TeamTasksListOutput{}
	out.Body.Tasks = tasks
	return out, nil
}

func (m Module) handleAddTeamTask(ctx context.Context, in *AddTeamTaskInput) (*TeamTaskOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	task, err := m.Service.AddTeamTask(ctx, in.ID, in.ToCommand(), caller)
	if err != nil {
		return nil, mapEventError(err, "POST /v1/events/"+in.ID+"/tasks", "EVENT_TASK_INVALID", "AddEventTask", http.StatusBadRequest)
	}
	out := &TeamTaskOutput{}
	out.Body.Task = task
	return out, nil
}

func (m Module) handleUpdateTeamTask(ctx context.Context, in *UpdateTeamTaskInput) (*TeamTaskOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	task, err := m.Service.UpdateTeamTask(ctx, in.ID, in.TaskID, in.ToCommand(), caller)
	if err != nil {
		return nil, mapEventError(err, "PATCH /v1/events/"+in.ID+"/tasks/"+in.TaskID, "EVENT_TASK_UPDATE_FAILED", "UpdateEventTask", http.StatusBadRequest)
	}
	out := &TeamTaskOutput{}
	out.Body.Task = task
	return out, nil
}

func (m Module) handleDeleteTeamTask(ctx context.Context, in *DeleteTeamTaskInput) (*DeleteTeamTaskOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	if err := m.Service.DeleteTeamTask(ctx, in.ID, in.TaskID, caller); err != nil {
		return nil, mapEventError(err, "DELETE /v1/events/"+in.ID+"/tasks/"+in.TaskID, "EVENT_TASK_DELETE_FAILED", "DeleteEventTask", http.StatusBadRequest)
	}
	out := &DeleteTeamTaskOutput{}
	out.Body.Deleted = true
	return out, nil
}
