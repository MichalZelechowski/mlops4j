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

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mlops4j.dataset.api.DataSetId;
import org.mlops4j.evaluation.api.Evaluation;
import org.mlops4j.evaluation.api.Metric;
import org.mlops4j.storage.api.ComponentBuilder;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.nd4j.evaluation.IEvaluation;
import org.nd4j.serde.json.JsonMappers;
import org.nd4j.shade.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DL4JEvaluation implements Evaluation {

    protected final DataSetId dataSetId;

    @Override
    public DataSetId getDataSetId() {
        return this.dataSetId;
    }

    public static class Builder implements ComponentBuilder<Evaluation> {

        private IEvaluation value;
        private byte[] bytesValue;
        private String type;
        private DataSetId dataSetId;

        @Override
        public DL4JEvaluation build() {
            Preconditions.checkNotNull(dataSetId, "DataSetId not set");
            if (value != null) {
                return new DL4JIEvaluation(dataSetId, value);
            }
            if (bytesValue == null || type == null) {
                throw new IllegalArgumentException("Value of evaluation has to be set either as object or bytes with type");
            }
            IEvaluation evaluation;
            try {
                evaluation = (IEvaluation) JsonMappers.getMapper().readValue(bytesValue, Class.forName(type));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(String.format("JSON %s cannot be mapped to type %s", new String(bytesValue), type));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(String.format("Cannot deserialize JSON %s to type %s", new String(bytesValue), type));
            } catch (IOException e) {
                throw new IllegalStateException(String.format("Cannot read value from bytes %s", new String(bytesValue)));
            }
            return new DL4JIEvaluation(dataSetId, evaluation);
        }

        public Builder value(IEvaluation evaluation) {
            this.value = evaluation;
            return this;
        }

        public Builder value(InputStream stream) throws IOException {
            this.bytesValue = stream.readAllBytes();
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder dataSetId(DataSetId id) {
            this.dataSetId = id;
            return this;
        }

        public Builder dataSetId(String id) {
            this.dataSetId = new DataSetId();
            dataSetId.fromBytes(id.getBytes());
            return this;
        }
    }

    @Override
    public ComponentBuilder<Evaluation> getBuilder() {
        return new Builder();
    }

    private static class DL4JIEvaluation extends DL4JEvaluation {

        private final IEvaluation value;

        protected DL4JIEvaluation(DataSetId dataSetId, IEvaluation value) {
            super(dataSetId);
            this.value = value;
        }

        @Override
        public Metric[] getMetrics() {
            return new Metric[0];
        }

        @Override
        public Optional<Metric> getMetric(String name) {
            return Optional.empty();
        }

        @Override
        public int compareTo(Evaluation o) {
            return 0;
        }

        @Override
        public Metadata<Evaluation> getMetadata() throws DurabilityException {
            try {
                byte[] bytes = JsonMappers.getMapper().writeValueAsBytes(value);
                return new Metadata<>(this)
                        .withParameter("value", bytes)
                        .withParameter("type", value.getClass().getName())
                        .withParameter("dataSetId", new String(dataSetId.asBytes()));
            } catch (JsonProcessingException e) {
                throw new DurabilityException(String.format("Cannot convert %s to bytes", value));
            }
        }
    }
}
