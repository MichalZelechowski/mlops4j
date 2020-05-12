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

import org.deeplearning4j.nn.api.Classifier;
import org.mlops4j.inference.api.Inferable;
import org.mlops4j.inference.api.Inference;
import org.mlops4j.inference.api.Input;
import org.mlops4j.inference.api.Output;
import org.mlops4j.api.ComponentBuilder;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
// TODO inference type may depend on runtime condition - maybe it should only tell about interpretation of results
// while batch vs single or parallelization should be setup in different way
public abstract class DL4JInference<I extends Input<?>, O extends Output<?>> implements Inference<I, O> {

    public static class Builder<I extends Input<?>, O extends Output<?>> implements ComponentBuilder<DL4JInference<I, O>> {

        private InferenceType type = InferenceType.SINGLE;

        @Override
        public DL4JInference<I, O> build() {
            switch (type) {
                case SINGLE:
                    return (DL4JInference<I, O>) new SingleInference();
            }
            return null;
        }

        public Builder type(InferenceType type) {
            this.type = type;
            return this;
        }

        public Builder type(String type) {
            return this.type(InferenceType.valueOf(type));
        }

        public Builder single() {
            return this.type(InferenceType.SINGLE);
        }

    }

    private static class SingleInference extends DL4JInference<DL4JInput<?>, Output<?>> {
        private final ReentrantLock lock = new ReentrantLock();

        @Override
        public CompletableFuture infer(Inferable inferable, DL4JInput input) {
            try {
                lock.lock();
                Classifier model = (Classifier) inferable.getModelRepresentation().get();
                if (input.getValue() instanceof INDArray) {
                    return CompletableFuture.completedFuture(
                            new ClassifierPredictor.SingleRecordPredictor().predict(model, input)
                    );
                } else {
                    throw new UnsupportedOperationException(String.format("Cannot support value of type %s", input.getValue().getClass()));
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Metadata getMetadata() throws DurabilityException {
            return new Metadata(this).withParameter("type", InferenceType.SINGLE.name());
        }
    }

    @Override
    public ComponentBuilder<? super Inference<I, O>> getBuilder() {
        return new Builder();
    }

    public enum InferenceType {
        SINGLE
    }

}
