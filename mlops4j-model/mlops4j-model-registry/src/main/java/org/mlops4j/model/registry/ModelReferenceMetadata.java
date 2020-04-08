/*
 * Copyright 2020 Michał Żelechowski <MichalZelechowski@github.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mlops4j.model.registry;

import org.mlops4j.api.DataConverter;
import org.mlops4j.api.DataConverterMetadata;
import org.mlops4j.api.Inference;
import org.mlops4j.api.InferenceMetadata;
import org.mlops4j.data.metadata.Metadata;

/**
 *
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public class ModelReferenceMetadata extends Metadata<ModelReference> {

    private final String name;
    private final String version;
    private final DataConverterMetadata dataConverter;
    private final InferenceMetadata inference;

    public ModelReferenceMetadata(String name, String version, DataConverterMetadata dataConverter, InferenceMetadata inference) {
        this.name = name;
        this.version = version;
        this.dataConverter = dataConverter;
        this.inference = inference;
    }
    
    public DataConverter getConverter() {
        return dataConverter.construct();
    }

    public Inference getInference(byte[] bytes) {
        return inference.construct(bytes);
    }

}
