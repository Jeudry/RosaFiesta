package httpapi

import (
	"encoding/json"
	"errors"
	"net/http"
	"strings"

	"github.com/Jeudry/rosafiesta/server/internal/events/app"
	"github.com/Jeudry/rosafiesta/server/internal/events/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type TemplatesOutput struct {
	Body struct {
		Templates []domain.EventTemplate `json:"templates"`
	}
}

type EventsListInput struct {
	Status string `query:"status" doc:"Optional event status filter (confirmed, pending, completed, cancelled)."`
}

type EventsListOutput struct {
	Body struct {
		Events []domain.Event `json:"events"`
	}
}

type EventByIDInput struct {
	ID string `path:"id" doc:"Event identifier."`
}

type EventDetailOutput struct {
	Body struct {
		Event     domain.Event `json:"event"`
		BalanceRD int          `json:"balanceRD"`
	}
}

func newEventDetailOutput(evt domain.Event) *EventDetailOutput {
	out := &EventDetailOutput{}
	out.Body.Event = evt
	out.Body.BalanceRD = evt.BalanceRD()
	return out
}

type CreateEventInput struct {
	Body struct {
		Title          string             `json:"title"`
		EventType      string             `json:"eventType,omitempty"`
		ClientName     string             `json:"clientName,omitempty"`
		ClientEmail    string             `json:"clientEmail,omitempty"`
		DateLabel      string             `json:"dateLabel"`
		DaysUntilEvent int                `json:"daysUntilEvent,omitempty"`
		Venue          string             `json:"venue"`
		Zone           string             `json:"zone,omitempty"`
		Guests         int                `json:"guests"`
		TotalRD        int                `json:"totalRD,omitempty"`
		PaidDepositRD  int                `json:"paidDepositRD,omitempty"`
		BalanceDueDate string             `json:"balanceDueDate,omitempty"`
		AssignedLead   string             `json:"assignedLead,omitempty"`
		SketchJSON     string             `json:"sketchJson,omitempty"`
		Items          []domain.EventItem `json:"items,omitempty"`
	}
}

func (in *CreateEventInput) ToCommand() app.CreateEventCommand {
	return app.CreateEventCommand{
		Title:          in.Body.Title,
		EventType:      in.Body.EventType,
		ClientName:     in.Body.ClientName,
		ClientEmail:    in.Body.ClientEmail,
		DateLabel:      in.Body.DateLabel,
		DaysUntilEvent: in.Body.DaysUntilEvent,
		Venue:          in.Body.Venue,
		Zone:           in.Body.Zone,
		Guests:         in.Body.Guests,
		TotalRD:        in.Body.TotalRD,
		PaidDepositRD:  in.Body.PaidDepositRD,
		BalanceDueDate: in.Body.BalanceDueDate,
		AssignedLead:   in.Body.AssignedLead,
		SketchJSON:     in.Body.SketchJSON,
		Items:          in.Body.Items,
	}
}

type UpdateEventInput struct {
	ID   string `path:"id" doc:"Event identifier."`
	Body struct {
		Title          *string             `json:"title,omitempty"`
		Venue          *string             `json:"venue,omitempty"`
		Zone           *string             `json:"zone,omitempty"`
		DateLabel      *string             `json:"dateLabel,omitempty"`
		DaysUntilEvent *int                `json:"daysUntilEvent,omitempty"`
		Guests         *int                `json:"guests,omitempty"`
		Status         *string             `json:"status,omitempty"`
		AssignedLead   *string             `json:"assignedLead,omitempty"`
		SketchJSON     *string             `json:"sketchJson,omitempty"`
		Items          *[]domain.EventItem `json:"items,omitempty"`
	}
}

func (in *UpdateEventInput) ToCommand() app.UpdateEventCommand {
	return app.UpdateEventCommand{
		Title:          in.Body.Title,
		Venue:          in.Body.Venue,
		Zone:           in.Body.Zone,
		DateLabel:      in.Body.DateLabel,
		DaysUntilEvent: in.Body.DaysUntilEvent,
		Guests:         in.Body.Guests,
		Status:         in.Body.Status,
		AssignedLead:   in.Body.AssignedLead,
		SketchJSON:     in.Body.SketchJSON,
		Items:          in.Body.Items,
	}
}

type DateChangeInput struct {
	ID   string `path:"id" doc:"Event identifier."`
	Body struct {
		NewDateLabel      string `json:"newDateLabel" doc:"Updated readable event date."`
		NewDaysUntilEvent int    `json:"newDaysUntilEvent" minimum:"2"`
	}
}

type DateChangeOutput struct {
	Body struct {
		Event      domain.Event                `json:"event"`
		Evaluation domain.DateChangeEvaluation `json:"evaluation"`
	}
}

type CancelEventInput struct {
	ID      string `path:"id" doc:"Event identifier."`
	RawBody []byte
}

func (in *CancelEventInput) Reason() string {
	rawText := string(in.RawBody)
	trimmed := strings.TrimSpace(rawText)
	if trimmed == "" {
		return ""
	}
	var payload struct {
		Reason string `json:"reason"`
	}
	if err := json.Unmarshal(in.RawBody, &payload); err != nil {
		return ""
	}
	return strings.TrimSpace(payload.Reason)
}

type CancelEventOutput struct {
	Body struct {
		Event  domain.Event              `json:"event"`
		Refund domain.RefundPolicyResult `json:"refund"`
	}
}

