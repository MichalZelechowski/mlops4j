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

import com.google.common.base.Preconditions;
import org.deeplearning4j.nn.api.Classifier;
import org.mlops4j.inference.api.Output;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public interface ClassifierPredictor<I extends DL4JInput, O extends Output> {

    O predict(Classifier classifier, I input);

    public class SingleRecordPredictor implements ClassifierPredictor<DL4JInput<INDArray>, Output<Integer>> {

        @Override
        public Output<Integer> predict(Classifier classifier, DL4JInput<INDArray> input) {
            INDArray inputArray = input.getValue();
            //TODO this check could be configurable for optimization
            Preconditions.checkArgument(inputArray.shape().length == 2,
                    String.format("Input shape is expected to be 2, but found %s instead", inputArray.shape().length));
            Preconditions.checkArgument(inputArray.shape()[0] == 1,
                    String.format("Expecting 1 row as input but found %s instead", inputArray.shape()[0]));
            int[] prediction = classifier.predict(inputArray);
            return Output.from(prediction[0]);
        }
    }
}
