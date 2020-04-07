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

package org.mlops4j.model.validation;

import org.mlops4j.model.evaluation.ModelEvaluation;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public interface ComparisonStrategy {
    boolean areComparable(ModelEvaluation a, ModelEvaluation b);

    ComparisonStatus compare(ModelEvaluation a, ModelEvaluation b);
}
