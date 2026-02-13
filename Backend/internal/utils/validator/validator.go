package validator

import (
	"strings"

	"Backend/internal/utils/apperrors"

	"github.com/go-playground/validator/v10"
)

var validate *validator.Validate

func init() {
	validate = validator.New(validator.WithRequiredStructEnabled())
}

// ValidateStruct validates a struct and returns an AppError if validation fails
func ValidateStruct(s interface{}) error {
	err := validate.Struct(s)
	if err != nil {
		// Verify if it's a validation error
		if _, ok := err.(*validator.InvalidValidationError); ok {
			return apperrors.NewInternal("invalid validation error: " + err.Error())
		}

		// Simple error formatting for now
		// In a real app, we might want to map fields to specific error messages
		var sb strings.Builder
		for _, err := range err.(validator.ValidationErrors) {
			sb.WriteString(err.Field() + " is " + err.Tag() + "; ")
		}
		return apperrors.NewValidation(strings.TrimSuffix(sb.String(), "; "))
	}
	return nil
}
