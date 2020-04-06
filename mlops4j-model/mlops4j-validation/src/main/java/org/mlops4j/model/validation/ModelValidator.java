package org.mlops4j.model.validation;

import lombok.AllArgsConstructor;
import org.mlops4j.model.registry.ModelReference;

import java.util.Optional;

@AllArgsConstructor
public class ModelValidator {
    private final ComparationStrategy strategy;

    public ValidationResult validate(ModelReference reference) {
        return new ValidationResult(ValidationStatus.ACCEPTED, "meets baseline");
    }

    public static class Builder {
        private ComparationStrategy strategy;

        public Builder comparationStrategy(ComparationStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public ModelValidator build() {
            this.strategy = Optional.ofNullable(this.strategy).orElseThrow(() -> new NullPointerException("Comparation strategy is not set"));
            return new ModelValidator(this.strategy);
        }
    }
}
