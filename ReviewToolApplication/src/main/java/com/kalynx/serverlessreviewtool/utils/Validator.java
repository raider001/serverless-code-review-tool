package com.kalynx.serverlessreviewtool.utils;

/**
 * Functional interface for validating input values
 */
@FunctionalInterface
public interface Validator {

    /**
     * Validates the input value
     *
     * @param value The value to validate
     * @return ValidationResult containing validation status and optional error message
     */
    ValidationResult validate(String value);

    /**
     * Result of a validation operation
     */
    class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;

        private ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return isValid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

