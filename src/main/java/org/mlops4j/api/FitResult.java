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

package org.mlops4j.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class FitResult {
    private final ResultStatus status;
    private final Integer iterationsDone;
    private final Exception exception;
    private final String message;

    public static FitResult success(Integer iterationsDone) {
        return new FitResult(ResultStatus.SUCCESS, iterationsDone, null, null);
    }

    public static FitResult failure(Integer iterationsDone, Exception exception, String message) {
        return new FitResult(ResultStatus.FAILURE, iterationsDone, exception, message);
    }
}
