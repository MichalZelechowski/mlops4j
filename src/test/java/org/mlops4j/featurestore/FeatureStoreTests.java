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

package org.mlops4j.featurestore;

import org.junit.jupiter.api.Test;
import org.mlops4j.dataset.api.DataSet;
import org.mlops4j.dataset.api.DataSetId;
import org.mlops4j.featurestore.api.FeatureStore;
import org.mlops4j.featurestore.impl.FeatureStoreBuilder;
import org.mockito.Mockito;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class FeatureStoreTests {

    @Test
    public void addDataSet() {
        FeatureStore store = new FeatureStoreBuilder().build();

        DataSet<?> dataSet = Mockito.mock(DataSet.class);
        Mockito.when(dataSet.getId()).thenReturn(new DataSetId("test", "v1", "2020"));
        store.put(dataSet);


    }
}
