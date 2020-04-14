package org.mlops4j.model.validation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mlops4j.data.metadata.ComponentBuilder;
import org.mlops4j.api.ModelEvaluation;
import org.mlops4j.model.registry.Model;

import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BaselineValidationStrategy implements ValidationStrategy {
    private final ModelEvaluation evaluation;
    private final ComparisonStrategy strategy;

    @Override
    public ValidationStatus validate(Model reference) {
        ComparisonStatus comparisonStatus = reference.getEvaluations()
                .filter(e -> this.strategy.areComparable(e, this.evaluation))
                .map(e -> this.strategy.compare(e, this.evaluation))
                .reduce((a, b) -> {
                    if (a == ComparisonStatus.WORSE || b == ComparisonStatus.WORSE) {
                        return ComparisonStatus.WORSE;
                    }
                    if (a == ComparisonStatus.BETTER || b == ComparisonStatus.BETTER) {
                        return ComparisonStatus.BETTER;
                    }
                    return ComparisonStatus.EQUAL;
                })
                .orElse(ComparisonStatus.INCONCLUSIVE);

        if (comparisonStatus == ComparisonStatus.BETTER) {
            return ValidationStatus.ACCEPTED;
        }
        return ValidationStatus.REJECTED;
    }

    public static class Builder implements ComponentBuilder<BaselineValidationStrategy> {


        private ModelEvaluation evaluation;
        private ComparisonStrategy strategy;

        @Override
        public BaselineValidationStrategy build() {
            this.evaluation = Optional.ofNullable(this.evaluation).orElseThrow(() -> new NullPointerException("Model base evaluation not set"));
            this.strategy = Optional.ofNullable(this.strategy).orElseThrow(() -> new NullPointerException("Comparison strategy not set"));

            return new BaselineValidationStrategy(this.evaluation, this.strategy);
        }

        public Builder evaluation(ModelEvaluation evaluation) {
            this.evaluation = evaluation;
            return this;
        }

        public Builder comparisonStrategy(ComparisonStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        @Override
        public Builder fromParameters(Map<String, Object> parameters) {
            return this;
        }
    }
}
