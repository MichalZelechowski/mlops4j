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

package org.mlops4j.dataset.impl.dl4j;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mlops4j.api.Representation;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.dataset.api.DataSetId;
import org.nd4j.linalg.dataset.adapter.SingletonDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

// TODO consider if this class should be actual single with different build methods
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DL4JDataSet<D> implements DataSet<D> {
    private final DataSetId id;

    @Override
    public DataSetId getId() {
        return this.id;
    }

    public static DL4JDataSet<DataSetIterator> from(DataSetId id, DataSetIterator iterator) {
        return new DataSetWithIterator(id, iterator);
    }

    public static DL4JDataSet<DataSetIterator> from(DataSetId id, org.nd4j.linalg.dataset.DataSet set) {
        return new DataSetWithIterator(id, new SingletonDataSetIterator(set));
    }

    private static class DataSetWithIterator extends DL4JDataSet<DataSetIterator> {

        private final DataSetIterator iterator;

        public DataSetWithIterator(DataSetId id, DataSetIterator iterator) {
            super(id);
            this.iterator = iterator;
        }

        @Override
        public Representation<DataSetIterator> getRepresentation() {
            return Representation.of(this.iterator);
        }

    }
}
