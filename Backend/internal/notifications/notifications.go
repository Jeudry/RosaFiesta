package notifications

import (
	"context"
	"fmt"
	"log"
)

// NotificationService handles sending push notifications via FCM.
type NotificationService struct {
	// In a real implementation, this would hold the Firebase App or HTTP client.
}

func NewNotificationService() *NotificationService {
	return &NotificationService{}
}

// SendPush sends a simple notification to a specific FCM token.
func (s *NotificationService) SendPush(ctx context.Context, token, title, body string) error {
	if token == "" {
		return nil // Nothing to do
	}

	// MOCK: In a real scenario, we would use the Firebase Admin SDK here.
	log.Printf("[FCM MOCK] Sending notification to %s: %s - %s", token, title, body)

	// Example of what the SDK call would look like:
	// client.Send(ctx, &messaging.Message{
	//     Token: token,
	//     Notification: &messaging.Notification{
	//         Title: title,
	//         Body:  body,
	//     },
	// })

	return nil
}

// SendPushToUser is a helper to find a user's token and send the push.
// (This logic might be better placed in the application layer if we need the Store).
func (s *NotificationService) NotifyStatusChange(ctx context.Context, token, eventName, newStatus string) error {
	title := fmt.Sprintf("Actualización de %s", eventName)
	body := fmt.Sprintf("Tu evento ahora está en estado: %s", newStatus)
	return s.SendPush(ctx, token, title, body)
}
