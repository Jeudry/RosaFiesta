package configModels

import "Backend/internal/ratelimiter"

type Config struct {
	Cors        CorsConfig
	Addr        string
	Db          DbConfig
	Env         string
	ApiURL      string
	FrontendURL string
	Mail        MailConfig
	Auth        AuthConfig
	Redis       RedisConfig
	RateLimiter ratelimiter.Config
}
