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

package org.mlops4j.model.impl.dl4j;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.mlops4j.api.Representation;
import org.mlops4j.model.api.ModelConfiguration;
import org.mlops4j.storage.api.ComponentBuilder;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public abstract class DL4JModelConfiguration<MODEL extends Model> implements ModelConfiguration<MODEL> {

    public static class Builder<MODEL extends Model> implements ComponentBuilder<DL4JModelConfiguration<MODEL>> {

        private MultiLayerConfiguration multilayerConfiguration;
        private MultiLayerNetwork network;

        @Override
        public DL4JModelConfiguration build() {
            if (multilayerConfiguration != null) {
                return new MultiLayerModelConfiguration(multilayerConfiguration, network);
            }
            throw new IllegalArgumentException("No recognized configuration set");
        }

        public Builder<MODEL> configuration(MultiLayerConfiguration configuration) {
            this.multilayerConfiguration = configuration;
            return this;
        }

        public Builder<MODEL> configuration(String json) {
            return this.configuration(MultiLayerConfiguration.fromJson(json));
        }

        public Builder<MODEL> model(InputStream model) throws IOException {
            network = ModelSerializer.restoreMultiLayerNetwork(model, true);
            return this;
        }
    }

    @Override
    public ComponentBuilder getBuilder() {
        return new Builder();
    }

    private static class MultiLayerModelConfiguration extends DL4JModelConfiguration<MultiLayerNetwork> {

        private final MultiLayerConfiguration configuration;
        private final MultiLayerNetwork network;

        public MultiLayerModelConfiguration(MultiLayerConfiguration configuration, MultiLayerNetwork network) {
            this.configuration = configuration;
            if (network == null) {
                this.network = new MultiLayerNetwork(configuration);
                this.network.init();
            } else {
                this.network = network;
            }
        }

        @Override
        public Representation<MultiLayerNetwork> getModelRepresentation() {
            return Representation.of(network);
        }

        @Override
        public Metadata getMetadata() throws DurabilityException {
            Path tempFile = null;
            try {
                tempFile = Files.createTempFile("mlops4j-model", ".bin");
                // TODO what about data normalization?
                ModelSerializer.writeModel(this.network, tempFile.toFile(), true);
                return new Metadata(this)
                        .withParameter("configuration", configuration.toJson())
                        .withParameter("model", tempFile);
            } catch (IOException e) {
                throw new DurabilityException("Cannot serialize network", e);
            } finally {
                if (tempFile != null) {
                    //noinspection ResultOfMethodCallIgnored
                    tempFile.toFile().delete();
                }
            }
        }

    }
}
