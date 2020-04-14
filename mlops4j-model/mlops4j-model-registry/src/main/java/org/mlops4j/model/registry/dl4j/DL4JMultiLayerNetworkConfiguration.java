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

package org.mlops4j.model.registry.dl4j;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class DL4JMultiLayerNetworkConfiguration extends DL4JModelConfiguration<MultiLayerNetwork> {

    private final MultiLayerNetwork implementation;

    private DL4JMultiLayerNetworkConfiguration(MultiLayerConfiguration configuration) {
        this.implementation = new MultiLayerNetwork(configuration);
    }

    @Override
    public MultiLayerNetwork getModelImplementation() {
        return this.implementation;
    }

    public static class Builder {
        private MultiLayerConfiguration configuration;

        public DL4JMultiLayerNetworkConfiguration build() {
            return new DL4JMultiLayerNetworkConfiguration(configuration);
        }

        public Builder configuration(MultiLayerConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder configuration(NeuralNetConfiguration.ListBuilder builder) {
            return this.configuration(builder.build());
        }
    }
}
