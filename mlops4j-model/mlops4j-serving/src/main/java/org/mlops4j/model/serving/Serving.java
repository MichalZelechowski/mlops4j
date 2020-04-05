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
package org.mlops4j.model.serving;

import org.datavec.api.records.Record;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 *
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public class Serving {

    private final TrainedModel modelReference;

    private Serving(TrainedModel modelReference) {
        this.modelReference = modelReference;
    }

    public Prediction infer(Features input) {
        if (input instanceof SingleInput) {
            Record record = ((SingleInput) input).getRecord();
            INDArray inputArray = this.prepareData(record);
            INDArray outputArray = this.getOutput(inputArray);
            Record outputRecord = this.prepareData(outputArray);
            return new SinglePrediction(outputRecord);
        }
        throw new IllegalArgumentException("Input " + input + " is not yet supported");
    }

    private INDArray prepareData(Record record) {
        return this.modelReference.prepareData(record);
    }

    private INDArray getOutput(INDArray input) {
        return this.modelReference.output(input);
    }

    private Record prepareData(INDArray array) {
        return this.modelReference.prepareData(array);
    }

    public static class Builder implements Cloneable {

        private TrainedModel modelReference;

        public Serving build() {
            if (this.modelReference == null) {
                throw new NullPointerException("Model reference not set");
            }
            return new Serving(this.modelReference);
        }

        public Builder trainedModel(TrainedModel modelReference) {
            this.modelReference = modelReference;
            return this;
        }
    }

}
