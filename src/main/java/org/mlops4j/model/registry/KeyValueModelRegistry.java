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

package org.mlops4j.model.registry;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.mlops4j.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor
public class KeyValueModelRegistry implements ModelRegistry {

    private final KeyValueStorage storage;

    @Override
    public Optional<Model> get(ModelId id) throws DurabilityException {
        // TODO check if lock is present
        Optional<byte[]> modelContent = storage.get(new String(id.asBytes()));
        if (modelContent.isPresent()) {
            byte[] modelBinary = modelContent.get();
            Metadata<Model> modelMetadata = new Metadata<>();
            modelMetadata.fromBytes(modelBinary);
            //TODO move it to the same class that performs writing to temporary storage
            //TODO how about going in parallel
            for (String hash : modelMetadata.getHashes()) {
                Path targetPath = Paths.get(FileUtils.getTempDirectoryPath(), hash);
                //TODO think about streaming
                byte[] content = storage.get(hash).orElseThrow(() -> new DurabilityException(String.format("Missing file with hash %s", hash)));
                try {
                    FileUtils.writeByteArrayToFile(targetPath.toFile(), content);
                } catch (IOException e) {
                    throw new DurabilityException(String.format("Cannot write %s to temporary tile", hash), e);
                }
            }
            return Optional.of(modelMetadata.getDurable());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void put(Model model) throws DurabilityException {
        //TODO add lock
        final Metadata<Model> metadata = model.getMetadata();
        this.storage.put(new String(model.getId().asBytes()), metadata.asBytes());
        //TODO move it to the same class that performs writing to temporary storage
        //TODO how about going in parallel
        for (String hash : metadata.getHashes()) {
            // TODO think about collision
            Path path = Paths.get(FileUtils.getTempDirectoryPath(), hash);
            try {
                // TODO add store from file
                storage.put(hash, Files.readAllBytes(path));
            } catch (IOException e) {
                throw new DurabilityException(String.format("Cannot read from file %s", path));
            }
        }
    }

    @Override
    public Metadata<ModelRegistry> getMetadata() throws DurabilityException {
        return new Metadata(this)
                .withParameter("storage", this.storage);
    }

    @Override
    public ComponentBuilder<ModelRegistry> getBuilder() {
        return new Builder();
    }

    public static class Builder extends ModelRegistry.Builder {
        private KeyValueStorage storage;

        @Override
        public ModelRegistry build() {
            KeyValueStorage storage = Optional.ofNullable(this.storage).orElse(new InMemoryKeyValueStorage.Builder().build());
            return new KeyValueModelRegistry(storage);
        }

        public Builder storage(KeyValueStorage storage) {
            this.storage = storage;
            return this;
        }

    }
}
