package worker

import (
	"context"
	"time"

	"Backend/internal/notifications"
	"Backend/internal/store"
	"Backend/internal/store/models"

	"go.uber.org/zap"
)

type NotificationSender struct {
	store  store.Storage
	logger *zap.SugaredLogger
	notif  *notifications.NotificationService
}

func NewNotificationSender(store store.Storage, logger *zap.SugaredLogger, notif *notifications.NotificationService) *NotificationSender {
	return &NotificationSender{
		store:  store,
		logger: logger,
		notif:  notif,
	}
}

func (w *NotificationSender) Start(ctx context.Context, interval time.Duration) {
	ticker := time.NewTicker(interval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			w.logger.Info("NotificationSender stopping")
			return
		case <-ticker.C:
			w.processNotifications(ctx)
		}
	}
}

func (w *NotificationSender) processNotifications(ctx context.Context) {
	// Let's get all active/confirmed events to check for pre-event reminders
	// and completed events for post-event reviews.

	now := time.Now()
	// Reminders threshold (24 hours from now)
	reminderThreshold := now.Add(24 * time.Hour)
	// 7-day reminder threshold
	sevenDaysFromNow := now.Add(7 * 24 * time.Hour)

	// We'll iterate over all events or create a custom query in a production environment.
	// For this prototype, we'll try to get all events by creating a GetAll method if we don't have one,
	// or querying all events. We'll add a helper method in EventStore if it doesn't exist.

	events, err := w.store.Events.GetAll(ctx)
	if err != nil {
		w.logger.Errorf("error fetching events for notifications: %v", err)
		return
	}

	for _, event := range events {
		// Drafts (and any other event without a date) can't trigger
		// date-based notifications.
		if event.Date == nil {
			continue
		}

		// 1. Auto-reminder 7 days before event
		if (event.Status == "confirmed" || event.Status == "paid") &&
			event.Date.After(sevenDaysFromNow.Add(-12*time.Hour)) && event.Date.Before(sevenDaysFromNow.Add(12*time.Hour)) {

			sent, err := w.store.NotificationLogs.HasNotificationBeenSent(ctx, event.ID, models.AutoReminder7d)
			if err != nil {
				w.logger.Errorf("error checking notification log: %v", err)
				continue
			}
			if !sent {
				user, err := w.store.Users.RetrieveById(ctx, event.UserID)
				if err == nil {
					_ = w.notif.NotifyStatusChange(ctx, user.FCMToken, "🎉 ¡Tu evento es en 7 días!", event.Name+" - Revisa tu checklist y prepárate")
					_ = w.store.NotificationLogs.LogNotification(ctx, event.ID, models.AutoReminder7d)
					w.logger.Infof("Sent 7-day auto-reminder for event %s", event.ID)
				}
			}
		}

		// 2. Pre-event reminder (24 hours)
		if (event.Status == "confirmed" || event.Status == "paid") &&
			event.Date.After(now) && event.Date.Before(reminderThreshold) {

			// Check if already sent
			sent, err := w.store.NotificationLogs.HasNotificationBeenSent(ctx, event.ID, models.PreEventReminder)
			if err != nil {
				w.logger.Errorf("error checking notification log: %v", err)
				continue
			}
			if !sent {
				// Send notification
				user, err := w.store.Users.RetrieveById(ctx, event.UserID)
				if err == nil {
					_ = w.notif.NotifyStatusChange(ctx, user.FCMToken, event.Name, "¡Tu evento es mañana! Recordatorio de Rosa Fiesta.")
					// Log it
					_ = w.store.NotificationLogs.LogNotification(ctx, event.ID, models.PreEventReminder)
					w.logger.Infof("Sent pre-event reminder for event %s", event.ID)
				}
			}
		}

		// 3. Post-event review
		if event.Status == "completed" || event.Status == "finished" {
			// Check if it's been at least 24h since the event ended. Or just send if completed.
			if now.After(event.Date.Add(24 * time.Hour)) {
				sent, err := w.store.NotificationLogs.HasNotificationBeenSent(ctx, event.ID, models.PostEventReview)
				if err != nil {
					w.logger.Errorf("error checking notification log: %v", err)
					continue
				}
				if !sent {
					// Send notification
					user, err := w.store.Users.RetrieveById(ctx, event.UserID)
					if err == nil {
						_ = w.notif.NotifyStatusChange(ctx, user.FCMToken, event.Name, "¡Gracias por confiar en nosotros! ¿Podrías dejarnos una reseña?")
						// Log it
						_ = w.store.NotificationLogs.LogNotification(ctx, event.ID, models.PostEventReview)
						w.logger.Infof("Sent post-event review request for event %s", event.ID)
					}
				}
			}
		}
	}
}
