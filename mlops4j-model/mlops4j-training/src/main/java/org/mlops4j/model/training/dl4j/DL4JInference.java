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

package org.mlops4j.model.training.dl4j;

import com.google.common.base.Preconditions;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.mlops4j.api.Inference;
import org.mlops4j.api.InferenceMetadata;
import org.mlops4j.data.metadata.ComponentBuilder;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.*;
import java.util.Map;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class DL4JInference implements Inference {
    private final Model model;

    public DL4JInference(Model model) {
        Preconditions.checkArgument(model instanceof MultiLayerNetwork, "Only MultiLayerNetwork is supported");
        this.model = model;
    }

    @Override
    public INDArray output(INDArray input) {
        if (this.model instanceof MultiLayerNetwork) {
            return ((MultiLayerNetwork) this.model).output(input);
        }
        throw new IllegalStateException("Model is not supported yet");
    }

    @Override
    public byte[] getModelBinary() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            final OutputStream output = new BufferedOutputStream(bout);
            ModelSerializer.writeModel(model, output, true);
            return bout.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not get model binary", exception);
        }
    }

    @Override
    public InferenceMetadata getMetadata() {
        return new InferenceMetadata(this.getClass().getName()+"$Builder");
    }


    public static class Builder implements ComponentBuilder<DL4JInference> {

        private MultiLayerNetwork multiLayerNetwork;

        @Override
        public DL4JInference build() {
            return new DL4JInference(this.multiLayerNetwork);
        }

        @Override
        public ComponentBuilder<DL4JInference> fromParameters(Map<String, Serializable> parameters) {
            byte[] binary = (byte[]) parameters.get("model");
            String type = (String) parameters.get("type");
            if ("MulitLayer".equals(type)) {
                ByteArrayInputStream bin = new ByteArrayInputStream(binary);
                BufferedInputStream in = new BufferedInputStream(bin);
                try {
                    this.multiLayerNetwork = ModelSerializer.restoreMultiLayerNetwork(in, true);
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot restore multi layer network", e);
                }
            } else {
                throw new IllegalArgumentException("Type " + type + " is not supported");
            }
            return this;
        }
    }
}
