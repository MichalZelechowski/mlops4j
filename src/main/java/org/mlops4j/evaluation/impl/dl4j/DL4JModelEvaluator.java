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

import org.deeplearning4j.nn.api.NeuralNetwork;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.evaluation.api.Evaluable;
import org.mlops4j.evaluation.api.EvaluationResult;
import org.mlops4j.evaluation.api.ModelEvaluator;
import org.mlops4j.storage.api.ComponentBuilder;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public abstract class DL4JModelEvaluator implements ModelEvaluator {

    public static class Builder implements ComponentBuilder<ModelEvaluator> {

        private EvaluationType type = EvaluationType.BASE;

        @Override
        public DL4JModelEvaluator build() {
            switch (type) {
                case BASE:
                    return new BaseEvaluator();
            }
            throw new IllegalArgumentException("No recognized configuration set");
        }

        public Builder neuralNetwork(EvaluationType type) {
            this.type = type;
            return this;
        }

    }

    @Override
    public ComponentBuilder<ModelEvaluator> getBuilder() {
        return new Builder();
    }

    private static class BaseEvaluator extends DL4JModelEvaluator {

        @Override
        public CompletableFuture<EvaluationResult> evaluate(Evaluable evaluable, DataSet evalSet) {
            // TODO is cast checking required? check construction
            NeuralNetwork network = (NeuralNetwork) evaluable.getEvaluationRepresentation().get();
            // TODO is representation type check required?
            Object setRepresentation = evalSet.getRepresentation().get();
            Evaluation evaluation = new Evaluation();
            if (setRepresentation instanceof DataSetIterator) {
                evaluation.setLabelsList((((DataSetIterator) setRepresentation)).getLabels());
                return CompletableFuture.supplyAsync(() -> {
                    Evaluation[] evaluations = network.doEvaluation((DataSetIterator) setRepresentation, evaluation);
                    //TODO require real evaluation
                    return EvaluationResult.success(Collections.emptyList());
                });
            } else {
                return CompletableFuture.completedFuture(
                        EvaluationResult.failure(Collections.emptyList(), null,
                                String.format("Unsupported type of dataset %s", setRepresentation.getClass()))
                );
            }
        }

        @Override
        public Metadata<ModelEvaluator> getMetadata() throws DurabilityException {
            return new Metadata<>(this);
        }
    }

    public enum EvaluationType {
        //TODO it's just terrible name
        BASE
    }
}
