//go:build !dev

package app

import "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"

func devModules(_ func()) []httpapi.Module {
	return nil
}
