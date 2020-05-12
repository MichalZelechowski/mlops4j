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

import org.datavec.api.records.reader.RecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.mlops4j.api.Representation;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.dataset.api.DataSetId;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
abstract class DL4JIteratorDataSet extends DL4JDataSet<DataSetIterator> {

    protected final DataSetIterator iterator;
    protected final int batchSize;

    protected DL4JIteratorDataSet(DataSetId id, DataSetIterator iterator) {
        super(id);
        this.iterator = iterator;
        this.batchSize = 32;
    }

    protected DL4JIteratorDataSet(DataSetId id, RecordReader reader) {
        this(id, reader, 32);
    }

    protected DL4JIteratorDataSet(DataSetId id, RecordReader reader, int batchSize) {
        super(id);
        this.iterator = new RecordReaderDataSetIterator(reader, batchSize);
        this.batchSize = batchSize;
    }

    @Override
    public Representation<DataSetIterator> getRepresentation() {
        return Representation.of(this.iterator);
    }

    @Override
    public Metadata<DataSet<DataSetIterator>> getMetadata() throws DurabilityException {
        return super.getMetadata().withParameter("batchSize", this.batchSize);
    }
}
