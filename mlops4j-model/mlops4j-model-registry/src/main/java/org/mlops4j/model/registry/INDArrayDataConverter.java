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

import org.apache.commons.lang3.tuple.Pair;
import org.datavec.api.records.Record;
import org.datavec.api.util.ndarray.RecordConverter;
import org.mlops4j.api.DataConverter;
import org.mlops4j.data.metadata.ComponentBuilder;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.List;

/**
 *
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public class INDArrayDataConverter implements DataConverter {

    @Override
    public INDArray map(Record record) {
        return RecordConverter.toArray(record.getRecord());
    }

    @Override
    public Record map(INDArray array) {
        return new org.datavec.api.records.impl.Record(RecordConverter.toRecord(array), null);
    }
    
    public static class Builder implements ComponentBuilder<INDArrayDataConverter> {

        @Override
        public INDArrayDataConverter build() {
            return new INDArrayDataConverter();
        }

        @Override
        public Builder fromParameters(List<Pair<String, Object>> parameters) {
            return this;
        }
        
    }

}
