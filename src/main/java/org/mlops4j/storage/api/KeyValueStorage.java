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

package org.mlops4j.storage.api;

import org.mlops4j.storage.api.exception.DurabilityException;

import java.util.Iterator;
import java.util.Optional;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
//TODO consider streaming capability
public interface KeyValueStorage extends Durable<KeyValueStorage> {

    void put(String key, byte[] value) throws DurabilityException;

    Optional<byte[]> get(String key) throws DurabilityException;

    Iterator<String> list() throws DurabilityException;

    Iterator<String> list(String prefix) throws DurabilityException;
}
