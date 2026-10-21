//go:build dev

package app

import (
	"github.com/Jeudry/rosafiesta/server/internal/devtools"
	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

func devModules(onReset func()) []httpapi.Module {
	return []httpapi.Module{devtools.Module{OnReset: onReset}}
}
