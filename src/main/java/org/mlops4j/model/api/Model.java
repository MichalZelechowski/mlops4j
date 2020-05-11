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

package org.mlops4j.model.api;

import org.mlops4j.evaluation.api.Evaluable;
import org.mlops4j.evaluation.api.Evaluation;
import org.mlops4j.inference.api.Inferable;
import org.mlops4j.storage.api.Durable;
import org.mlops4j.training.api.Trainable;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public interface Model extends Trainable, Evaluable, Inferable, Durable<Model> {

    ModelId getId();

    Optional<Model> getParent();

    Stream<Evaluation> getEvaluations();

}
