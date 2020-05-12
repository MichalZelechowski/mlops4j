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

import org.mlops4j.dataset.api.DataSetId;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.adapter.SingletonDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.ByteArrayOutputStream;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class SingleDataSet extends DL4JIteratorDataSet {
    private final DataSet set;

    protected SingleDataSet(DataSetId id, org.nd4j.linalg.dataset.DataSet set) {
        super(id, new SingletonDataSetIterator(set));
        this.set = set;
    }

    @Override
    public Metadata<org.mlops4j.dataset.api.DataSet<DataSetIterator>> getMetadata() throws DurabilityException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        set.save(bos);
        return super.getMetadata().withParameter("set", bos.toByteArray());
    }
}
