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

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.mlops4j.api.ComponentBuilder;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.dataset.api.DataSetId;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

    @Override
    public Builder getBuilder() {
        return new Builder();
    }

    @Override
    public Metadata<DataSet<D>> getMetadata() throws DurabilityException {
        return new Metadata<>(this)
                .withParameter("name", this.id.getName())
                .withParameter("version", this.id.getVersion())
                .withParameter("partition", this.id.getPartition());
    }

    public static class Builder<D> implements ComponentBuilder<DL4JDataSet<D>> {

        private File csv;
        private int batchSize = 32;
        private String name;
        private String version;
        private String partition;
        private org.nd4j.linalg.dataset.DataSet set;

        public Builder<D> csv(File file) {
            this.csv = file;
            return this;
        }

        public Builder<D> csv(String name) {
            this.csv = new File(name);
            return this;
        }

        public Builder<D> batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder<D> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<D> version(String version) {
            this.version = version;
            return this;
        }

        public Builder<D> partition(String partition) {
            this.partition = partition;
            return this;
        }

        private DataSetId getId() {
            return new DataSetId(name, version, partition);
        }

        public Builder<D> set(org.nd4j.linalg.dataset.DataSet set) {
            this.set = set;
            return this;
        }

        public Builder<D> set(InputStream stream) {
            this.set = new org.nd4j.linalg.dataset.DataSet();
            this.set.load(stream);
            return this;
        }

        @Override
        public DL4JDataSet<D> build() {
            Preconditions.checkNotNull(this.name, "DataSet has to be named");
            Preconditions.checkNotNull(this.version, "DataSet has to be versioned");
            Preconditions.checkNotNull(this.partition, "DataSet has to have partition");

            if (csv != null) {
                try {
                    CSVRecordReader reader = new CSVRecordReader();
                    reader.initialize(new FileSplit(this.csv));
                    return (DL4JDataSet<D>) new CSVDataSet(getId(), reader, this.batchSize, this.name);
                } catch (IOException e) {
                    throw new IllegalArgumentException(String.format("Could not read csv file %s", this.csv), e);
                } catch (InterruptedException e) {
                    throw new IllegalArgumentException(String.format("Reading of csv file %s interrupted", this.csv), e);
                }
            }
            if (this.set != null) {
                return (DL4JDataSet<D>) new SingleDataSet(getId(), this.set);
            }
            throw new UnsupportedOperationException("Cannot build data set from given arguments");
        }

        public Builder<D> id(DataSetId id) {
            this.name = id.getName();
            this.version = id.getVersion();
            this.partition = id.getPartition();
            return this;
        }
    }

}
