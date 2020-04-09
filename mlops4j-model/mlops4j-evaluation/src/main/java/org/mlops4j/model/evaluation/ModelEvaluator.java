/*
 *  Copyright 2020 Michał Żelechowski <MichalZelechowski@github.com>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.mlops4j.model.evaluation;

import lombok.AllArgsConstructor;
import org.mlops4j.api.ModelEvaluation;
import org.mlops4j.data.preparation.DataReference;
import org.mlops4j.model.registry.ModelReference;
import org.mlops4j.model.registry.ModelRegistry;

import java.util.Optional;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor
public class ModelEvaluator {
    private final EvaluationStrategy strategy;
    private final ModelRegistry registry;

    public ModelEvaluation evaluate(ModelReference model, DataReference dataReference) {
        ModelEvaluation evaluation = this.strategy.evaluate(model, dataReference);
        model.addEvaluation(evaluation);
        this.registry.putModel(model);

        return evaluation;
    }

    public static class Builder {

        private EvaluationStrategy strategy;
        private ModelRegistry registry;

        public Builder evaluationStrategy(EvaluationStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder registry(ModelRegistry registry) {
            this.registry = registry;
            return this;
        }

        public ModelEvaluator build() {
            this.strategy = Optional.ofNullable(this.strategy).orElseThrow(() -> new NullPointerException("Evaluation strategy not set"));
            this.registry = Optional.ofNullable(this.registry).orElseThrow(() -> new NullPointerException("Model registry not set"));
            return new ModelEvaluator(this.strategy, this.registry);
        }
    }
}
