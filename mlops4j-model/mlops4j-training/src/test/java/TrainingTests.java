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

import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.junit.jupiter.api.Test;
import org.mlops4j.data.preparation.DataSet;
import org.mlops4j.model.registry.Model;
import org.mlops4j.model.registry.ModelConfiguration;
import org.mlops4j.model.registry.ModelRegistry;
import org.mlops4j.model.registry.dl4j.DL4JMultiLayerNetworkConfiguration;
import org.mlops4j.model.training.Training;
import org.mlops4j.model.training.TrainingConfiguration;
import org.mlops4j.model.training.dl4j.DL4JDataSetIteratorProvider;
import org.mlops4j.model.training.dl4j.DL4JEpochBasedTrainingConfiguration;
import org.mlops4j.storage.InMemoryKeyValueStorage;
import org.mlops4j.storage.JavaDataSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class TrainingTests {

    @Test
    public void firstTrainingOfModel() {
        ModelRegistry registry = new ModelRegistry.Builder()
                .storage(new InMemoryKeyValueStorage())
                .serializer(new JavaDataSerializer())
                .build();

        TrainingConfiguration trainingConfiguration = new DL4JEpochBasedTrainingConfiguration.Builder().epochs(1).build();
        ModelConfiguration modelConfiguration = new DL4JMultiLayerNetworkConfiguration.Builder().configuration(
                new NeuralNetConfiguration.Builder()
                        .list()
                        .layer(0, new DenseLayer.Builder().nIn(2).nOut(3).build())
                        .layer(1, new OutputLayer.Builder(
                                LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                                .activation(Activation.SOFTMAX)
                                .nIn(3).nOut(2).build())
        ).build();

        DataSet dataReference = new DL4JDataSetIteratorProvider("test_set", "20191012120000",
                new org.nd4j.linalg.dataset.DataSet(Nd4j.ones(1, 2), Nd4j.ones(1, 2)));

        Training training = new Training.Builder()
                .trainingConfiguration(trainingConfiguration)
                .modelConfiguration(modelConfiguration)
                .trainingDataSet(dataReference)
                .modelRegistry(registry)
                .build();

        training.run("test_model", "test_version");

        Optional<Model> model = registry.getModel("test_model", "test_version", "test_set", "20191012120000", "1");
        assertThat(model).isPresent();
    }

//    @Test
//    public void trainModel() {
//        ModelRegistry registry = new ModelRegistry.Builder()
//                .storage(new InMemoryKeyValueStorage())
//                .serializer(new JavaDataSerializer())
//                .build();
//
//        TrainingConfiguration trainingConfiguration = new TrainingConfiguration();
//        DataSet dataReference = new DataSet() {
//            @Override
//            public String getName() {
//                return "some name";
//            }
//        };
//        Training training = new Training.Builder()
//                .trainingConfiguration(trainingConfiguration)
//                .trainingDataSet(dataReference)
//                .modelRegistry(registry)
//                .build();
//
//        training.run("test_model", "test_version");
//
//        assertThat(registry.getModel("test_model", "test_version")).isPresent();
//    }
}
