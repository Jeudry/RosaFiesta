// Package clock abstrae la hora para que el dominio y los servicios nunca lean el reloj del sistema directamente.
package clock

import "time"

// Clock entrega la hora actual. Las pruebas usan [Fixed]; el servidor usa [System].
type Clock interface {
	// Now devuelve el instante actual en UTC.
	Now() time.Time
}

// System es el reloj real.
type System struct{}

// Now devuelve la hora real en UTC.
func (System) Now() time.Time { return time.Now().UTC() }

// Fixed es un reloj controlado para pruebas.
type Fixed struct{ At time.Time }

// Now devuelve el instante fijo.
func (f *Fixed) Now() time.Time { return f.At }

// Advance mueve el reloj d hacia adelante.
func (f *Fixed) Advance(d time.Duration) { f.At = f.At.Add(d) }
