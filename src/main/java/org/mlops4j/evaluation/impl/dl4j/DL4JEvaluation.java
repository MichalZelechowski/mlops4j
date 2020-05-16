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
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.mlops4j.api.ComponentBuilder;
import org.mlops4j.dataset.api.DataSetId;
import org.mlops4j.evaluation.api.Evaluation;
import org.mlops4j.evaluation.api.Metric;
import org.mlops4j.evaluation.api.SingleMetricValue;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.nd4j.evaluation.IEvaluation;
import org.nd4j.evaluation.IMetric;
import org.nd4j.evaluation.classification.ROC;
import org.nd4j.evaluation.classification.ROCBinary;
import org.nd4j.evaluation.classification.ROCMultiClass;
import org.nd4j.serde.json.JsonMappers;
import org.nd4j.shade.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        public Evaluation build() {
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
    public ComponentBuilder<? super Evaluation> getBuilder() {
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
            List<Pair<? extends IMetric, String>> iMetrics;
            if (this.value instanceof org.nd4j.evaluation.classification.Evaluation) {
                org.nd4j.evaluation.classification.Evaluation.Metric[] values = org.nd4j.evaluation.classification.Evaluation.Metric.values();
                iMetrics = Lists.transform(Lists.newArrayList(values), m -> Pair.of(m, m.name()));
            } else if (this.value instanceof ROC) {
                iMetrics = Lists.transform(Lists.newArrayList(ROC.Metric.values()), m -> Pair.of(m, m.name()));
            } else if (this.value instanceof ROCBinary) {
                iMetrics = Lists.transform(Lists.newArrayList(ROCBinary.Metric.values()), m -> Pair.of(m, m.name()));
            } else if (this.value instanceof ROCMultiClass) {
                iMetrics = Lists.transform(Lists.newArrayList(ROCMultiClass.Metric.values()), m -> Pair.of(m, m.name()));
            } else {
                throw new IllegalArgumentException(String.format("Does not support evaluation of type %s", value.getClass()));
            }
            return iMetrics.stream()
                    .map(iMetric -> new Metric<>(
                            iMetric.getValue().toLowerCase(),
                            new SingleMetricValue(this.value.getValue(iMetric.getKey()))))
                    .toArray(Metric[]::new);
        }

        @Override
        public Optional<Metric> getMetric(String name) {
            // TODO should optimize lookup
            return Stream.of(this.getMetrics()).filter(m -> m.getName().equals(name)).findAny();
        }

        @Override
        public Set<String> getMetricNames() {
            // TODO should optimize lookup
            return Stream.of(this.getMetrics()).map(Metric::getName).collect(Collectors.toSet());
        }

        @Override
        public int compareTo(Evaluation o) {
            //TODO figure out real comparision, probably with injectable comparator
            //naive comparation will only check if all metrics exposed are strongly different, which may be inaccurate
            //in many cases (f.e. precision vs accuracy)
            List<Integer> comparisons = Lists.newLinkedList();
            for (Metric m : this.getMetrics()) {
                Optional<Metric> correspondingMetric = o.getMetric(m.getName());
                if (correspondingMetric.isPresent()) {
                    comparisons.add(correspondingMetric.map(cm -> m.compareTo(cm)).get());
                }
            }
            if (comparisons.stream().allMatch(i -> i > 0)) {
                return 1;
            }
            if (comparisons.stream().allMatch(i -> i < 0)) {
                return -1;
            }
            return 0;
        }

        @Override
        public Metadata<Evaluation> getMetadata() throws DurabilityException {
            // there's trust that value can be always deserialized - maybe Metric should be stored only?
            byte[] bytes = value.toJson().getBytes();
            return new Metadata<>(this)
                    .withParameter("value", bytes)
                    .withParameter("type", value.getClass().getName())
                    .withParameter("dataSetId", new String(dataSetId.asBytes()));
        }
    }
}
