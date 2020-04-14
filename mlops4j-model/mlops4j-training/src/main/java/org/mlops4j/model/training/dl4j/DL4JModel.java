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
import org.mlops4j.api.Inference;
import org.mlops4j.model.training.Model;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DL4JModel extends Model {
    private final Inference inference;

    @Override
    public Inference getInference() {
        return this.inference;
    }

    public static class Builder {
        private Inference inference;
        private org.deeplearning4j.nn.api.Model model;

        public DL4JModel build() {
            if (this.inference == null) {
                this.inference = new DL4JInference(this.model);
            }
            return new DL4JModel(inference);
        }

        public Builder inference(Inference inference) {
            this.inference = inference;
            return this;
        }

        public Builder neuralNetwork(NeuralNetwork network) {
            if (network instanceof org.deeplearning4j.nn.api.Model) {
                this.model = (org.deeplearning4j.nn.api.Model)network;
            }
            return this;
        }

    }
}
