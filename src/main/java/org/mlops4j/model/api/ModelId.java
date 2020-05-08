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

package org.mlops4j.model.api;

import lombok.*;
import org.mlops4j.storage.api.Storable;
import org.mlops4j.dataset.api.DataSetId;

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
public class ModelId implements Serializable, Storable {
    private String name;
    private String version;
    private DataSetId dataSetId;
    private Integer iteration;

    public ModelId(String name, String version) {
        this(name, version, null, null);
    }

    public ModelId withIteration(Integer iteration) {
        if (iteration != this.iteration) {
            return new ModelId(this.name, this.version, this.dataSetId, iteration);
        } else {
            return this;
        }
    }

    public ModelId withDataSetId(DataSetId dataSetId) {
        if (dataSetId != this.dataSetId) {
            return new ModelId(this.name, this.version, dataSetId, this.iteration);
        } else {
            return this;
        }
    }

    @Override
    public byte[] asBytes() {
        StringJoiner joiner = new StringJoiner("/")
                .add(this.name)
                .add(this.version);
        if (this.dataSetId != null) {
            joiner.add(new String(this.dataSetId.asBytes()));
        } else {
            joiner.add("###NONE###");
        }
        if (this.iteration != null) {
            joiner.add(this.iteration.toString());
        } else {
            joiner.add("0");
        }
        return joiner.toString().getBytes();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        if (name != null || version != null || dataSetId != null || iteration != null) {
            throw new IllegalStateException("ModelId is immutable, but something is trying to change it");
        }

        String[] parts = new String(bytes).split("/");
        this.name = parts[0];
        this.version = parts[1];
        if (parts[2].equals("###NONE###")) {
            this.dataSetId = null;
            this.iteration = parts[3].equals("0") ? null : Integer.parseInt(parts[3]);
        } else {
            DataSetId dataSetId = new DataSetId();
            // TODO bring that logic to DataSetId?
            dataSetId.fromBytes(String.join("/", parts[2], parts[3], parts[4]).getBytes());

            this.dataSetId = dataSetId;
            this.iteration = Integer.parseInt(parts[5]);
        }
    }
}
