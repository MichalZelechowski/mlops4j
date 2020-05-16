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

package org.mlops4j.validation;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.mlops4j.api.ResultStatus;
import org.mlops4j.evaluation.api.Evaluation;
import org.mlops4j.evaluation.api.Metric;
import org.mlops4j.evaluation.api.SingleMetricValue;
import org.mlops4j.fixture.TestEvaluation;
import org.mlops4j.validation.api.Validable;
import org.mlops4j.validation.api.ValidationResult;
import org.mlops4j.validation.api.Validator;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class ValidatorTests {

    @Test
    public void validateSomeValidables() throws ExecutionException, InterruptedException {
        Validator<String> validator = new TestValidator();
        Validable<String> validable1 = new TestValidable("1", 0.5f);
        Validable<String> validable2 = new TestValidable("2", 0.8f);

        CompletableFuture<ValidationResult<String>> future = validator.validate(validable1, validable2);

        assertThat(future).isCompleted();
        ValidationResult<String> result = future.get();
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getSelectedId()).isEqualTo("2");
    }

    private static class TestValidator implements Validator<String> {
        @Override
        public CompletableFuture<ValidationResult<String>> validate(Validable<String>... validables) {
            Evaluation eval1 = validables[0].getEvaluations().next();
            Evaluation eval2 = validables[1].getEvaluations().next();
            Metric<Float> metric1 = eval1.getMetric("testMetric").orElse(new Metric("testMetric", new SingleMetricValue(0.0)));
            Metric<Float> metric2 = eval2.getMetric("testMetric").orElse(new Metric("testMetric", new SingleMetricValue(0.0)));
            String id = metric1.compareTo(metric2) > 0 ? validables[0].getId() : validables[1].getId();
            ValidationResult<String> result = new ValidationResult<>(ResultStatus.SUCCESS, null, null, id);
            return CompletableFuture.completedFuture(result);
        }
    }

    @AllArgsConstructor
    private static class TestValidable implements Validable<String> {

        @Getter
        private final String id;
        private final Float value;

        @Override
        public Iterator<Evaluation> getEvaluations() {
            return Lists.<Evaluation>newArrayList(new TestEvaluation(value)).iterator();
        }

    }
}
