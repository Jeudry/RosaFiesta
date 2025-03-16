package configModels

import "time"

type TokenConfig struct {
	Secret string
	Aud    string
	Iss    string
	Exp    time.Duration
}
