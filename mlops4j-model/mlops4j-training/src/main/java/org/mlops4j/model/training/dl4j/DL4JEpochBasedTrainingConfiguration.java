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

package org.mlops4j.model.training.dl4j;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mlops4j.data.metadata.ComponentBuilder;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DL4JEpochBasedTrainingConfiguration extends DL4JTrainingConfiguration<DL4JEpochBasedTrainer> {

    private final Integer epochs;

    @Override
    public DL4JEpochBasedTrainer getTrainer() {
        return new DL4JEpochBasedTrainer(this.epochs);
    }

    @Override
    public Integer getCyclesNumber() {
        return this.epochs;
    }

    public static class Builder implements ComponentBuilder<DL4JEpochBasedTrainingConfiguration> {

        private Integer epochs = 1;

        @Override
        public DL4JEpochBasedTrainingConfiguration build() {
            return new DL4JEpochBasedTrainingConfiguration(epochs);
        }

        public Builder epochs(int epochs) {
            this.epochs = epochs;
            return this;
        }

        @Override
        public Builder fromParameters(Map<String, Serializable> parameters) {
            this.epochs((Integer) parameters.getOrDefault("epochs", this.epochs));
            return this;
        }
    }
}
