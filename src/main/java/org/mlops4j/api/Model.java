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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public interface Model extends Trainable, Evaluable, Inferable, Durable<Model> {

    ModelId getId();

    Optional<Model> getParent();

    Stream<Evaluation> getEvaluations();

    class Builder implements ComponentBuilder<Model> {
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
        private Collection<Evaluation> evaluations = Lists.newLinkedList();

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

        public Builder evaluations(Collection<Evaluation> evaluations) {
            this.evaluations.clear();
            this.evaluations.addAll(evaluations);
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
                    this.registry, this.evaluations);
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
