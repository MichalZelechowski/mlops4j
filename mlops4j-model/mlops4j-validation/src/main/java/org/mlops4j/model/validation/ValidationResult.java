package org.mlops4j.model.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidationResult {
    private final ValidationStatus status;
    private final String message;
}
