package org.mlops4j.model.validation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mlops4j.model.registry.Model;

import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelValidator {
    private final ValidationStrategy strategy;

    public ValidationResult validate(Model reference) {
        ValidationStatus status = this.strategy.validate(reference);
        String message = (status == ValidationStatus.ACCEPTED) ?
                String.format("%s/%s meets baseline", reference.getName(), reference.getVersion()) :
                String.format("%s/%s does not meet baseline", reference.getName(), reference.getVersion());
        return new ValidationResult(status, message);
    }

    public static class Builder {
        private ValidationStrategy strategy;

        public Builder validationStrategy(ValidationStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public ModelValidator build() {
            this.strategy = Optional.ofNullable(this.strategy).orElseThrow(() -> new NullPointerException("Validation strategy is not set"));
            return new ModelValidator(this.strategy);
        }
    }
}
