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

package org.mlops4j.model.training;

import org.apache.commons.lang3.tuple.Pair;
import org.mlops4j.api.Inference;
import org.mlops4j.data.metadata.ComponentBuilder;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.List;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class MockInference implements Inference {
    @Override
    public INDArray output(INDArray input) {
        return null;
    }

    @Override
    public byte[] getModelBinary() {
        return new byte[0];
    }

    public static class Builder implements Inference.Builder<MockInference> {

        @Override
        public MockInference build() {
            return new MockInference();
        }

        @Override
        public ComponentBuilder<MockInference> fromParameters(List<Pair<String, Object>> parameters) {
            return this;
        }

        @Override
        public Inference.Builder<MockInference> model(byte[] bytes) {
            return this;
        }
    }
}
