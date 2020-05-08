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

package org.mlops4j.evaluation.impl.dl4j;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.api.NeuralNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.mlops4j.api.Representation;
import org.mlops4j.evaluation.api.EvaluationConfiguration;
import org.mlops4j.model.impl.dl4j.DL4JModelConfiguration;
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

public abstract class DL4JEvaluationConfiguration implements EvaluationConfiguration {

    public static class Builder implements ComponentBuilder<EvaluationConfiguration> {

        private NeuralNetwork network;

        @Override
        public DL4JEvaluationConfiguration build() {
            return new NeuralNetworkEvaluationConfiguration(network);
        }

        public <N extends Model> Builder modelConfiguration(DL4JModelConfiguration<N> modelConfiguration) {
            // TODO consider check of type and throwing durability / configuration exception
            network = (NeuralNetwork) modelConfiguration.getModelRepresentation().get();
            return this;
        }

        public <N extends Model> Builder network(InputStream network) throws IOException {
            this.network = ModelSerializer.restoreMultiLayerNetwork(network, true);
            return this;
        }
    }

    @Override
    public ComponentBuilder<EvaluationConfiguration> getBuilder() {
        return new Builder();
    }

    private static class NeuralNetworkEvaluationConfiguration extends DL4JEvaluationConfiguration {

        private NeuralNetwork network;

        private NeuralNetworkEvaluationConfiguration(NeuralNetwork network) {
            this.network = network;
        }

        @Override
        public Representation<NeuralNetwork> getEvaluationRepresentation() {
            return Representation.of(this.network);
        }

        @Override
        public Metadata getMetadata() throws DurabilityException {
            Path tempFile = null;
            try {
                tempFile = Files.createTempFile("mlops4j-model", ".bin");
                // TODO what about data normalization?
                // TODO handle type mismatch issue
                ModelSerializer.writeModel((Model) this.network, tempFile.toFile(), true);
                return new Metadata<>(this).withParameter("network", tempFile);
            } catch (IOException e) {
                throw new DurabilityException("Cannot serialize network", e);
            } finally {
                if (tempFile != null) {
                    tempFile.toFile().delete();
                }
            }
        }

    }
}
