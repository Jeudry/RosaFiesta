package config

import "time"

type TokenConfig struct {
	Secret string
	Aud    string
	Iss    string
	Exp    time.Duration
}
