/*
 * Copyright 2020 Michał Żelechowski <MichalZelechowski@github.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mlops4j.model.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mlops4j.data.metadata.ComponentBuilder;
import org.mlops4j.model.evaluation.ModelEvaluation;
import org.mlops4j.model.registry.INDArrayDataConverter;
import org.mlops4j.model.registry.Inference;
import org.mlops4j.model.registry.ModelReference;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.List;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public class ValidationTests {

    @Test
    public void validateModelAgainstBaseline() {
        ModelEvaluation evaluation = new ModelEvaluation() {
        };

        ModelReference reference = new ModelReference.Builder()
                .name("testModel")
                .version("1.0")
                .converter(new INDArrayDataConverter())
                .inference(new SqrInference())
                .evaluations(evaluation)
                .build();

        ComparisonStrategy comparisonStrategy = new ComparisonStrategy() {
            @Override
            public boolean areComparable(ModelEvaluation a, ModelEvaluation b) {
                return true;
            }

            @Override
            public ComparisonStatus compare(ModelEvaluation a, ModelEvaluation b) {
                return ComparisonStatus.BETTER;
            }
        };

        ValidationStrategy strategy = new BaselineValidationStrategy.Builder()
                .evaluation(evaluation)
                .comparisonStrategy(comparisonStrategy)
                .build();

        ModelValidator validator = new ModelValidator.Builder()
                .validationStrategy(strategy)
                .build();

        ValidationResult result = validator.validate(reference);

        Assertions.assertThat(result.getStatus()).isEqualTo(ValidationStatus.ACCEPTED);
        Assertions.assertThat(result.getMessage()).contains("meets baseline");
    }

    public static class SqrInference implements Inference {

        @Override
        public INDArray output(INDArray input) {
            return Transforms.pow(input, 2.0, true);
        }

        @Override
        public byte[] getModelBinary() {
            return new byte[1];
        }

        public static class Builder implements Inference.Builder {

            @Override
            public Inference build() {
                return new SqrInference();
            }

            @Override
            public Builder model(byte[] bytes) {
                return this;
            }

            @Override
            public ComponentBuilder fromParameters(List parameters) {
                return this;
            }

        }

    }
}
