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
import org.mlops4j.api.ComponentBuilder;
import org.mlops4j.api.Representation;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.dataset.api.DataSetId;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDataSet implements DataSet {
    @Override
    public Representation getRepresentation() {
        return Representation.of(new ThirdPartyDataSetRepresentation(10));
    }

    @Override
    public DataSetId getId() {
        return new DataSetId("testSet", "v1", "20200427");
    }

    @Override
    public Metadata getMetadata() throws DurabilityException {
        return new Metadata(this);
    }

    @Override
    public ComponentBuilder getBuilder() {
        return new Builder();
    }

    public static class Builder implements ComponentBuilder<TestDataSet> {

        public TestDataSet build() {
            return new TestDataSet();
        }
    }
}
