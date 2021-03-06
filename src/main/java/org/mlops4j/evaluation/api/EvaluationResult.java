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

package org.mlops4j.evaluation.api;

import lombok.Getter;
import org.mlops4j.api.Result;
import org.mlops4j.api.ResultStatus;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@Getter
public class EvaluationResult extends Result {
    private final Iterable<? extends Evaluation> evaluations;

    private EvaluationResult(ResultStatus status, String message, Iterable<Evaluation> evaluations, Exception exception) {
        super(status, message, exception);
        this.evaluations = evaluations;
    }

    public static EvaluationResult success(Iterable<Evaluation> evaluations) {
        return new EvaluationResult(ResultStatus.SUCCESS, null, evaluations, null);
    }

    public static EvaluationResult failure(Iterable<Evaluation> evaluations, Exception exception, String message) {
        return new EvaluationResult(ResultStatus.FAILURE, message, evaluations, exception);
    }

}
