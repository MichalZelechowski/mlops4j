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

package org.mlops4j.model.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mlops4j.api.Representation;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.dataset.api.DataSetId;
import org.mlops4j.evaluation.api.Evaluation;
import org.mlops4j.evaluation.api.EvaluationConfiguration;
import org.mlops4j.evaluation.api.EvaluationResult;
import org.mlops4j.evaluation.api.ModelEvaluator;
import org.mlops4j.inference.api.Inference;
import org.mlops4j.inference.api.Input;
import org.mlops4j.inference.api.Output;
import org.mlops4j.model.api.Model;
import org.mlops4j.model.api.ModelConfiguration;
import org.mlops4j.model.api.ModelId;
import org.mlops4j.model.registry.api.ModelRegistry;
import org.mlops4j.api.ComponentBuilder;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.mlops4j.training.api.FitResult;
import org.mlops4j.training.api.Trainer;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class BaseModel implements Model {
    private final ModelConfiguration configuration;
    private final EvaluationConfiguration evaluationConfiguration;
    private final ModelEvaluator evaluator;
    private final Inference inference;
    private final Trainer trainer;
    private ModelId id;
    private ModelId parent;
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
            try {
                this.modelRegistry.put(this);
            } catch (DurabilityException e) {
                //TODO add logging
                return EvaluationResult.failure(this.evaluations, e, "Cannot put model in repository");
            }
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
    public ComponentBuilder<? super Model> getBuilder() {
        return new Builder();
    }

    public static class Builder implements ComponentBuilder<Model> {
        private ModelConfiguration configuration;
        private ModelEvaluator evaluator;
        private Inference inference;
        private Trainer trainer;
        private String name;
        private String version;
        private EvaluationConfiguration evaluationConfiguration;
        private ModelRegistry registry;
        private ModelId parent;
        private ModelId id;
        private final Collection<Evaluation> validables = Lists.newLinkedList();

        public Builder configuration(ModelConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder evaluator(ModelEvaluator evaluator) {
            this.evaluator = evaluator;
            return this;
        }

        public Builder inference(Inference inference) {
            this.inference = inference;
            return this;
        }

        public Builder trainer(Trainer trainer) {
            this.trainer = trainer;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder parent(ModelId parent) {
            this.parent = parent;
            return this;
        }

        public Builder parent(String parent) {
            this.parent = toId(parent);
            return this;
        }

        public Builder id(String id) {
            this.id = toId(id);
            return this;
        }

        private ModelId toId(String idString) {
            ModelId modelId = new ModelId();
            modelId.fromBytes(idString.getBytes());
            return modelId;
        }

        public Builder evaluations(Collection<Evaluation> validables) {
            this.validables.clear();
            this.validables.addAll(validables);
            return this;
        }

        public Model build() {
            this.isSetOrThrow(this.configuration, "configuration");
            this.isSetOrThrow(this.evaluator, "evaluator");
            this.isSetOrThrow(this.inference, "inference");
            this.isSetOrThrow(this.trainer, "trainer");
            this.isSetOrThrow(this.evaluationConfiguration, "evaluationConfiguration");

            ModelId modelId;
            if (this.name == null || this.version == null) {
                this.isSetOrThrow(this.id, "id");
                modelId = this.id;
            } else {
                modelId = new ModelId(name, version);
            }

            return new BaseModel(configuration, evaluationConfiguration, evaluator, inference, trainer, modelId, parent,
                    this.registry, this.validables);
        }

        private void isSetOrThrow(Object property, String name) {
            Preconditions.checkNotNull(property, "%s not set", name);
        }

        public Builder evaluationConfiguration(EvaluationConfiguration evaluationConfiguration) {
            this.evaluationConfiguration = evaluationConfiguration;
            return this;
        }

        public Builder modelRegistry(ModelRegistry registry) {
            this.registry = registry;
            return this;
        }
    }
}
