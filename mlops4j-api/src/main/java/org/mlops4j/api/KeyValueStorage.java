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

package org.mlops4j.api;

import java.util.Arrays;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public abstract class KeyValueStorage {

    public abstract Optional<byte[]> get(String... key);

    public abstract void put(byte[] bytes, String... key);

    protected void validateKey(String... key) {
        if (key.length == 0) {
            throw new IllegalArgumentException("At least one key element is required");
        }
        if (Stream.of(key).anyMatch(part -> part.contains(this.getSeparator()))) {
            throw new IllegalArgumentException(String.format("Key parts %s contain separator [%s]", Arrays.toString(key), this.getSeparator()));
        }
        if (Stream.of(key).anyMatch(part -> part.contains(".."))) {
            throw new IllegalArgumentException(String.format("Key parts %s contain [..]", Arrays.toString(key)));
        }
    }

    protected String constructKey(String... key) {
        this.validateKey(key);

        StringJoiner joiner = new StringJoiner(this.getSeparator());
        for (String part : key) {
            joiner.add(part);
        }
        return joiner.toString();
    }

    protected String getSeparator() {
        return "/";
    }
}