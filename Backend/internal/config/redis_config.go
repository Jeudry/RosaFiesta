package config

type RedisConfig struct {
	Addr    string
	Pw      string
	Db      int
	Enabled bool
}
