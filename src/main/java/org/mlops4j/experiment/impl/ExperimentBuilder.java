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

import com.google.common.base.Preconditions;
import org.mlops4j.api.ComponentBuilder;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.experiment.api.Experiment;
import org.mlops4j.experiment.api.ExperimentRepository;
import org.mlops4j.model.api.Model;
import org.mlops4j.model.api.ModelId;
import org.mlops4j.model.registry.api.ModelRegistry;
import org.mlops4j.storage.api.exception.DurabilityException;

import java.time.Instant;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public class ExperimentBuilder implements ComponentBuilder<Experiment> {

    private Model model;
    private DataSet evalDataSet;
    private DataSet trainDataSet;
    private ExperimentRepository experimentRepository;
    private ModelRegistry modelRegistry;
    private ModelId modelId;
    private Instant startTime;
    private Instant endTime;

    @Override
    public Experiment build() {
        Preconditions.checkNotNull(evalDataSet, "Eval dataset has to be setup");
        Preconditions.checkNotNull(trainDataSet, "Train datset has to be setup");
        Preconditions.checkNotNull(modelRegistry, "Model registry has to be setup");
        Preconditions.checkNotNull(experimentRepository, "Experiment repository has to be setup");

        if (model == null) {
            Preconditions.checkNotNull(modelId, "If model is not given, at least modelId is required");
            try {
                model = this.modelRegistry.get(modelId).orElseThrow();
            } catch (DurabilityException e) {
                throw new IllegalArgumentException(String.format("Model %s could not be read from registry", modelId));
            }
        }
        //TODO what about other experiments?
        SingleExperiment singleExperiment = new SingleExperiment(model, trainDataSet, evalDataSet, experimentRepository, modelRegistry);
        singleExperiment.setStartTime(this.startTime);
        singleExperiment.setEndTime(this.endTime);
        return singleExperiment;
    }

    public ExperimentBuilder model(Model model) {
        this.model = model;
        return this;
    }

    public ExperimentBuilder modelId(String modelId) {
        ModelId id = new ModelId();
        id.fromBytes(modelId.getBytes());

        this.modelId = id;
        return this;
    }

    public ExperimentBuilder evalDataSet(DataSet dataSet) {
        this.evalDataSet = dataSet;
        return this;
    }

    public ExperimentBuilder trainDataSet(DataSet dataSet) {
        this.trainDataSet = dataSet;
        return this;
    }

    public ExperimentBuilder repository(ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
        return this;
    }

    public ExperimentBuilder modelRegistry(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
        return this;
    }
}
