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

package org.mlops4j.experiment.api;

import lombok.*;
import org.mlops4j.dataset.api.DataSetId;
import org.mlops4j.storage.api.Storable;
import org.nd4j.shade.protobuf.common.primitives.Bytes;

import java.util.Arrays;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@ToString
public class ExperimentId implements Storable {
    private String name;
    private String version;
    private DataSetId trainSetId;
    private DataSetId evalSetId;

    @Override
    public byte[] asBytes() {
        byte[] modelIdBytes = (name + "/" + version).getBytes();
        byte[] trainSetIdBytes = this.trainSetId.asBytes();
        byte[] evalSetIdBytes = this.evalSetId.asBytes();
        byte[] zero = new byte[]{0};
        byte[] result = Bytes.concat(modelIdBytes, zero, trainSetIdBytes, zero, evalSetIdBytes);
        return result;
    }

    @Override
    public void fromBytes(byte[] bytes) {
        int[] indexes = new int[3];
        int j = 0;
        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i] == 0) {
                indexes[j++] = i;
            }
        }
        byte[] modelIdBytes = Arrays.copyOfRange(bytes, indexes[0], indexes[1]);
        byte[] trainSetIdBytes = Arrays.copyOfRange(bytes, indexes[1] + 1, indexes[2]);
        byte[] evalSetIdBytes = Arrays.copyOfRange(bytes, indexes[2] + 1, bytes.length);

        this.name = new String(modelIdBytes).split("/")[0];
        this.version = new String(modelIdBytes).split("/")[1];
        this.trainSetId = new DataSetId();
        this.trainSetId.fromBytes(trainSetIdBytes);
        this.evalSetId = new DataSetId();
        this.evalSetId.fromBytes(evalSetIdBytes);
    }
}
