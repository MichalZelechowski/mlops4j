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
import org.datavec.api.writable.NDArrayWritable;
import org.datavec.api.writable.Writable;
import org.datavec.api.writable.WritableType;
import org.junit.jupiter.api.Test;
import org.mlops4j.api.Inference;
import org.mlops4j.data.metadata.ComponentBuilder;
import org.mlops4j.model.registry.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public class PredictionTests {

    @Test
    public void createPredictionFromServingWithModel() {
        ModelReference reference = new ModelReference.Builder()
                .name("testModel")
                .version("1.0")
                .converter(new INDArrayDataConverter())
                .inference(new SqrInference())
                .build();

        ModelRegistry registry = new ModelRegistry.Builder()
                .storage(new InMemoryKeyValueStorage())
                .serializer(new JavaDataSerializer())
                .build();
        registry.putModel(reference);

        TrainedModel modelReference = new TrainedModel.Builder()
                .name("testModel")
                .version("1.0")
                .modelRegistry(registry)
                .build();
        
        Serving serving = new Serving.Builder()
                .trainedModel(modelReference)
                .build();
        
        PredictionService service = new PredictionService.Builder()
                .serving(serving)
                .local()
                .build();

        Request request = new Request(new Record(Lists.newArrayList(new FloatWritable(2.0f)), null));
        Response response = service.predict(request);

        assertThat(response).isNotNull();
        Record outputRecord = ((Record) response.getOutput().getValue());
        assertThat(outputRecord.getRecord()).hasSize(1);
        List<Writable> resultArray = outputRecord.getRecord();
        assertThat(resultArray.get(0).getType()).isEqualTo(WritableType.NDArray);
        NDArrayWritable ndResultArray = (NDArrayWritable) resultArray.get(0);
        assertThat(ndResultArray.get().getFloat(0, 0)).isEqualTo(4.0f);
        assertThat(ndResultArray.get().shape()).isEqualTo(new long[]{1, 1});
    }

    public static class SqrInference implements Inference {

        @Override
        public INDArray output(INDArray input) {
            return Transforms.pow(input, 2.0, true);
        }

        @Override
        public byte[] getModelBinary() {
            return new byte[1];
        }

        public static class Builder implements Inference.Builder {

            @Override
            public Inference build() {
                return new SqrInference();
            }

            @Override
            public Builder model(byte[] bytes) {
                return this;
            }

            @Override
            public ComponentBuilder fromParameters(List parameters) {
                return this;
            }

        }

    }
}
