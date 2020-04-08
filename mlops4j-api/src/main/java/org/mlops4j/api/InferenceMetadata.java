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

import org.apache.commons.lang3.tuple.Pair;
import org.mlops4j.data.metadata.Metadata;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public class InferenceMetadata extends Metadata<Inference> {

    public InferenceMetadata(String component) {
        super(component, Collections.EMPTY_LIST);
    }

    public InferenceMetadata(String component, List<Pair<String, Object>> parameters) {
        super(component, parameters);
    }

    public Inference construct(byte[] bytes) {
        Inference.Builder<Inference> builder = (Inference.Builder) this.getBuilder();
        return builder.model(bytes).build();
    }

}