type GuestsListOutput struct {
	Body struct {
		Guests []domain.EventGuest `json:"guests"`
	}
}

type GuestOutput struct {
	Body struct {
		Guest domain.EventGuest `json:"guest"`
	}
}

type AddGuestInput struct {
	ID   string `path:"id" doc:"Event identifier."`
	Body struct {
		FullName     string `json:"fullName"`
		RSVPStatus   string `json:"rsvpStatus,omitempty"`
		TableLabel   string `json:"tableLabel,omitempty"`
		DietaryNotes string `json:"dietaryNotes,omitempty"`
	}
}

func (in *AddGuestInput) ToCommand() app.AddGuestCommand {
	return app.AddGuestCommand{
		FullName:     in.Body.FullName,
		RSVPStatus:   in.Body.RSVPStatus,
		TableLabel:   in.Body.TableLabel,
		DietaryNotes: in.Body.DietaryNotes,
	}
}

type UpdateGuestInput struct {
	ID      string `path:"id" doc:"Event identifier."`
	GuestID string `path:"guestId" doc:"Guest identifier."`
	Body    struct {
		FullName     *string `json:"fullName,omitempty"`
		RSVPStatus   *string `json:"rsvpStatus,omitempty"`
		TableLabel   *string `json:"tableLabel,omitempty"`
		DietaryNotes *string `json:"dietaryNotes,omitempty"`
	}
}

func (in *UpdateGuestInput) ToCommand() app.UpdateGuestCommand {
	return app.UpdateGuestCommand{
		FullName:     in.Body.FullName,
		RSVPStatus:   in.Body.RSVPStatus,
		TableLabel:   in.Body.TableLabel,
		DietaryNotes: in.Body.DietaryNotes,
	}
}

type ScheduleListOutput struct {
	Body struct {
		Blocks []domain.ScheduleBlock `json:"blocks"`
	}
}

type ScheduleBlockOutput struct {
	Body struct {
		Block domain.ScheduleBlock `json:"block"`
	}
}

type AddScheduleBlockInput struct {
	ID   string `path:"id" doc:"Event identifier."`
	Body struct {
		TimeRange string `json:"timeRange"`
		Title     string `json:"title"`
		OwnerName string `json:"ownerName,omitempty"`
		Status    string `json:"status,omitempty"`
		SortOrder int    `json:"sortOrder,omitempty"`
	}
}

func (in *AddScheduleBlockInput) ToCommand() app.AddScheduleBlockCommand {
	return app.AddScheduleBlockCommand{
		TimeRange: in.Body.TimeRange,
		Title:     in.Body.Title,
		OwnerName: in.Body.OwnerName,
		Status:    in.Body.Status,
		SortOrder: in.Body.SortOrder,
	}
}

type DeleteScheduleBlockInput struct {
	ID      string `path:"id" doc:"Event identifier."`
	BlockID string `path:"blockId" doc:"Schedule block identifier."`
}

type DeleteScheduleBlockOutput struct {
	Body struct {
		Deleted bool `json:"deleted"`
	}
}

type ChatThreadsOutput struct {
	Body struct {
		Threads []domain.ChatThread `json:"threads"`
	}
}

type ChatThreadInput struct {
	ThreadID string `path:"threadId" doc:"Chat thread identifier."`
}

type ChatMessagesOutput struct {
	Body struct {
		Messages []domain.ChatMessage `json:"messages"`
	}
}

type ChatMessageOutput struct {
	Body struct {
		Message domain.ChatMessage `json:"message"`
	}
}

type PostChatMessageInput struct {
	ThreadID string `path:"threadId" doc:"Chat thread identifier."`
	Body     struct {
		EventID        string `json:"eventId,omitempty"`
		EventTitle     string `json:"eventTitle,omitempty"`
		RecipientLabel string `json:"recipientLabel,omitempty"`
		Body           string `json:"body"`
		AttachmentName string `json:"attachmentName,omitempty"`
	}
}

func (in *PostChatMessageInput) ToCommand() app.PostChatMessageCommand {
	return app.PostChatMessageCommand{
		EventID:        in.Body.EventID,
		EventTitle:     in.Body.EventTitle,
		RecipientLabel: in.Body.RecipientLabel,
		Body:           in.Body.Body,
		AttachmentName: in.Body.AttachmentName,
	}
}

func mapEventError(err error, endpoint, defaultCode, policy string, defaultStatus int) *platformhttp.APIError {
	if errors.Is(err, domain.ErrEventForbidden) {
		return platformhttp.NewAPIError(
			http.StatusForbidden,
			"FORBIDDEN_EVENT_ACCESS",
			err.Error(),
			endpoint,
			"ClientEventIsolation",
			err.Error(),
		)
	}
	if isEventNotFound(err) {
		return platformhttp.NewAPIError(
			http.StatusNotFound,
			"EVENT_NOT_FOUND",
			err.Error(),
			endpoint,
			"EventMustExist",
			err.Error(),
		)
	}
	return platformhttp.NewAPIError(
		defaultStatus,
		defaultCode,
		err.Error(),
		endpoint,
		policy,
		err.Error(),
	)
}

func isEventNotFound(err error) bool {
	if errors.Is(err, domain.ErrEventNotFound) {
		return true
	}
	if errors.Is(err, domain.ErrGuestNotFound) {
		return true
	}
	if errors.Is(err, domain.ErrScheduleBlockNotFound) {
		return true
	}
	return errors.Is(err, domain.ErrChatThreadNotFound)
}
