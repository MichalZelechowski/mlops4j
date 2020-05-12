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

package org.mlops4j.fixture;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mlops4j.api.Representation;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.evaluation.api.Evaluable;
import org.mlops4j.evaluation.api.EvaluationResult;
import org.mlops4j.evaluation.api.ModelEvaluator;
import org.mlops4j.api.ComponentBuilder;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestModelEvaluator implements ModelEvaluator {
    @Override
    public CompletableFuture<EvaluationResult> evaluate(Evaluable evaluable, DataSet evalSet) {
        Representation<ThirdPartyEvaluation> representation = evaluable.getEvaluationRepresentation();
        ThirdPartyEvaluation thirdPartyEvaluation = representation.get();

        Representation<ThirdPartyDataSetRepresentation> dataSetRepresentation = evalSet.getRepresentation();
        ThirdPartyDataSetRepresentation dataSet = dataSetRepresentation.get();

        TestEvaluation testEvaluation = new TestEvaluation(thirdPartyEvaluation.evaluate(dataSet));
        EvaluationResult result = EvaluationResult.success(Collections.singletonList(testEvaluation));

        return CompletableFuture.completedFuture(result);
    }

    @Override
    public Metadata<ModelEvaluator> getMetadata() throws DurabilityException {
        Metadata metadata = new Metadata(this);
        return metadata;
    }

    @Override
    public ComponentBuilder<? super ModelEvaluator> getBuilder() {
        return new Builder();
    }

    public static class Builder implements ComponentBuilder<ModelEvaluator> {

        public ModelEvaluator build() {
            return new TestModelEvaluator();
        }
    }
}
