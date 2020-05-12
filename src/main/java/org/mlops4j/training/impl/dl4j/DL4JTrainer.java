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

package org.mlops4j.training.impl.dl4j;

import org.deeplearning4j.nn.api.NeuralNetwork;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.api.ComponentBuilder;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.mlops4j.training.api.FitResult;
import org.mlops4j.training.api.Trainable;
import org.mlops4j.training.api.Trainer;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;

import java.util.concurrent.CompletableFuture;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public abstract class DL4JTrainer implements Trainer {

    public static class Builder implements ComponentBuilder<Trainer> {

        private Integer epochs = 1;

        @Override
        public Trainer build() {
            return new NeuralNetworkTrainer(epochs);
        }

        public Builder epochs(Integer epochs) {
            this.epochs = epochs;
            return this;
        }
    }


    @Override
    public ComponentBuilder<? super Trainer> getBuilder() {
        return new Builder();
    }

    public static class NeuralNetworkTrainer extends DL4JTrainer {

        private final Integer epochs;

        public NeuralNetworkTrainer(Integer epochs) {
            this.epochs = epochs;
        }

        @Override
        public CompletableFuture<FitResult> fit(Trainable trainable, DataSet trainSet) {
            final NeuralNetwork network = (NeuralNetwork) trainable.getModelRepresentation().get();
            final Object setRepresentation = trainSet.getRepresentation().get();
            return CompletableFuture.supplyAsync(() -> {
                for (int i = 0; i < epochs; ++i) {
                    try {
                        fit(network, setRepresentation);
                    } catch (RuntimeException rex) { //TODO consider if failure can happen due to out of memory
                        return FitResult.failure(i, rex, "Training failed");
                    }
                }
                return FitResult.success(epochs);
            });
        }

        private void fit(NeuralNetwork network, Object setRepresentation) {
            if (setRepresentation instanceof DataSetIterator) {
                network.fit((DataSetIterator) setRepresentation);
            } else if (setRepresentation instanceof MultiDataSetIterator) {
                network.fit((MultiDataSetIterator) setRepresentation);
            } else {
                throw new IllegalArgumentException(String.format("Cannot use %s representation for the training, required DataSetIterator or MultiDataSetIterator", setRepresentation));
            }
        }

        @Override
        public Metadata<Trainer> getMetadata() throws DurabilityException {
            return new Metadata<>(this).withParameter("epochs", epochs);
        }

    }
}
