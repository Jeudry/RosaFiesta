package main

import (
	"encoding/json"
	"fmt"
	"time"
)

type TimelineItem struct {
	ID          string    `json:"id"`
	EventID     string    `json:"event_id"`
	Title       string    `json:"title"`
	Description string    `json:"description"`
	StartTime   time.Time `json:"start_time"`
	EndTime     time.Time `json:"end_time"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
}

func main() {
	items := []TimelineItem{}

	envelope := map[string]interface{}{
		"data": items,
	}

	b, _ := json.Marshal(envelope)
	fmt.Println("Empty list response:", string(b))

	items = append(items, TimelineItem{
		ID:        "uuid-1",
		EventID:   "uuid-event",
		Title:     "Test",
		StartTime: time.Now(),
		EndTime:   time.Now().Add(time.Hour),
	})

	envelope["data"] = items
	b, _ = json.Marshal(envelope)
	fmt.Println("Single item response:", string(b))
}
