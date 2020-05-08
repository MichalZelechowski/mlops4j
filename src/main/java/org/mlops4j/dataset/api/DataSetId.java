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

package org.mlops4j.dataset.api;

import lombok.*;
import org.mlops4j.storage.api.Storable;

import java.io.Serializable;
import java.util.StringJoiner;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@ToString
public class DataSetId implements Serializable, Storable {
    private String name;
    private String version;
    private String partition;

    @Override
    public byte[] asBytes() {
        return new StringJoiner("/")
                .add(name)
                .add(version)
                .add(partition)
                .toString().getBytes();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        if (name != null || version != null || partition != null) {
            throw new IllegalStateException("DataSetId is immutable, but something is trying to change it");
        }

        String[] parts = new String(bytes).split("/");
        this.name = parts[0];
        this.version = parts[1];
        this.partition = parts[2];
    }
}
