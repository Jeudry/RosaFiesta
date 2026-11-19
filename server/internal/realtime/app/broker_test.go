package app_test

import (
	"context"
	"encoding/json"
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/realtime/app"
	"github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

func TestBrokerFiltersByRoleClientAndBacklog(t *testing.T) {
	broker := app.NewBroker(time.Now)
	broker.Publish(domain.RealtimeEvent{
		Type:     domain.EventQuoteCreated,
		EntityID: "q-1",
		ClientID: "client-a",
		Audience: domain.AudienceClient,
		Summary:  "Cotización creada",
	})
	broker.Publish(domain.RealtimeEvent{
		Type:     domain.EventPaymentPending,
		EntityID: "pay-1",
		ClientID: "client-b",
		Audience: domain.AudienceAdmin,
		Summary:  "Solo admin",
	})

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()
	ch, unsub := broker.Subscribe(ctx, app.SubscriberFilter{
		PrincipalID:   "client-a",
		PrincipalRole: "client",
	}, 0)
	defer unsub()

	select {
	case evt := <-ch:
		if evt.EntityID != "q-1" {
			t.Fatalf("expected q-1 for client-a, got %s", evt.EntityID)
		}
	case <-time.After(time.Second):
		t.Fatal("timed out waiting for backlog event")
	}

	select {
	case unexpected := <-ch:
		t.Fatalf("client-a should not receive admin-only event %+v", unexpected)
	case <-time.After(40 * time.Millisecond):
	}
}

func TestBrokerDeliverRemoteEnvelopeSkipsSelfAndDeliversPeers(t *testing.T) {
	broker := app.NewBroker(time.Now)
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()
	ch, unsub := broker.Subscribe(ctx, app.SubscriberFilter{PrincipalRole: "admin"}, 0)
	defer unsub()

	selfPayload, _ := json.Marshal(map[string]any{
		"instanceId": broker.InstanceID(),
		"event": domain.RealtimeEvent{
			Type:     domain.EventQuoteCreated,
			EntityID: "self-1",
			Audience: domain.AudienceAdmin,
		},
	})
	if broker.DeliverRemoteEnvelope(string(selfPayload)) {
		t.Fatal("expected broker to ignore envelope from same instanceId")
	}

	peerPayload, _ := json.Marshal(map[string]any{
		"instanceId": "peer-node-2",
		"event": domain.RealtimeEvent{
			Type:     domain.EventPaymentPending,
			EntityID: "peer-pay-99",
			Audience: domain.AudienceAdmin,
		},
	})
	if !broker.DeliverRemoteEnvelope(string(peerPayload)) {
		t.Fatal("expected broker to deliver remote envelope from peer instance")
	}
	select {
	case evt := <-ch:
		if evt.EntityID != "peer-pay-99" {
			t.Fatalf("expected peer-pay-99, got %s", evt.EntityID)
		}
	case <-time.After(time.Second):
		t.Fatal("timed out waiting for peer remote event")
	}
}
