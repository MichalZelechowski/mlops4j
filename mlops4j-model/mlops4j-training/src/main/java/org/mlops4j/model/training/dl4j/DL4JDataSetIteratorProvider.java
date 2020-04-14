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

package org.mlops4j.model.training.dl4j;

import org.nd4j.linalg.dataset.adapter.SingletonDataSetIterator;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIteratorFactory;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class DL4JDataSetIteratorProvider implements DL4JDataSetProvider<DataSetIterator>, DataSetIteratorFactory {
    private final String name;
    private final String partition;
    private final DataSetIterator iterator;

    public DL4JDataSetIteratorProvider(String name, String partition, DataSetIterator iterator) {
        this.name = name;
        this.partition = partition;
        this.iterator = iterator;
    }

    public DL4JDataSetIteratorProvider(String name, String partition, DataSet dataSet) {
        this.name = name;
        this.partition = partition;
        this.iterator = new SingletonDataSetIterator(dataSet);
    }


    @Override
    public DataSetIterator get() {
        return this.iterator;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPartition() {
        return this.partition;
    }

    @Override
    public DataSetIterator create() {
        return this.get();
    }
}
