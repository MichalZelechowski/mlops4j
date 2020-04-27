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
package org.mlops4j.model.registry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mlops4j.api.DataSerializer;
import org.mlops4j.api.KeyValueStorage;

import java.util.Optional;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelRegistry {

    private static final String MODEL = "model";
    private static final String TRAINED = "trained";
    private static final String METADATA = "metadata";
    private static final String BINARY = "binary";

    private final KeyValueStorage storage;
    private final DataSerializer serializer;

    public Optional<Model> getModel(String name, String version, String dataSet, String partition, String iteration) {
        Optional<byte[]> metadata = storage.get(MODEL, TRAINED, name, version, dataSet, partition, iteration, METADATA);
        if (metadata.isEmpty()) {
            return Optional.empty();
        }

        Optional<byte[]> binary = storage.get(MODEL, TRAINED, name, version, dataSet, partition, iteration, BINARY);
        if (binary.isEmpty()) {
            return Optional.empty();
        }

        ModelMetadata modelMetadata = (ModelMetadata) serializer.construct(metadata.get());
        return Optional.of(new Model.Builder()
                .name(name)
                .version(version)
                .dataSet(dataSet)
                .partition(partition)
                .cycles(iteration)
                .converter(modelMetadata.getConverter())
                .inference(modelMetadata.getInference(binary.get()))
                .build());
    }

    public void putModel(Model reference) {
        byte[] metadata = serializer.hydrolyze(reference.getMetadata());
        storage.put(metadata, MODEL, TRAINED, reference.getName(), reference.getVersion(),
                reference.getDataSet(), reference.getPartition(), reference.getCycles(), METADATA);

        byte[] binary = reference.getInference().getModelBinary();
        storage.put(binary, MODEL, TRAINED, reference.getName(), reference.getVersion(),
                reference.getDataSet(), reference.getPartition(), reference.getCycles(), BINARY);
    }

    public static class Builder {

        private KeyValueStorage storage;
        private DataSerializer<?> serializer;

        public Builder storage(KeyValueStorage storage) {
            this.storage = storage;
            return this;
        }

        public Builder serializer(DataSerializer<?> serializer) {
            this.serializer = serializer;
            return this;
        }

        public ModelRegistry build() {
            this.storage = Optional.ofNullable(this.storage)
                    .orElseThrow(() -> new NullPointerException("Storage not set for model registry"));
            this.serializer = Optional.ofNullable(this.serializer)
                    .orElseThrow(() -> new NullPointerException("Serializer not set for model registry"));
            return new ModelRegistry(this.storage, this.serializer);
        }
    }

}
