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

import org.mlops4j.api.ComponentBuilder;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.evaluation.api.EvaluationResult;
import org.mlops4j.experiment.api.*;
import org.mlops4j.model.api.Model;
import org.mlops4j.model.registry.api.ModelRegistry;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class SingleExperiment implements Experiment {
    private static final Logger LOG = LoggerFactory.getLogger(SingleExperiment.class);

    private Model model;
    private final DataSet<?> trainDataSet;
    private final DataSet<?> evalDataSet;
    private final ExperimentRepository experimentRepository;
    private final ModelRegistry modelRegistry;
    private Instant startTime;
    private Instant endTime;

    public SingleExperiment(Model model, DataSet<?> trainDataSet, DataSet<?> evalDataSet, ExperimentRepository experimentRepository, ModelRegistry modelRegistry) {
        this.model = model;
        this.trainDataSet = trainDataSet;
        this.evalDataSet = evalDataSet;
        this.experimentRepository = experimentRepository;
        this.modelRegistry = modelRegistry;
    }

    @Override
    public Metadata<Experiment> getMetadata() throws DurabilityException {
        Metadata<Experiment> metadata = new Metadata<>(this)
                .withParameter("modelId", new String(model.getId().asBytes()))
                .withParameter("trainDataSet", trainDataSet)
                .withParameter("evalDataSet", evalDataSet)
                .withParameter("repository", experimentRepository)
                .withParameter("modelRegistry", modelRegistry);
        if (startTime != null) {
            metadata = metadata.withParameter("startTime", startTime);
        }
        if (endTime != null) {
            metadata = metadata.withParameter("endTime", startTime);
        }
        return metadata;
    }

    @Override
    public ComponentBuilder<? super Experiment> getBuilder() {
        return new ExperimentBuilder();
    }

    @Override
    public CompletableFuture<ExperimentResult> run() {
        try {
            start();
        } catch (DurabilityException e) {
            return CompletableFuture.completedFuture(new ExperimentResult(e));
        }

        return model.fit(this.trainDataSet).thenCompose(fitResult -> {
            if (fitResult.isSuccessful()) {
                CompletableFuture<EvaluationResult> evaluation = model.evaluate(evalDataSet);
                return evaluation.thenApply(evalResult -> new ExperimentResult(fitResult, evalResult));
            } else {
                return CompletableFuture.completedFuture(new ExperimentResult(fitResult, null));
            }
        }).thenApply(result -> {
            try {
                this.stop();
                return result;
            } catch (DurabilityException e) {
                return new ExperimentResult(result, "Failure when stopping experiment", e);
            }
        });
    }

    @Override
    public ExperimentId getId() {
        return new ExperimentId(model.getId(), trainDataSet.getId(), evalDataSet.getId());
    }

    private void start() throws DurabilityException {
        LOG.info("Starting experiment with model {}, train set {} and eval set {}",
                this.model.getId(),
                this.trainDataSet.getId(),
                this.evalDataSet.getId());
        this.startTime = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        this.experimentRepository.put(this);
    }

    private void stop() throws DurabilityException {
        LOG.info("Stopping experiment with model {}, train set {} and eval set {}",
                this.model.getId(),
                this.trainDataSet.getId(),
                this.evalDataSet.getId());
        this.experimentRepository.put(this);
        this.endTime = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        LOG.info("Duration: {}", this.getDuration());
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public boolean isFinished() {
        return this.endTime != null;
    }

    public Optional<Duration> getDuration() {
        if (this.isFinished()) {
            return Optional.of(Duration.between(this.startTime, this.endTime));
        } else {
            return Optional.empty();
        }
    }
}
