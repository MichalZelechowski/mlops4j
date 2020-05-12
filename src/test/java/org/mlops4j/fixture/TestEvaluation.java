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

import lombok.ToString;
import org.mlops4j.dataset.api.DataSetId;
import org.mlops4j.evaluation.api.Evaluation;
import org.mlops4j.evaluation.api.Metric;
import org.mlops4j.evaluation.api.SingleMetricValue;
import org.mlops4j.api.ComponentBuilder;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;

import java.util.Optional;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@ToString
public class TestEvaluation implements Evaluation {
    private final float value;

    public TestEvaluation(float value) {
        this.value = value;
    }

    @Override
    public Metric[] getMetrics() {
        return new Metric[]{new Metric("testMetric", new SingleMetricValue((double) this.value))};
    }

    @Override
    public Optional<Metric> getMetric(String name) {
        return Optional.of(name).filter(n -> "testMetric".equals(n)).map(x -> getMetrics()[0]);
    }

    @Override
    public DataSetId getDataSetId() {
        return new DataSetId("test", "1", "2020");
    }

    @Override
    public int compareTo(Evaluation o) {
        return 0;
    }

    @Override
    public boolean equals(Object evaluation) {
        return this.value == ((TestEvaluation) evaluation).value;
    }

    @Override
    public Metadata<Evaluation> getMetadata() throws DurabilityException {
        return new Metadata<>(this).withParameter("value", value);
    }

    @Override
    public ComponentBuilder<? super Evaluation> getBuilder() {
        return new Builder();
    }

    public static final class Builder implements ComponentBuilder<Evaluation> {

        private Float value;

        public Builder value(Float value) {
            this.value = value;
            return this;
        }

        @Override
        public Evaluation build() {
            return new TestEvaluation(value);
        }
    }
}
