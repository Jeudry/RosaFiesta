package worker

import (
	"context"
	"fmt"
	"time"

	"Backend/internal/notifications"
	"Backend/internal/store"

	"go.uber.org/zap"
)

type DelayChecker struct {
	store         store.Storage
	logger        *zap.SugaredLogger
	notifications *notifications.NotificationService
}

func NewDelayChecker(store store.Storage, logger *zap.SugaredLogger, notifications *notifications.NotificationService) *DelayChecker {
	return &DelayChecker{
		store:         store,
		logger:        logger,
		notifications: notifications,
	}
}

func (c *DelayChecker) Start(ctx context.Context) {
	ticker := time.NewTicker(5 * time.Minute)
	defer ticker.Stop()

	c.logger.Info("Delay checker worker started")

	for {
		select {
		case <-ctx.Done():
			c.logger.Info("Delay checker worker stopping")
			return
		case <-ticker.C:
			c.checkDelayedItems(ctx)
		}
	}
}

func (c *DelayChecker) checkDelayedItems(ctx context.Context) {
	c.logger.Debug("Checking for overdue critical timeline items...")

	items, err := c.store.Timeline.GetOverdueCriticalItems(ctx)
	if err != nil {
		c.logger.Errorf("Error fetching overdue critical items: %v", err)
		return
	}

	if len(items) == 0 {
		return
	}

	// Fetch organizer tokens once per check cycle
	organizerTokens, err := c.store.Users.GetOrganizersFCMTokens(ctx)
	if err != nil {
		c.logger.Errorf("Error fetching organizer FCM tokens: %v", err)
		// We'll continue anyway to at least notify the event owner
		organizerTokens = []string{}
	}

	for _, item := range items {
		c.logger.Warnf("Critical item delayed: %s (Event: %s)", item.Title, item.EventID)

		title := "¡ALERTA CRÍTICA!"
		body := fmt.Sprintf("El ítem '%s' lleva más de 15 minutos retrasado.", item.Title)

		// 1. Notify the event owner
		if item.UserFCMToken != "" {
			err := c.notifications.SendPush(ctx, item.UserFCMToken, title, body)
			if err != nil {
				c.logger.Errorf("Error sending push notification to owner: %v", err)
			}
		} else {
			c.logger.Warnf("No FCM token for owner of event %s, skipping push to owner", item.EventID)
		}

		// 2. Notify all organizers
		for _, orgToken := range organizerTokens {
			// Avoid double-notifying if the owner is also an organizer
			if orgToken == item.UserFCMToken {
				continue
			}

			err := c.notifications.SendPush(ctx, orgToken, title, body)
			if err != nil {
				c.logger.Errorf("Error sending push notification to organizer: %v", err)
			}
		}
	}
}
