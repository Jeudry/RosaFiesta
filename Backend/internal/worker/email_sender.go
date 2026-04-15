package worker

import (
	"context"
	"fmt"
	"time"

	"Backend/internal/mailer"
	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/google/uuid"
	"go.uber.org/zap"
)

type EmailSender struct {
	store  store.Storage
	logger *zap.SugaredLogger
	mailer mailer.Client
}

func NewEmailSender(store store.Storage, logger *zap.SugaredLogger, mailer mailer.Client) *EmailSender {
	return &EmailSender{
		store:  store,
		logger: logger,
		mailer: mailer,
	}
}

func (w *EmailSender) Start(ctx context.Context, interval time.Duration) {
	ticker := time.NewTicker(interval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			w.logger.Info("EmailSender stopping")
			return
		case <-ticker.C:
			w.processEmailReminders(ctx)
		}
	}
}

func (w *EmailSender) processEmailReminders(ctx context.Context) {
	now := time.Now()

	// 7-day reminder threshold
	sevenDaysFromNow := now.Add(7 * 24 * time.Hour)
	// 24-hour reminder threshold
	twentyFourHoursFromNow := now.Add(24 * time.Hour)

	events, err := w.store.Events.GetAll(ctx)
	if err != nil {
		w.logger.Errorf("error fetching events for email reminders: %v", err)
		return
	}

	for _, event := range events {
		if event.Date == nil {
			continue
		}

		user, err := w.store.Users.RetrieveById(ctx, event.UserID)
		if err != nil {
			continue
		}

		emailData := map[string]any{
			"UserName":        user.FirstName,
			"EventName":       event.Name,
			"EventDate":       event.Date.Format("02 Jan 2006"),
			"EventTime":       "",
			"EventLocation":   event.Location,
			"ReviewURL":       "https://rosafiesta.com/reviews",
			"ChecklistURL":    fmt.Sprintf("https://rosafiesta.com/event/%s/checklist", event.ID.String()),
		}

		// 7-day email reminder (±12h window)
		if event.Date.After(sevenDaysFromNow.Add(-12*time.Hour)) && event.Date.Before(sevenDaysFromNow.Add(12*time.Hour)) {
			if event.Status == "confirmed" || event.Status == "paid" {
				sent, _ := w.store.NotificationLogs.HasNotificationBeenSent(ctx, event.ID, models.AutoReminder7d)
				if !sent {
					_, err := w.mailer.Send(mailer.AutoReminder7dTemplate, user.FirstName, user.Email, emailData, false)
					if err == nil {
						_ = w.store.NotificationLogs.LogNotification(ctx, event.ID, models.AutoReminder7d)
						w.logger.Infof("Sent 7-day auto-reminder for event %s to %s", event.ID, user.Email)
					}
				}
			}
		}

		// 24-hour email reminder (±30min window)
		if event.Date.After(twentyFourHoursFromNow.Add(-30*time.Minute)) && event.Date.Before(twentyFourHoursFromNow.Add(30*time.Minute)) {
			if event.Status == "confirmed" || event.Status == "paid" {
				sent, _ := w.store.NotificationLogs.HasNotificationBeenSent(ctx, event.ID, models.EmailEventReminder24h)
				if !sent {
					_, err := w.mailer.Send(mailer.EventReminder24hTemplate, user.FirstName, user.Email, emailData, false)
					if err == nil {
						_ = w.store.NotificationLogs.LogNotification(ctx, event.ID, models.EmailEventReminder24h)
						w.logger.Infof("Sent 24h email reminder for event %s to %s", event.ID, user.Email)
					}
				}
			}
		}

		// Post-event thank you email (day after event)
		yesterday := time.Now().AddDate(0, 0, -1).Truncate(24 * time.Hour)
		tomorrow := yesterday.AddDate(0, 0, 2).Truncate(24 * time.Hour)
		if event.Date.After(yesterday) && event.Date.Before(tomorrow) {
			if event.Status == "confirmed" || event.Status == "paid" {
				sent, _ := w.store.NotificationLogs.HasNotificationBeenSent(ctx, event.ID, models.EmailEventThankYou)
				if !sent {
					_, err := w.mailer.Send(mailer.EventThankYouTemplate, user.FirstName, user.Email, emailData, false)
					if err == nil {
						_ = w.store.NotificationLogs.LogNotification(ctx, event.ID, models.EmailEventThankYou)
						w.logger.Infof("Sent thank-you email for event %s to %s", event.ID, user.Email)
					}
				}
			}
		}
	}
}

// SendThankYouEmail sends a one-off thank you email for a specific event.
func (w *EmailSender) SendThankYouEmail(ctx context.Context, eventID uuid.UUID) error {
	event, err := w.store.Events.GetByID(ctx, eventID)
	if err != nil {
		return err
	}

	user, err := w.store.Users.RetrieveById(ctx, event.UserID)
	if err != nil {
		return err
	}

	emailData := map[string]any{
		"UserName":      user.FirstName,
		"EventName":     event.Name,
		"EventDate":     "",
		"EventLocation": "",
		"ReviewURL":     "https://rosafiesta.com/reviews",
	}

	_, err = w.mailer.Send(mailer.EventThankYouTemplate, user.FirstName, user.Email, emailData, false)
	return err
}
