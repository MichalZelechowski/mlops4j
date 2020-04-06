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

import java.util.Optional;

/**
 *
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

    public Optional<ModelReference> getModel(String name, String version) {
        Optional<byte[]> metadata = storage.get(MODEL, TRAINED, name, version, METADATA);
        if (metadata.isEmpty()) {
            return Optional.empty();
        }

        Optional<byte[]> binary = storage.get(MODEL, TRAINED, name, version, BINARY);
        if (binary.isEmpty()) {
            return Optional.empty();
        }

        ModelReferenceMetadata modelMetadata = (ModelReferenceMetadata) serializer.construct(metadata.get());
        return Optional.of(new ModelReference.Builder()
                .name(name)
                .version(version)
                .converter(modelMetadata.getConverter())
                .inference(modelMetadata.getInference(binary.get()))
                .build());
    }

    public void addModel(ModelReference reference) {
        byte[] metadata = serializer.hydrolize(reference.getMetadata());
        storage.put(metadata, MODEL, TRAINED, reference.getName(), reference.getVersion(), METADATA);
        
        byte[] binary = reference.getInference().getModelBinary();
        storage.put(binary, MODEL, TRAINED, reference.getName(), reference.getVersion(), BINARY);
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
