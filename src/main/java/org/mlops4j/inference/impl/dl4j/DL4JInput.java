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

package org.mlops4j.inference.impl.dl4j;

import lombok.AllArgsConstructor;
import org.mlops4j.inference.api.Input;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public abstract class DL4JInput<I> implements Input<I> {

    public static DL4JInput<INDArray> from(INDArray array) {
        return new INDArrayInput(array);
    }

    public static DL4JInput<DataSet> from(DataSet dataSet) {
        return new DataSetInput(dataSet);
    }

    public static DL4JInput<INDArray> from(float[] values) {
        return new INDArrayInput(Nd4j.create(values, 1, values.length));
    }

    @AllArgsConstructor
    public static class INDArrayInput extends DL4JInput<INDArray> {

        private final INDArray value;

        @Override
        public INDArray getValue() {
            return this.value;
        }
    }
    @AllArgsConstructor
    public static class DataSetInput extends DL4JInput<DataSet> {

        private final DataSet value;

        @Override
        public DataSet getValue() {
            return this.value;
        }
    }
}
