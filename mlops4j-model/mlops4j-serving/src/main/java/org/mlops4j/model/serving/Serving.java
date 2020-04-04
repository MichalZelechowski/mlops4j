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

import com.google.common.collect.Lists;
import org.datavec.api.records.impl.Record;
import org.datavec.api.writable.FloatWritable;

/**
 *
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public class Serving {

    public Output infer(Input input) {
        if (input instanceof SingleInput) {
            Record record = ((SingleInput) input).getRecord();
            float inputValue = record.getRecord().get(0).toFloat();
            return new SingleOutput(new Record(Lists.newArrayList(new FloatWritable(inputValue * inputValue)), null));
        }
        throw new IllegalArgumentException("Input " + input + " is not yet supported");
    }

    public static class Builder implements Cloneable {

        public Serving build() {
            return new Serving();
        }
    }

}
