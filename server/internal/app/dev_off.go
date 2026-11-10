//go:build !dev

package app

import (
	"context"

	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

func devModules(_ func(ctx context.Context)) []httpapi.Module {
	return nil
}
