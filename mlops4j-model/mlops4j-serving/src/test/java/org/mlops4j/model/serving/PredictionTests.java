/*
 * Copyright 2020 Michał Żelechowski <MichalZelechowski@github.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mlops4j.model.serving;

import static org.assertj.core.api.Assertions.assertThat;
import org.datavec.api.records.impl.Record;
import org.datavec.api.writable.DoubleWritable;
import org.datavec.api.writable.FloatWritable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.nd4j.shade.protobuf.common.collect.Lists;

/**
 *
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public class PredictionTests {

    @Test
    public void createPredictionFromServing() {
        Serving serving = new Serving.Builder().build();
        PredictionService service = new PredictionService.Builder().serving(serving).local().build();

        Request request = new Request(new Record(Lists.newArrayList(new FloatWritable(2.0f)), null));
        Response response = service.predict(request);

        assertThat(response).isNotNull();
        assertThat(response.getOutput()).isEqualTo(new NumericalOutput(4.0f));
    }
}
