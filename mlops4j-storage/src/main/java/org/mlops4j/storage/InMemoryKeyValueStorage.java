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
package org.mlops4j.storage;

import org.mlops4j.api.KeyValueStorage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public class InMemoryKeyValueStorage extends KeyValueStorage {

    private final Map<String, byte[]> container;

    public InMemoryKeyValueStorage() {
        this.container = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<byte[]> get(String... key) {
        final String constructedKey = this.constructKey(key);
        return Optional.ofNullable(container.get(constructedKey));
    }

    @Override
    public void put(byte[] bytes, String... key) {
        final String constructedKey = this.constructKey(key);
        this.container.put(constructedKey, bytes);
    }

}
