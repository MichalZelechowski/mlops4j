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

package org.mlops4j.experiment.impl;

import org.mlops4j.evaluation.api.EvaluationResult;
import org.mlops4j.experiment.api.Experiment;
import org.mlops4j.experiment.api.ExperimentResult;
import org.mlops4j.model.api.Model;
import org.mlops4j.storage.api.ComponentBuilder;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.mlops4j.training.api.FitResult;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class SingleExperiment implements Experiment {
    private final Model model;

    public SingleExperiment(Model model) {
        this.model = model;
    }

    @Override
    public Metadata<Experiment> getMetadata() throws DurabilityException {
        return new Metadata<>(this).withParameter("model", model);
    }

    @Override
    public ComponentBuilder<Experiment> getBuilder() {
        return new Builder();
    }

    @Override
    public CompletableFuture<ExperimentResult> run() {
//        model.fit(dataSet)
        ExperimentResult result = new ExperimentResult(FitResult.success(1), EvaluationResult.success(Collections.EMPTY_LIST));
        return CompletableFuture.completedFuture(result);
    }
}
