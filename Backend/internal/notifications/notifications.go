package notifications

import (
	"context"
	"fmt"
	"log"

	firebase "firebase.google.com/go/v4"
	"firebase.google.com/go/v4/messaging"
	"google.golang.org/api/option"
)

// NotificationService handles sending push notifications via FCM.
type NotificationService struct {
	client  *messaging.Client
	enabled bool
}

// NewNotificationService creates a new FCM notification service.
// Falls back to mock mode if Firebase credentials are not available.
func NewNotificationService() (*NotificationService, error) {
	ctx := context.Background()

	// Try to initialize Firebase Admin SDK
	opt := option.WithCredentialsFile("/etc/secrets/firebase-service-account.json")
	app, err := firebase.NewApp(ctx, nil, opt)
	if err != nil {
		// Try without explicit credentials (will use ADC in Cloud Run, GKE, etc.)
		app, err = firebase.NewApp(ctx, nil)
		if err != nil {
			log.Printf("[FCM] Firebase not configured, using mock mode: %v", err)
			return &NotificationService{enabled: false}, nil
		}
	}

	client, err := app.Messaging(ctx)
	if err != nil {
		log.Printf("[FCM] Failed to get Firebase Messaging client, using mock mode: %v", err)
		return &NotificationService{enabled: false}, nil
	}

	return &NotificationService{client: client, enabled: true}, nil
}

// SendPush sends a push notification to a specific FCM token.
func (s *NotificationService) SendPush(ctx context.Context, token, title, body string) error {
	if token == "" {
		return nil
	}

	if !s.enabled {
		log.Printf("[FCM MOCK] Sending notification to %s: %s - %s", token, title, body)
		return nil
	}

	message := &messaging.Message{
		Token: token,
		Notification: &messaging.Notification{
			Title: title,
			Body:  body,
		},
		Android: &messaging.AndroidConfig{
			Notification: &messaging.AndroidNotification{
				Icon: "ic_notification",
				Color: "#FF3CAC",
			},
		},
		APNS: &messaging.APNSConfig{
			Payload: &messaging.APNSPayload{
				Aps: &messaging.Aps{
					Sound: "default",
				},
			},
		},
	}

	_, err := s.client.Send(ctx, message)
	if err != nil {
		log.Printf("[FCM] Failed to send notification: %v", err)
		return err
	}

	log.Printf("[FCM] Sent notification to %s: %s - %s", token, title, body)
	return nil
}

// NotifyStatusChange sends a status change notification to a user.
func (s *NotificationService) NotifyStatusChange(ctx context.Context, token, eventName, newStatus string) error {
	title := fmt.Sprintf("Actualización de %s", eventName)
	body := fmt.Sprintf("Tu evento ahora está en estado: %s", newStatus)
	return s.SendPush(ctx, token, title, body)
}
