package common

import (
	"fmt"
	"regexp"
	"strconv"
	"strings"
)

// Convert color input to hexadecimal
func convertToHex(color string) (uint32, error) {
	// Check if the color is already in hexadecimal format
	if matched, _ := regexp.MatchString(`^0x[0-9A-Fa-f]{6,8}$`, color); matched {
		hexValue, err := strconv.ParseUint(color[2:], 16, 32)
		if err != nil {
			return 0, err
		}
		return uint32(hexValue), nil
	}

	// Check if the color is in RGB format
	if matched, _ := regexp.MatchString(`^rgb\(\d{1,3},\d{1,3},\d{1,3}\)$`, color); matched {
		color = strings.TrimPrefix(color, "rgb(")
		color = strings.TrimSuffix(color, ")")
		rgbValues := strings.Split(color, ",")
		if len(rgbValues) != 3 {
			return 0, fmt.Errorf("invalid RGB format")
		}
		r, err := strconv.Atoi(strings.TrimSpace(rgbValues[0]))
		if err != nil {
			return 0, err
		}
		g, err := strconv.Atoi(strings.TrimSpace(rgbValues[1]))
		if err != nil {
			return 0, err
		}
		b, err := strconv.Atoi(strings.TrimSpace(rgbValues[2]))
		if err != nil {
			return 0, err
		}
		return uint32((r << 16) | (g << 8) | b), nil
	}

	// Check if the color is in HTML color code format
	if matched, _ := regexp.MatchString(`^#[0-9A-Fa-f]{6}$`, color); matched {
		hexValue, err := strconv.ParseUint(color[1:], 16, 32)
		if err != nil {
			return 0, err
		}
		return uint32(hexValue), nil
	}

	return 0, fmt.Errorf("invalid color format")
}
