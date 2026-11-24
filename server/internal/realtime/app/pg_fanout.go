package app

import (
	"context"
	"encoding/json"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

const (
	pgRealtimeChannel = "rosafiesta_events"
	pgNotifyTimeout   = 2 * time.Second
	pgReconnectDelay  = time.Second
)

type remoteEnvelope struct {
	InstanceID string               `json:"instanceId"`
	Event      domain.RealtimeEvent `json:"event"`
}

// InstanceID returns the unique node identifier attached to notifications emitted by this broker.
func (b *Broker) InstanceID() string {
	return b.instanceID
}

// StartPostgresFanout enables Postgres LISTEN/NOTIFY cross-instance SSE broadcasting until ctx ends.
func (b *Broker) StartPostgresFanout(ctx context.Context, pool *pgxpool.Pool) *Broker {
	if pool == nil {
		return b
	}
	b.mu.Lock()
	b.pgPool = pool
	b.mu.Unlock()
	if ctx == nil {
		return b
	}
	if ctx.Done() != nil {
		go b.listenPostgresLoop(ctx, pool)
	}
	return b
}

// DeliverRemoteEnvelope parses a Postgres notification payload and broadcasts events from peer instances.
func (b *Broker) DeliverRemoteEnvelope(rawPayload string) bool {
	if strings.TrimSpace(rawPayload) == "" {
		return false
	}
	var env remoteEnvelope
	if err := json.Unmarshal([]byte(rawPayload), &env); err != nil {
		return false
	}
	if env.InstanceID == b.instanceID {
		return false
	}
	b.deliverEventLocally(env.Event)
	return true
}

func (b *Broker) deliverEventLocally(event domain.RealtimeEvent) domain.RealtimeEvent {
	b.mu.Lock()
	b.nextSeq++
	event.Seq = b.nextSeq
	if event.ID == "" {
		event.ID = fmtEventID(event.Seq)
	}
	if event.OccurredAt.IsZero() {
		event.OccurredAt = b.clock().UTC()
	}
	if event.Audience == "" {
		event.Audience = domain.AudienceAll
	}
	b.appendRecentLocked(event)
	targets := b.matchingSubscribersLocked(event)
	for _, target := range targets {
		select {
		case target.ch <- event:
		default:
		}
	}
	b.mu.Unlock()
	return event
}

func (b *Broker) notifyPostgres(pool *pgxpool.Pool, event domain.RealtimeEvent) {
	if pool == nil {
		return
	}
	payloadBytes, err := json.Marshal(remoteEnvelope{
		InstanceID: b.instanceID,
		Event:      event,
	})
	if err != nil {
		return
	}
	notifyCtx, cancel := context.WithTimeout(context.Background(), pgNotifyTimeout)
	defer cancel()
	_, _ = pool.Exec(notifyCtx, "SELECT pg_notify($1, $2)", pgRealtimeChannel, string(payloadBytes))
}

func (b *Broker) listenPostgresLoop(ctx context.Context, pool *pgxpool.Pool) {
	for {
		if ctx.Err() != nil {
			return
		}
		b.consumePostgresNotifications(ctx, pool)
		select {
		case <-ctx.Done():
			return
		case <-time.After(pgReconnectDelay):
		}
	}
}

func (b *Broker) consumePostgresNotifications(ctx context.Context, pool *pgxpool.Pool) {
	connCfg := pool.Config().ConnConfig.Copy()
	conn, err := pgx.ConnectConfig(ctx, connCfg)
	if err != nil {
		return
	}
	defer func() {
		closeCtx, cancel := context.WithTimeout(context.Background(), pgNotifyTimeout)
		defer cancel()
		_ = conn.Close(closeCtx)
	}()
	if _, listenErr := conn.Exec(ctx, "LISTEN "+pgRealtimeChannel); listenErr != nil {
		return
	}
	for {
		notification, waitErr := conn.WaitForNotification(ctx)
		if waitErr != nil {
			return
		}
		if notification != nil {
			b.DeliverRemoteEnvelope(notification.Payload)
		}
	}
}

func newBrokerInstanceID() string {
	return "node-" + uuid.NewString()
}

func fmtEventID(seq int64) string {
	return "evt-" + strings.TrimSpace(uuidIntString(seq))
}

func uuidIntString(seq int64) string {
	return jsonNumberString(seq)
}

func jsonNumberString(seq int64) string {
	raw, err := json.Marshal(seq)
	if err != nil {
		return "0"
	}
	return string(raw)
}
