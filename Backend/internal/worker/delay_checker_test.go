package worker

import (
	"context"
	"testing"

	"Backend/internal/notifications"
	"Backend/internal/store"
	"Backend/internal/store/mocks"
	"Backend/internal/store/models"

	"github.com/google/uuid"
	"github.com/stretchr/testify/mock"
	"go.uber.org/zap"
)

func TestDelayChecker_checkDelayedItems(t *testing.T) {
	logger := zap.NewNop().Sugar()
	notificationService := notifications.NewNotificationService()

	t.Run("should notify owner and organizers for delayed items", func(t *testing.T) {
		mockTimeline := &mocks.TimelineStore{}
		mockUsers := &mocks.UserStore{}

		checker := &DelayChecker{
			store:         store.Storage{Timeline: mockTimeline, Users: mockUsers},
			logger:        logger,
			notifications: notificationService,
		}
		eventID := uuid.New()
		ownerToken := "owner-token"
		orgToken1 := "org-token-1"
		orgToken2 := "org-token-2"

		delayedItems := []models.TimelineItemWithUser{
			{
				TimelineItem: models.TimelineItem{
					ID:         uuid.New(),
					EventID:    eventID,
					Title:      "Banquete",
					IsCritical: true,
				},
				UserFCMToken: ownerToken,
			},
		}

		organizerTokens := []string{orgToken1, orgToken2, ownerToken} // owner is also an org

		mockTimeline.On("GetOverdueCriticalItems", mock.Anything).Return(delayedItems, nil)
		mockUsers.On("GetOrganizersFCMTokens", mock.Anything).Return(organizerTokens, nil)

		// We use checkDelayedItems directly instead of Start to avoid loops
		checker.checkDelayedItems(context.Background())

		mockTimeline.AssertExpectations(t)
		mockUsers.AssertExpectations(t)
	})

	t.Run("should handle no delayed items", func(t *testing.T) {
		mockTimeline := &mocks.TimelineStore{}
		checker := &DelayChecker{
			store:         store.Storage{Timeline: mockTimeline},
			logger:        logger,
			notifications: notificationService,
		}

		mockTimeline.On("GetOverdueCriticalItems", mock.Anything).Return([]models.TimelineItemWithUser{}, nil)

		checker.checkDelayedItems(context.Background())

		mockTimeline.AssertExpectations(t)
	})
}
