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

import com.google.common.collect.Iterators;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.mlops4j.api.ResultStatus;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.evaluation.api.*;
import org.mlops4j.fixture.*;
import org.mlops4j.inference.api.Inference;
import org.mlops4j.inference.api.Input;
import org.mlops4j.inference.api.Output;
import org.mlops4j.model.api.Model;
import org.mlops4j.model.api.ModelConfiguration;
import org.mlops4j.model.api.ModelId;
import org.mlops4j.model.impl.BaseModel;
import org.mlops4j.model.registry.api.ModelRegistry;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.mlops4j.training.api.FitResult;
import org.mlops4j.training.api.Trainer;

import java.util.Arrays;
import java.util.Comparator;
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

        Model model = getModel(registry);

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

    private Model getModel(ModelRegistry registry) {
        ModelConfiguration modelConfiguration = new TestModelConfiguration.Builder().build();
        ModelEvaluator evaluator = new TestModelEvaluator.Builder().build();
        Inference inference = new TestInference.Builder().build();
        Trainer trainer = new TestTrainer.Builder().build();
        EvaluationConfiguration evaluationConfiguration = new TestEvaluationConfiguration.Builder().build();

        return new BaseModel.Builder()
                .configuration(modelConfiguration)
                .evaluationConfiguration(evaluationConfiguration)
                .evaluator(evaluator)
                .inference(inference)
                .trainer(trainer)
                .modelRegistry(registry)
                .name("testModel")
                .version("1.0")
                .build();
    }

    @Test
    public void modelTrainingReiterate() throws ExecutionException, InterruptedException, DurabilityException {
        ModelRegistry registry = new ModelRegistry.Builder().build();

        Model model = getModel(registry);

        DataSet trainSet = new TestDataSet.Builder().build();

        Future<FitResult> trainFuture = model.fit(trainSet).thenCompose(ft -> model.fit(trainSet));

        assertThat(trainFuture).isDone();
        FitResult fitResult = trainFuture.get();
        assertThat(fitResult.getStatus()).describedAs(fitResult.getMessage().toString() + fitResult.getException().toString()).isEqualTo(ResultStatus.SUCCESS);

        ModelId[] modelIds = Iterators.toArray(registry.list(), ModelId.class);
        assertThat(modelIds).hasSize(2);
        Arrays.sort(modelIds, Comparator.comparing(ModelId::getIteration));

        assertThat(modelIds[0].getDataSetId().equals(trainSet.getId()));
        assertThat(modelIds[1].getDataSetId().equals(trainSet.getId()));
        assertThat(modelIds[0].getIteration().equals(10));
        assertThat(modelIds[1].getIteration().equals(20));
    }

}
