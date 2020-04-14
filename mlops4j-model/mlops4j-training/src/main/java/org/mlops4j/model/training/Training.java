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
import org.mlops4j.data.preparation.DataSet;
import org.mlops4j.model.registry.Model;
import org.mlops4j.model.registry.ModelConfiguration;
import org.mlops4j.model.registry.ModelRegistry;

import java.util.Optional;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Training {
    private final TrainingConfiguration trainingConfiguration;
    private final DataSet dataReference;
    private final ModelRegistry registry;
    private final ModelConfiguration modelConfiguration;

    public TrainingResult run(String name, String version) {
        Trainer trainer = this.trainingConfiguration.getTrainer();
        Object modelImplementation = this.modelConfiguration.getModelImplementation();
        TrainingResult result = trainer.fit(modelImplementation, this.dataReference);

        this.registerModel(name, version, this.dataReference.getName(), this.dataReference.getPartition(),
                result.getCyclesNumber(), result.getModel());
        return result;
    }

    private void registerModel(String name, String version, String dataSetName, String partition,
                               Integer cyclesNumber, org.mlops4j.model.training.Model model) {
        Model reference = new Model.Builder()
                .name(name)
                .version(version)
                .dataSet(dataSetName)
                .partition(partition)
                .cycles(cyclesNumber.toString())
                .inference(model.getInference())
                .build();

        this.registry.putModel(reference);
    }

    public static class Builder {
        private TrainingConfiguration trainingConfiguration;
        private DataSet dataReference;
        private ModelRegistry registry;
        private ModelConfiguration modelConfiguration;

        public Builder trainingConfiguration(TrainingConfiguration trainingConfiguration) {
            this.trainingConfiguration = trainingConfiguration;
            return this;
        }

        public Builder trainingDataSet(DataSet dataReference) {
            this.dataReference = dataReference;
            return this;
        }

        public Builder modelRegistry(ModelRegistry registry) {
            this.registry = registry;
            return this;
        }

        public Training build() {
            this.trainingConfiguration = Optional.ofNullable(this.trainingConfiguration).orElseThrow(() -> new NullPointerException("Training configuration not set"));
            this.dataReference = Optional.ofNullable(this.dataReference).orElseThrow(() -> new NullPointerException("Data reference not set"));
            this.registry = Optional.ofNullable(this.registry).orElseThrow(() -> new NullPointerException("Model registry not set"));
            this.modelConfiguration = Optional.ofNullable(this.modelConfiguration).orElseThrow(() -> new NullPointerException("Model configuration not set"));
            return new Training(trainingConfiguration, dataReference, registry, modelConfiguration);
        }

        public Builder modelConfiguration(ModelConfiguration modelConfiguration) {
            this.modelConfiguration = modelConfiguration;
            return this;
        }
    }
}
