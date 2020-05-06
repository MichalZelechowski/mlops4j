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

package org.mlops4j.api;

import com.google.common.collect.Iterables;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class BaseModel implements Model {
    private final ModelConfiguration configuration;
    private final EvaluationConfiguration evaluationConfiguration;
    private final ModelEvaluator evaluator;
    private final Inference inference;
    private final Trainer trainer;
    private ModelId id;
    private ModelId parent = null;
    private final ModelRegistry modelRegistry;
    private final Collection<Evaluation> evaluations;

    @Override
    public CompletableFuture<FitResult> fit(DataSet trainSet) {
        CompletableFuture<FitResult> fitResult = this.trainer.fit(this, trainSet);
        return Optional.ofNullable(this.modelRegistry).map(
                mr -> fitResult.thenApply(
                        fr -> {
                            this.updateModelId(trainSet.getId(), fr.getIterationsDone());
                            try {
                                this.modelRegistry.put(this);
                            } catch (DurabilityException e) {
                                //TODO add logging
                                return FitResult.failure(fr.getIterationsDone(), e, "Cannot put model in repository");
                            }
                            return fr;
                        })
        ).orElse(fitResult);
    }

    private void updateModelId(DataSetId dataSetId, Integer iterationsDone) {
        int iterations;
        if (dataSetId.equals(this.id.getDataSetId())) {
            iterations = Optional.ofNullable(this.id.getIteration()).orElse(0) + iterationsDone;
        } else {
            iterations = iterationsDone;
        }
        ModelId newModelId = this.id.withDataSetId(dataSetId).withIteration(iterations);

        this.parent = this.id;
        this.id = newModelId;
    }

    @Override
    public Representation getModelRepresentation() {
        return this.configuration.getModelRepresentation();
    }

    @Override
    public CompletableFuture<EvaluationResult> evaluate(DataSet evalSet) {
        return this.evaluator.evaluate(this, evalSet).thenApply(result -> {
            Iterables.addAll(this.evaluations, result.getEvaluations());
            // TODO store model again!
            return result;
        });
    }

    @Override
    public Representation getEvaluationRepresentation() {
        return this.evaluationConfiguration.getEvaluationRepresentation();
    }

    @Override
    public ModelId getId() {
        return this.id;
    }

    @Override
    public Optional<Model> getParent() {
        return Optional.empty();
    }

    @Override
    public <VALUE> CompletableFuture<Output<VALUE>> infer(Input input) {
        return this.inference.infer(this, input);
    }

    @Override
    public Stream<Evaluation> getEvaluations() {
        return this.evaluations.stream();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseModel baseModel = (BaseModel) o;
        return Objects.equals(id, baseModel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public Metadata<Model> getMetadata() throws DurabilityException {
        Metadata<Model> metadata = new Metadata<>(this);
        metadata.withParameter("configuration", this.configuration);
        metadata.withParameter("evaluationConfiguration", this.evaluationConfiguration);
        metadata.withParameter("evaluator", this.evaluator);
        metadata.withParameter("inference", this.inference);
        metadata.withParameter("trainer", this.trainer);
        metadata.withParameter("id", new String(this.id.asBytes()));
        if (this.parent != null) {
            metadata.withParameter("parent", new String(this.parent.asBytes()));
        }
        metadata.withParameter("modelRegistry", this.modelRegistry);
        metadata.withParameter("evaluations", this.evaluations);
        return metadata;
    }

    @Override
    public ComponentBuilder getBuilder() {
        return new Model.Builder();
    }
}
