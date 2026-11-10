//go:build dev

package app

import (
	"context"

	"github.com/Jeudry/rosafiesta/server/internal/devtools"
	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

func devModules(onReset func(ctx context.Context)) []httpapi.Module {
	return []httpapi.Module{devtools.Module{OnReset: onReset}}
}
