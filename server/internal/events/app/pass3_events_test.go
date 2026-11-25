package app_test

import (
	"testing"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/events/app"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

func TestEventsSetupPhotosContactPhoneAndSketchPreservation(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	svc := app.NewService()
	caller := platformhttp.Principal{
		ID:       uuid.New(),
		Email:    "cliente@rosafiesta.do",
		FullName: "Laura Cliente",
		Role:     "client",
	}

	created, err := svc.CreateEventForCaller(ctx, app.CreateEventCommand{
		Title:          "Boda Jardín",
		DateLabel:      "Sáb 12 dic",
		Venue:          "Jardín Botánico",
		Guests:         90,
		SketchJSON:     `{"elements":[{"id":"mesa-1","x":120}]}`,
		SetupPhotoUrls: []string{"/v1/media/med-1", " "},
		ContactPhone:   "809-555-0199",
	}, caller)
	if err != nil {
		t.Fatalf("CreateEventForCaller failed: %v", err)
	}
	if len(created.SetupPhotoUrls) != 1 {
		t.Fatalf("expected 1 sanitized setup photo url, got %v", created.SetupPhotoUrls)
	}
	if created.ContactPhone != "809-555-0199" {
		t.Fatalf("expected ContactPhone persisted, got %q", created.ContactPhone)
	}

	// Updating title without sketchJson or with empty string must preserve existing client sketch.
	emptySketch := "  "
	newTitle := "Boda Jardín Actualizada"
	nextPhotos := []string{"/v1/media/med-1", "/v1/media/med-2"}
	updated, updErr := svc.UpdateEventForCaller(ctx, created.ID, app.UpdateEventCommand{
		Title:          &newTitle,
		SketchJSON:     &emptySketch,
		SetupPhotoUrls: &nextPhotos,
	}, caller)
	if updErr != nil {
		t.Fatalf("UpdateEventForCaller failed: %v", updErr)
	}
	if updated.SketchJSON != `{"elements":[{"id":"mesa-1","x":120}]}` {
		t.Fatalf("expected existing SketchJSON preserved, got %q", updated.SketchJSON)
	}
	if len(updated.SetupPhotoUrls) != 2 {
		t.Fatalf("expected 2 setup photo urls after update, got %v", updated.SetupPhotoUrls)
	}
}
