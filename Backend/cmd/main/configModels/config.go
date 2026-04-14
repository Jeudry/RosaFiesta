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
	R2          R2Config
	Firebase    FirebaseConfig
	WhatsApp    WhatsAppConfig
}

type R2Config struct {
	AccountID string
	AccessKey string
	SecretKey string
	Bucket    string
}

type FirebaseConfig struct {
	Enabled bool
}

type WhatsAppConfig struct {
	PhoneNumberID string
	AccessToken   string
	FromName     string
}
