/*
 * Copyright 2020 Michał Żelechowski <MichalZelechowski@github.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mlops4j.model.serving;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.datavec.api.records.Record;
import org.mlops4j.model.registry.ModelReference;
import org.mlops4j.model.registry.ModelRegistry;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 *
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@Getter
@EqualsAndHashCode
public class TrainedModel {

    private final ModelReference reference;

    private TrainedModel(ModelReference reference) {
        this.reference = reference;
    }

    public INDArray output(INDArray input) {
        return this.reference.getInference().output(input);
    }

    public INDArray prepareData(Record record) {
        return this.reference.getConverter().map(record);
    }

    public Record prepareData(INDArray array) {
        return this.reference.getConverter().map(array);
    }

    public static class Builder implements Cloneable {

        private String name;
        private String version;
        private ModelRegistry registry;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder modelRegistry(ModelRegistry registry) {
            this.registry = registry;
            return this;
        }

        public TrainedModel build() {
            if (this.name == null) {
                throw new IllegalArgumentException("Name of the model not provided");
            }
            if (this.version == null) {
                throw new IllegalArgumentException("Version of the model not provided");
            }
            if (this.registry == null) {
                throw new IllegalArgumentException("Model registry not set");
            }
            ModelReference model = this.registry.getModel(this.name, this.version)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot find model of [%s/%s]", this.name, this.version)));
            return new TrainedModel(model);
        }
    }

}
