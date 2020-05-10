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

package org.mlops4j.storage.impl;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.mlops4j.storage.api.ComponentBuilder;
import org.mlops4j.storage.api.KeyValueStorage;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

@AllArgsConstructor
public class FileSystemKeyValueStorage implements KeyValueStorage {

    private final Path root;

    @Override
    public void put(String key, byte[] value) throws DurabilityException {
        Path targetPath = root;
        try {
            targetPath = targetPath.resolve(key);
            FileUtils.writeByteArrayToFile(targetPath.toFile(), value);
        } catch (IOException e) {
            throw new DurabilityException(String.format("Cannot write file to %s at key", targetPath, key));
        }
    }

    @Override
    public Optional<byte[]> get(String key) throws DurabilityException {
        if (Files.exists(this.root.resolve(key))) {
            try {
                return Optional.of(FileUtils.readFileToByteArray(this.root.resolve(key).toFile()));
            } catch (IOException e) {
                throw new DurabilityException(String.format("Cannot read file from file %s", this.root.resolve(key)), e);
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Metadata<KeyValueStorage> getMetadata() throws DurabilityException {
        return new Metadata<>(this).withParameter("root", this.root.toFile().getAbsolutePath());
    }

    @Override
    public ComponentBuilder<KeyValueStorage> getBuilder() {
        return new Builder();
    }

    public static class Builder implements ComponentBuilder<KeyValueStorage> {

        private Path root;

        @Override
        public FileSystemKeyValueStorage build() {
            Preconditions.checkNotNull(root, "Root path must be set");
            return new FileSystemKeyValueStorage(this.root);
        }

        public Builder root(String root) {
            this.root = Path.of(root);
            return this;
        }

        public Builder root(File root) {
            this.root = Path.of(root.toURI());
            return this;
        }

        public Builder root(Path root) {
            this.root = root;
            return this;
        }

    }
}
