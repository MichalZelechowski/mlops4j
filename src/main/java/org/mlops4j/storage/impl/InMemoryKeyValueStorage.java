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

import com.google.common.collect.Maps;
import org.apache.commons.text.RandomStringGenerator;
import org.mlops4j.storage.api.ComponentBuilder;
import org.mlops4j.storage.api.KeyValueStorage;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public class InMemoryKeyValueStorage implements KeyValueStorage {
    private final ConcurrentMap<String, byte[]> content;
    private final String name;

    private InMemoryKeyValueStorage(String name, ConcurrentMap<String, byte[]> content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public void put(String key, byte[] value) {
        this.content.put(key, value);
    }

    @Override
    public Optional<byte[]> get(String key) {
        return Optional.ofNullable(this.content.get(key));
    }

    @Override
    public Metadata<KeyValueStorage> getMetadata() throws DurabilityException {
        return new Metadata<KeyValueStorage>(this).withParameter("name", this.name);
    }

    @Override
    public ComponentBuilder<KeyValueStorage> getBuilder() {
        return new Builder();
    }

    public static class Builder implements ComponentBuilder<KeyValueStorage> {

        private final static ConcurrentMap<String, ConcurrentMap<String, byte[]>> MAPS = Maps.newConcurrentMap();
        private String name;

        @Override
        public InMemoryKeyValueStorage build() {
            String name = Optional.ofNullable(this.name)
                    .orElse(new RandomStringGenerator.Builder().withinRange('a', 'z').build().generate(40));
            MAPS.computeIfAbsent(name, n -> Maps.newConcurrentMap());
            return new InMemoryKeyValueStorage(name, MAPS.get(name));
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

    }
}
