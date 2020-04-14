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

package org.mlops4j.model.training.dl4j;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.deeplearning4j.nn.api.NeuralNetwork;
import org.mlops4j.model.training.Model;
import org.mlops4j.model.training.TrainingResult;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;

import java.util.Optional;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DL4JEpochBasedTrainer implements DL4JTrainer<NeuralNetwork> {
    private final Integer epochs;

    @Override
    public TrainingResult fit(NeuralNetwork modelImplementation, DL4JDataSetProvider dataSetProvider) {
        Object dataSet = dataSetProvider.get();
        if (dataSet instanceof MultiDataSetIterator) {
            for (int i = 0; i < this.epochs; ++i) {
                modelImplementation.fit((MultiDataSetIterator) dataSet);
            }
        } else if (dataSet instanceof DataSetIterator) {
            for (int i = 0; i < this.epochs; ++i) {
                modelImplementation.fit((DataSetIterator) dataSet);
            }
        } else {
            throw new IllegalArgumentException("DataSet provider returned unsupported type: " +
                    Optional.ofNullable(dataSet).orElse(new Object()).getClass());
        }
        Model model = new DL4JModel.Builder().neuralNetwork(modelImplementation).build();
        TrainingResult result = new TrainingResult(model, this.epochs);
        return result;
    }


}
