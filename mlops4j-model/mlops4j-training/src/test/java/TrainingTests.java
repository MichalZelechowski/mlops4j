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

import org.junit.jupiter.api.Test;
import org.mlops4j.data.preparation.DataReference;
import org.mlops4j.model.registry.InMemoryKeyValueStorage;
import org.mlops4j.model.registry.JavaDataSerializer;
import org.mlops4j.model.registry.ModelRegistry;
import org.mlops4j.model.training.Training;
import org.mlops4j.model.training.TrainingConfiguration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class TrainingTests {

    @Test
    public void trainModel() {
        ModelRegistry registry = new ModelRegistry.Builder()
                .storage(new InMemoryKeyValueStorage())
                .serializer(new JavaDataSerializer())
                .build();

        TrainingConfiguration trainingConfiguration = new TrainingConfiguration();
        DataReference dataReference = new DataReference() {
            @Override
            public String getName() {
                return "some name";
            }
        };
        Training training = new Training.Builder()
                .configuration(trainingConfiguration)
                .dataReferences(dataReference)
                .registry(registry)
                .build();

        training.perform("test_model", "test_version");

        assertThat(registry.getModel("test_model", "test_version")).isPresent();
    }
}
