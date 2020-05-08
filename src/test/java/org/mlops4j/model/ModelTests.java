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

package org.mlops4j.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.mlops4j.api.*;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.dataset.api.DataSetId;
import org.mlops4j.evaluation.api.*;
import org.mlops4j.inference.api.Inferable;
import org.mlops4j.inference.api.Inference;
import org.mlops4j.inference.api.Input;
import org.mlops4j.inference.api.Output;
import org.mlops4j.model.api.Model;
import org.mlops4j.model.api.ModelConfiguration;
import org.mlops4j.model.impl.BaseModel;
import org.mlops4j.model.registry.api.ModelRegistry;
import org.mlops4j.storage.api.ComponentBuilder;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.mlops4j.training.api.FitResult;
import org.mlops4j.training.api.Trainable;
import org.mlops4j.training.api.Trainer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class ModelTests {

    @Test
    public void modelTrainEvaluateAndPredict() throws ExecutionException, InterruptedException, DurabilityException {
        ModelRegistry registry = new ModelRegistry.Builder().build();
        ModelConfiguration modelConfiguration = new TestModelConfiguration.Builder().build();
        ModelEvaluator evaluator = new TestModelEvaluator.Builder().build();
        Inference inference = new TestInference.Builder().build();
        Trainer trainer = new TestTrainer.Builder().build();
        EvaluationConfiguration evaluationConfiguration = new TestEvaluationConfiguration.Builder().build();

        Model model = new BaseModel.Builder()
                .configuration(modelConfiguration)
                .evaluationConfiguration(evaluationConfiguration)
                .evaluator(evaluator)
                .inference(inference)
                .trainer(trainer)
                .modelRegistry(registry)
                .name("testModel")
                .version("1.0")
                .build();

        DataSet trainSet = new TestDataSet.Builder().build();

        Future<FitResult> trainFuture = model.fit(trainSet);

        assertThat(trainFuture).isDone();
        FitResult fitResult = trainFuture.get();
        assertThat(fitResult.getStatus()).describedAs(fitResult.getMessage().toString() + fitResult.getException().toString()).isEqualTo(ResultStatus.SUCCESS);

        DataSet evalSet = new TestDataSet.Builder().build();
        Future<EvaluationResult> evaluationFuture = model.evaluate(evalSet);
        assertThat(evaluationFuture).isDone();
        EvaluationResult evaluationResult = evaluationFuture.get();
        assertThat(evaluationResult.getStatus()).describedAs(evaluationResult.getMessage().toString() + evaluationResult.getException().toString()).isEqualTo(ResultStatus.SUCCESS);

        Model storedModel = registry.get(model.getId()).orElseThrow(() -> new AssertionError("Cannot find model"));

        assertThat(storedModel).isEqualTo(model);
        assertThat(storedModel.getParent()).isNotPresent();

        Input input = new TestDTO(2.0f);
        Future<Output<Float>> outputFuture = storedModel.infer(input);

        assertThat(outputFuture).isDone();
        Output<Float> output = outputFuture.get();
        assertThat(output.getValue()).isEqualTo(4.0f + (float) Math.pow(0.5f, 10), Offset.<Float>offset(0.001f));

        storedModel = registry.get(model.getId()).orElseThrow(() -> new AssertionError("Cannot find model"));
        assertThat(storedModel.getEvaluations()).hasSize(1);
        Evaluation testEvaluation = new TestEvaluation(9.0f);
        assertThat(storedModel.getEvaluations().findFirst()).isPresent();
        assertThat(storedModel.getEvaluations().findFirst().get()).isEqualTo(testEvaluation);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TestModelConfiguration implements ModelConfiguration {

        private ThirdPartyModelRepresentation rep;

        @Override
        public Representation getModelRepresentation() {
            return Representation.of(rep);
        }

        @Override
        public Metadata getMetadata() throws DurabilityException {
            Metadata metadata = new Metadata(this)
                    .withParameter("error",
                            // TODO should accept byte array as binary
                            new ByteArrayInputStream(ByteBuffer.allocate(4).putFloat(rep.error).array())
                    );
            return metadata;
        }

        @Override
        public ComponentBuilder getBuilder() {
            return new Builder();
        }

        public static class Builder implements ComponentBuilder<ModelConfiguration> {

            private Float error = 1.0f;

            public Builder error(Float error) {
                this.error = error;
                return this;
            }

            public Builder error(InputStream error) throws IOException {
                this.error = ByteBuffer.wrap(error.readAllBytes()).getFloat();
                return this;
            }

            public ModelConfiguration build() {
                return new TestModelConfiguration(new ThirdPartyModelRepresentation(error));
            }
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TestModelEvaluator implements ModelEvaluator {
        @Override
        public CompletableFuture<EvaluationResult> evaluate(Evaluable evaluable, DataSet evalSet) {
            Representation<ThirdPartyEvaluation> representation = evaluable.getEvaluationRepresentation();
            ThirdPartyEvaluation thirdPartyEvaluation = representation.get();

            Representation<ThirdPartyDataSetRepresentation> dataSetRepresentation = evalSet.getRepresentation();
            ThirdPartyDataSetRepresentation dataSet = dataSetRepresentation.get();

            TestEvaluation testEvaluation = new TestEvaluation(thirdPartyEvaluation.evaluate(dataSet));
            EvaluationResult result = EvaluationResult.success(Collections.singletonList(testEvaluation));

            return CompletableFuture.completedFuture(result);
        }

        @Override
        public Metadata<ModelEvaluator> getMetadata() throws DurabilityException {
            Metadata metadata = new Metadata(this);
            return metadata;
        }

        @Override
        public ComponentBuilder<ModelEvaluator> getBuilder() {
            return new Builder();
        }

        public static class Builder implements ComponentBuilder<ModelEvaluator> {

            public ModelEvaluator build() {
                return new TestModelEvaluator();
            }
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TestInference implements Inference<TestDTO, TestDTO> {
        @Override
        public CompletableFuture<TestDTO> infer(Inferable inferable, TestDTO input) {
            Representation<ThirdPartyModelRepresentation> representation = inferable.getModelRepresentation();
            return CompletableFuture.completedFuture(new TestDTO(representation.get().infer(input.getValue())));
        }

        @Override
        public Metadata<Inference<TestDTO, TestDTO>> getMetadata() throws DurabilityException {
            return new Metadata<>(this);
        }

        @Override
        public ComponentBuilder<Inference<TestDTO, TestDTO>> getBuilder() {
            return new Builder();
        }

        public static class Builder implements ComponentBuilder<Inference<TestDTO, TestDTO>> {

            public Inference<TestDTO, TestDTO> build() {
                return new TestInference();
            }
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TestTrainer implements Trainer {

        @Override
        public CompletableFuture<FitResult> fit(Trainable trainable, DataSet trainSet) {
            Representation<ThirdPartyModelRepresentation> representation = trainable.getModelRepresentation();
            ThirdPartyModelRepresentation testRepresentation = representation.get();

            Representation<ThirdPartyDataSetRepresentation> dataSetRepresentation = trainSet.getRepresentation();
            ThirdPartyDataSetRepresentation dataSet = dataSetRepresentation.get();

            testRepresentation.fit(dataSet);

            return CompletableFuture.completedFuture(FitResult.success(1));
        }

        @Override
        public Metadata<Trainer> getMetadata() throws DurabilityException {
            return new Metadata<>(this);
        }

        @Override
        public ComponentBuilder<Trainer> getBuilder() {
            return new Builder();
        }

        public static class Builder implements ComponentBuilder<Trainer> {

            public Trainer build() {
                return new TestTrainer();
            }

        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TestDataSet implements DataSet {
        @Override
        public Representation getRepresentation() {
            return Representation.of(new ThirdPartyDataSetRepresentation(10));
        }

        @Override
        public DataSetId getId() {
            return new DataSetId("testSet", "v1", "20200427");
        }

        public static class Builder {

            public TestDataSet build() {
                return new TestDataSet();
            }
        }
    }

    public static class TestDTO implements Input<Float>, Output<Float> {
        private Float x;

        public TestDTO(Float x) {
            this.x = x;
        }

        public Float getValue() {
            return this.x;
        }
    }

    @ToString
    public static class TestEvaluation implements Evaluation {
        private final float value;

        public TestEvaluation(float value) {
            this.value = value;
        }

        @Override
        public Metric[] getMetrics() {
            return new Metric[]{new Metric("testMetric", new SingleMetricValue((double) this.value))};
        }

        @Override
        public Optional<Metric> getMetric(String name) {
            return Optional.of(name).filter(n -> "testMetric".equals(n)).map(x -> getMetrics()[0]);
        }

        @Override
        public int compareTo(Evaluation o) {
            return 0;
        }

        @Override
        public boolean equals(Object evaluation) {
            return this.value == ((TestEvaluation) evaluation).value;
        }

        @Override
        public Metadata<Evaluation> getMetadata() throws DurabilityException {
            return new Metadata<>(this).withParameter("value", value);
        }

        @Override
        public ComponentBuilder<Evaluation> getBuilder() {
            return new Builder();
        }

        public static final class Builder implements ComponentBuilder<Evaluation> {

            private Float value;

            public Builder value(Float value) {
                this.value = value;
                return this;
            }

            @Override
            public TestEvaluation build() {
                return new TestEvaluation(value);
            }
        }
    }

    @AllArgsConstructor
    public static class ThirdPartyModelRepresentation {

        private Float error = 1.0f;

        public void fit(ThirdPartyDataSetRepresentation dataSet) {
            do {
                error *= 0.5f;
            } while (dataSet.hasNext());
        }

        public Float infer(Float x) {
            return x * x + error;
        }
    }

    public static class TestEvaluationConfiguration implements EvaluationConfiguration {

        @Override
        public Representation getEvaluationRepresentation() {
            return Representation.of(new ThirdPartyEvaluation());
        }

        @Override
        public Metadata getMetadata() throws DurabilityException {
            return new Metadata(this);
        }

        @Override
        public ComponentBuilder getBuilder() {
            return new Builder();
        }

        public static class Builder implements ComponentBuilder<EvaluationConfiguration> {

            public EvaluationConfiguration build() {
                return new TestEvaluationConfiguration();
            }
        }
    }

    public static class ThirdPartyDataSetRepresentation {

        private int size;

        public ThirdPartyDataSetRepresentation(int size) {
            this.size = size;
        }

        public boolean hasNext() {
            size--;
            return size != 0;
        }
    }

    public static class ThirdPartyEvaluation {
        public ThirdPartyEvaluation() {
        }

        public float evaluate(ThirdPartyDataSetRepresentation dataSet) {
            int counter = 0;
            while (dataSet.hasNext()) {
                counter++;
            }
            return counter;
        }
    }

}
