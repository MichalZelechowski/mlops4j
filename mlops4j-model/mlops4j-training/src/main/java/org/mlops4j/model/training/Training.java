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

package org.mlops4j.model.training;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mlops4j.api.Inference;
import org.mlops4j.data.preparation.DataReference;
import org.mlops4j.model.registry.ModelReference;
import org.mlops4j.model.registry.ModelRegistry;

import java.util.Optional;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Training {
    private final TrainingConfiguration configuration;
    private final DataReference dataReference;
    private final ModelRegistry registry;

    public void perform(String name, String version) {
        Model model = this.createModel();
        model.fit(dataReference);

        this.registerModel(name, version, model);
    }

    private Model createModel() {
        return new Model(){
            @Override
            public void fit(DataReference dataReference) {

            }

            @Override
            public Inference getInference() {
                return new MockInference();
            }
        };
    }

    private void registerModel(String name, String version, Model model) {
        ModelReference reference = new ModelReference.Builder()
                .name(name)
                .version(version)
                .inference(model.getInference())
                .build();

        this.registry.putModel(reference);
    }

    public static class Builder {
        private TrainingConfiguration configuration;
        private DataReference dataReference;
        private ModelRegistry registry;

        public Builder configuration(TrainingConfiguration trainingConfiguration) {
            this.configuration = trainingConfiguration;
            return this;
        }

        public Builder dataReferences(DataReference dataReference) {
            this.dataReference = dataReference;
            return this;
        }

        public Builder registry(ModelRegistry registry) {
            this.registry = registry;
            return this;
        }

        public Training build() {
            this.configuration = Optional.ofNullable(this.configuration).orElseThrow(() -> new NullPointerException("Training configuration not set"));
            this.dataReference = Optional.ofNullable(this.dataReference).orElseThrow(() -> new NullPointerException("Data reference not set"));
            this.registry = Optional.ofNullable(this.registry).orElseThrow(() -> new NullPointerException("Model registry not set"));
            return new Training(configuration, dataReference, registry);
        }
    }
}
