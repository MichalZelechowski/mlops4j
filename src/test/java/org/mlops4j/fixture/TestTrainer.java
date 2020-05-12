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
import org.mlops4j.api.ComponentBuilder;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.mlops4j.training.api.FitResult;
import org.mlops4j.training.api.Trainable;
import org.mlops4j.training.api.Trainer;

import java.util.concurrent.CompletableFuture;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestTrainer implements Trainer {

    @Override
    public CompletableFuture<FitResult> fit(Trainable trainable, DataSet trainSet) {
        Representation<ThirdPartyModelRepresentation> representation = trainable.getModelRepresentation();
        ThirdPartyModelRepresentation testRepresentation = representation.get();

        Representation<ThirdPartyDataSetRepresentation> dataSetRepresentation = trainSet.getRepresentation();
        ThirdPartyDataSetRepresentation dataSet = dataSetRepresentation.get();

        testRepresentation.fit(dataSet);

        return CompletableFuture.completedFuture(FitResult.success(1));
    }

    @Override
    public Metadata<Trainer> getMetadata() throws DurabilityException {
        return new Metadata<>(this);
    }

    @Override
    public ComponentBuilder<? super Trainer> getBuilder() {
        return new Builder();
    }

    public static class Builder implements ComponentBuilder<Trainer> {

        public Trainer build() {
            return new TestTrainer();
        }

    }
}