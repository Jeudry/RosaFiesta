package config

type AuthConfig struct {
	Basic  AuthBasicConfig
	Token  TokenConfig
	ApiKey ApiKeyConfig
}
