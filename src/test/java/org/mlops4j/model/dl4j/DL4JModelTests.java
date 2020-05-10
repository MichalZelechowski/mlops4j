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

package org.mlops4j.model.dl4j;

import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.junit.jupiter.api.Test;
import org.mlops4j.api.ResultStatus;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.dataset.api.DataSetId;
import org.mlops4j.dataset.impl.dl4j.DL4JDataSet;
import org.mlops4j.evaluation.api.EvaluationResult;
import org.mlops4j.evaluation.impl.dl4j.DL4JEvaluationConfiguration;
import org.mlops4j.evaluation.impl.dl4j.DL4JModelEvaluator;
import org.mlops4j.inference.api.Input;
import org.mlops4j.inference.api.Output;
import org.mlops4j.inference.impl.dl4j.DL4JInference;
import org.mlops4j.inference.impl.dl4j.DL4JInput;
import org.mlops4j.model.api.Model;
import org.mlops4j.model.impl.BaseModel;
import org.mlops4j.model.impl.dl4j.DL4JModelConfiguration;
import org.mlops4j.model.registry.api.ModelRegistry;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.mlops4j.training.api.FitResult;
import org.mlops4j.training.impl.dl4j.DL4JTrainer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class DL4JModelTests {

    @Test
    public void modelTrainEvaluateAndPredict() throws ExecutionException, InterruptedException, DurabilityException, TimeoutException {
        ModelRegistry registry = new ModelRegistry.Builder().build();
        DL4JModelConfiguration modelConfiguration = new DL4JModelConfiguration.Builder()
                .configuration(new NeuralNetConfiguration.Builder()
                        .updater(new Adam())
                        .l2(1e-4)
                        .list()
                        .layer(new DenseLayer.Builder()
                                .nIn(2 * 2) // Number of input datapoints.
                                .nOut(1000) // Number of output datapoints.
                                .activation(Activation.RELU) // Activation function.
                                .weightInit(WeightInit.XAVIER) // Weight initialization.
                                .build())
                        .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                                .nIn(1000)
                                .nOut(2)
                                .activation(Activation.SOFTMAX)
                                .weightInit(WeightInit.XAVIER)
                                .build())
                        .build())
                .build();

        DL4JTrainer trainer = new DL4JTrainer.Builder().epochs(1).build();
        DL4JEvaluationConfiguration evaluationConfiguration = new DL4JEvaluationConfiguration.Builder()
                .modelConfiguration(modelConfiguration)
                .build();
        DL4JModelEvaluator evaluator = new DL4JModelEvaluator.Builder()
                .neuralNetwork(DL4JModelEvaluator.EvaluationType.BASE)
                .build();
        DL4JInference inference = new DL4JInference.Builder().single().build();

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

        DataSet trainSet = DL4JDataSet.from(new DataSetId("test", "1", "20200105"),
                new org.nd4j.linalg.dataset.DataSet(
                        Nd4j.create(new float[]{1.0f, 0.0f, 0.0f, 1.0f}, 1, 4),
                        Nd4j.create(new float[]{1.0f, 0.0f}, 1, 2)
                ));

        Future<FitResult> trainFuture = model.fit(trainSet);

        FitResult fitResult = trainFuture.get(10, TimeUnit.SECONDS);
        assertThat(fitResult.getStatus())
                .describedAs(fitResult.getMessage().toString()+ ' ' + fitResult.getException())
                .isEqualTo(ResultStatus.SUCCESS);

        DataSet evalSet = DL4JDataSet.from(new DataSetId("eval", "1", "20200105"),
                new org.nd4j.linalg.dataset.DataSet(
                        Nd4j.create(new float[]{1.0f, 0.0f, 0.0f, 1.0f}, 1, 4),
                        Nd4j.create(new float[]{1.0f, 0.0f}, 1, 2))
        );

        Future<EvaluationResult> evaluationFuture = model.evaluate(evalSet);
        EvaluationResult evaluationResult = evaluationFuture.get(10, TimeUnit.SECONDS);
        assertThat(evaluationResult.getStatus()).describedAs(evaluationResult.getMessage().toString() + evaluationResult.getException().toString()).isEqualTo(ResultStatus.SUCCESS);

        Model storedModel = registry.get(model.getId()).orElseThrow(() -> new AssertionError("Cannot find model"));

        assertThat(storedModel).isEqualTo(model);
        assertThat(storedModel.getParent()).isNotPresent();

        Input input = DL4JInput.from(new float[]{1.0f, 0.0f, 0.1f, 1.0f});
        Future<Output<Integer>> outputFuture = storedModel.infer(input);

        Output<Integer> output = outputFuture.get();
        assertThat(output.getValue()).isBetween(0, 1);

        storedModel = registry.get(model.getId()).orElseThrow(() -> new AssertionError("Cannot find model"));
        assertThat(storedModel.getEvaluations()).hasSize(1);
        assertThat(storedModel.getEvaluations().findFirst()).isPresent();
        assertThat(storedModel.getEvaluations().findFirst().get().getDataSetId()).isEqualTo(evalSet.getId());
    }
}
