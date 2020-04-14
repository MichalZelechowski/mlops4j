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

import org.junit.jupiter.api.Test;
import org.mlops4j.api.Inference;
import org.mlops4j.api.ModelEvaluation;
import org.mlops4j.data.metadata.ComponentBuilder;
import org.mlops4j.data.preparation.DataSet;
import org.mlops4j.model.evaluation.EvaluationStrategy;
import org.mlops4j.model.evaluation.ModelEvaluator;
import org.mlops4j.model.registry.*;
import org.mlops4j.storage.InMemoryKeyValueStorage;
import org.mlops4j.storage.JavaDataSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class EvaluationTests {

    @Test
    public void evaluateModel() {
        Model reference = new Model.Builder()
                .name("testModel")
                .version("1.0")
                .converter(new INDArrayDataConverter())
                .inference(new SqrInference())
                .build();

        ModelRegistry registry = new ModelRegistry.Builder()
                .storage(new InMemoryKeyValueStorage())
                .serializer(new JavaDataSerializer())
                .build();

        EvaluationStrategy strategy = (model, dataReference) -> new ConstantModelEvaluation(dataReference.getName());
        ModelEvaluator evaluator = new ModelEvaluator.Builder()
                .registry(registry)
                .evaluationStrategy(strategy)
                .build();

        DataSet data = () -> "testSet";
        ModelEvaluation evaluation = evaluator.evaluate(reference, data);
        assertThat(evaluation).isInstanceOf(ConstantModelEvaluation.class);
        assertThat(((ConstantModelEvaluation) evaluation).name).isEqualTo("testSet");
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
            public ComponentBuilder fromParameters(Map<String, Object> parameters) {
                return this;
            }

        }

    }

    public static class ConstantModelEvaluation implements ModelEvaluation {

        public final String name;

        public ConstantModelEvaluation(String name) {
            this.name = name;
        }
    }
}
