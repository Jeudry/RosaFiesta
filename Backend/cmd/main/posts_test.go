package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"testing"

	"Backend/cmd/main/configModels"
	"Backend/cmd/main/view_models/posts"
	authMocks "Backend/internal/auth/mocks"
	storeMocks "Backend/internal/store/mocks"
	"Backend/internal/store/models"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"github.com/stretchr/testify/mock"
)

func TestCreatePost(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		payload      posts.CreatePostPayload
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should create post successfully",
			payload: posts.CreatePostPayload{
				Title:   "My First Post",
				Content: "This is the content of my post",
				Tags:    []string{"intro", "hello"},
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				postsM := app.Store.Posts.(*storeMocks.PostsStore)
				postsM.On("Create", mock.Anything, mock.Anything).Return(nil).Once()
			},
			expectedCode: http.StatusCreated,
		},
		{
			name: "should return 400 for missing title",
			payload: posts.CreatePostPayload{
				Title:   "", // Missing required title
				Content: "Some content",
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()
			},
			expectedCode: http.StatusBadRequest,
		},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			app := newTestApplication(t, configModels.Config{})
			if tc.setupMocks != nil {
				tc.setupMocks(app)
			}

			mux := app.Mount()
			body, _ := json.Marshal(tc.payload)
			req, _ := http.NewRequest(http.MethodPost, "/v1/posts", bytes.NewBuffer(body))
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestGetPost(t *testing.T) {
	postID := uuid.New()
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		postID      string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:    "should return post by ID",
			postID: postID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()

				postsM := app.Store.Posts.(*storeMocks.PostsStore)
				postsM.On("RetrieveById", mock.Anything, postID).Return(&models.Post{
					ID:      postID,
					UserID:  userID,
					Title:   "Test Post",
					Content: "Test content",
				}, nil).Once()

				commentsM := app.Store.Comments.(*storeMocks.CommentsStore)
				commentsM.On("RetrieveCommentsByPostId", mock.Anything, postID).Return([]models.Comment{}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			app := newTestApplication(t, configModels.Config{})
			if tc.setupMocks != nil {
				tc.setupMocks(app)
			}

			mux := app.Mount()
			req, _ := http.NewRequest(http.MethodGet, "/v1/posts/"+tc.postID, nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestGetUserFeed(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should return user feed",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				postsM := app.Store.Posts.(*storeMocks.PostsStore)
				postsM.On("GetUserFeed", mock.Anything, userID, mock.Anything).Return([]models.PostWithMetadata{
					{Post: models.Post{ID: uuid.New(), Title: "Post 1"}},
					{Post: models.Post{ID: uuid.New(), Title: "Post 2"}},
				}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name: "should return empty feed",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				postsM := app.Store.Posts.(*storeMocks.PostsStore)
				postsM.On("GetUserFeed", mock.Anything, userID, mock.Anything).Return([]models.PostWithMetadata{}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			app := newTestApplication(t, configModels.Config{})
			if tc.setupMocks != nil {
				tc.setupMocks(app)
			}

			mux := app.Mount()
			req, _ := http.NewRequest(http.MethodGet, "/v1/users/feed", nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}