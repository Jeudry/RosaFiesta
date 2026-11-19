// Package app implementa el broker en memoria para eventos en tiempo real (SSE).
package app

import (
	"context"
	"strings"
	"sync"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

const (
	defaultRecentLimit   = 200
	defaultSubscriberBuf = 64
)

// SubscriberFilter acota el flujo recibido por una conexión SSE.
type SubscriberFilter struct {
	PrincipalID   string
	PrincipalRole string
	TopicPrefix   string
	EventID       string
}

type subscriber struct {
	filter SubscriberFilter
	ch     chan domain.RealtimeEvent
}

// Broker gestiona suscripciones concurrentes y un historial acotado de eventos recientes.
type Broker struct {
	mu          sync.RWMutex
	instanceID  string
	pgPool      *pgxpool.Pool
	nextSeq     int64
	nextSubID   uint64
	recent      []domain.RealtimeEvent
	subscribers map[uint64]*subscriber
	clock       func() time.Time
}

// NewBroker inicializa un Broker en memoria listo para publicar y retransmitir eventos.
func NewBroker(clock func() time.Time) *Broker {
	if clock == nil {
		clock = time.Now
	}
	return &Broker{
		instanceID:  newBrokerInstanceID(),
		recent:      make([]domain.RealtimeEvent, 0, defaultRecentLimit),
		subscribers: make(map[uint64]*subscriber),
		clock:       clock,
	}
}

// Publish asigna un número de secuencia monotónico, guarda el evento en caché y lo difunde.
func (b *Broker) Publish(event domain.RealtimeEvent) domain.RealtimeEvent {
	published := b.deliverEventLocally(event)
	b.mu.RLock()
	pool := b.pgPool
	b.mu.RUnlock()
	if pool != nil {
		b.notifyPostgres(pool, published)
	}
	return published
}

// Subscribe registra un oyente nuevo y reenvía los eventos posteriores a sinceSeq.
func (b *Broker) Subscribe(ctx context.Context, filter SubscriberFilter, sinceSeq int64) (<-chan domain.RealtimeEvent, func()) {
	sub := &subscriber{
		filter: filter,
		ch:     make(chan domain.RealtimeEvent, defaultSubscriberBuf),
	}

	b.mu.Lock()
	b.nextSubID++
	subID := b.nextSubID
	backlog := b.backlogAfterLocked(filter, sinceSeq)
	for _, missed := range backlog {
		select {
		case sub.ch <- missed:
		default:
		}
	}
	b.subscribers[subID] = sub
	b.mu.Unlock()

	cancel := func() {
		b.unsubscribe(subID)
	}
	go func() {
		<-ctx.Done()
		b.unsubscribe(subID)
	}()
	return sub.ch, cancel
}

func (b *Broker) unsubscribe(subID uint64) {
	b.mu.Lock()
	sub, exists := b.subscribers[subID]
	if exists {
		delete(b.subscribers, subID)
		close(sub.ch)
	}
	b.mu.Unlock()
}

func (b *Broker) appendRecentLocked(event domain.RealtimeEvent) {
	if len(b.recent) >= defaultRecentLimit {
		copy(b.recent, b.recent[1:])
		b.recent[len(b.recent)-1] = event
		return
	}
	b.recent = append(b.recent, event)
}

func (b *Broker) matchingSubscribersLocked(event domain.RealtimeEvent) []*subscriber {
	matched := make([]*subscriber, 0, len(b.subscribers))
	for _, sub := range b.subscribers {
		if matchesFilter(event, sub.filter) {
			matched = append(matched, sub)
		}
	}
	return matched
}

func (b *Broker) backlogAfterLocked(filter SubscriberFilter, sinceSeq int64) []domain.RealtimeEvent {
	if sinceSeq < 0 {
		sinceSeq = 0
	}
	items := make([]domain.RealtimeEvent, 0, len(b.recent))
	for _, event := range b.recent {
		if event.Seq <= sinceSeq {
			continue
		}
		if matchesFilter(event, filter) {
			items = append(items, event)
		}
	}
	return items
}

func matchesFilter(event domain.RealtimeEvent, filter SubscriberFilter) bool {
	if !event.CanReceive(filter.PrincipalRole, filter.PrincipalID) {
		return false
	}
	topicPrefix := strings.TrimSpace(filter.TopicPrefix)
	if topicPrefix != "" {
		hasPrefix := strings.HasPrefix(string(event.Type), topicPrefix)
		if !hasPrefix {
			return false
		}
	}
	eventID := strings.TrimSpace(filter.EventID)
	if eventID != "" {
		matchesEntity := event.EntityID == eventID
		if !matchesEntity {
			return false
		}
	}
	return true
}
