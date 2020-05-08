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

package org.mlops4j.training.api;

import lombok.Getter;
import org.mlops4j.api.Result;
import org.mlops4j.api.ResultStatus;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@Getter
public class FitResult extends Result {
    private final Integer iterationsDone;

    public FitResult(ResultStatus status, String message, Exception exception, Integer iterationsDone) {
        super(status, message, exception);
        this.iterationsDone = iterationsDone;
    }

    public static FitResult success(Integer iterationsDone) {
        return new FitResult(ResultStatus.SUCCESS, null, null, iterationsDone);
    }

    public static FitResult failure(Integer iterationsDone, Exception exception, String message) {
        return new FitResult(ResultStatus.FAILURE, message, exception, iterationsDone);
    }
}
