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

package org.mlops4j.experiment;

import org.junit.jupiter.api.Test;
import org.mlops4j.api.ResultStatus;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.evaluation.api.EvaluationConfiguration;
import org.mlops4j.evaluation.api.ModelEvaluator;
import org.mlops4j.experiment.api.Experiment;
import org.mlops4j.experiment.api.ExperimentBuilder;
import org.mlops4j.experiment.api.ExperimentRepository;
import org.mlops4j.experiment.api.ExperimentResult;
import org.mlops4j.experiment.impl.ExperimentRepositoryBuilder;
import org.mlops4j.fixture.*;
import org.mlops4j.inference.api.Inference;
import org.mlops4j.model.api.Model;
import org.mlops4j.model.api.ModelConfiguration;
import org.mlops4j.model.impl.BaseModel;
import org.mlops4j.model.registry.api.ModelRegistry;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.mlops4j.training.api.Trainer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class ExperimentTests {

    @Test
    public void experimentIsRun() throws InterruptedException, ExecutionException, TimeoutException, DurabilityException {
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
        DataSet evalSet = new TestDataSet.Builder().build();

        ExperimentRepository experimentRepository = new ExperimentRepositoryBuilder().build();
        Experiment experiment = new ExperimentBuilder()
                .model(model)
                .trainDataSet(trainSet)
                .evalDataSet(evalSet)
                .repository(experimentRepository)
                .modelRegistry(registry)
                .build();

        CompletableFuture<ExperimentResult> resultFuture = experiment.run();

        ExperimentResult result = resultFuture.get(10, TimeUnit.SECONDS);
        assertThat(result.getStatus()).isEqualTo(ResultStatus.SUCCESS);

        assertThat(registry.get(model.getId())).isPresent();
        assertThat(registry.get(model.getId()).get().getEvaluations()).isNotEmpty();
    }
}
