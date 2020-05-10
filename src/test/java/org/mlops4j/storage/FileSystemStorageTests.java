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

package org.mlops4j.storage;

import com.google.common.io.Files;
import org.junit.jupiter.api.Test;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.mlops4j.storage.impl.FileSystemKeyValueStorage;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class FileSystemStorageTests {

    @Test
    public void testReadAndWrite() throws DurabilityException {
        FileSystemKeyValueStorage storage = new FileSystemKeyValueStorage.Builder().root(Files.createTempDir()).build();
        Random rnd = new Random();
        byte[] object_1 = new byte[100];
        byte[] object_2 = new byte[1000];
        byte[] object_3 = new byte[5];
        rnd.nextBytes(object_1);
        rnd.nextBytes(object_2);
        rnd.nextBytes(object_3);
        storage.put("some_key", object_1);
        storage.put("some/key", object_2);
        storage.put("some/long/key", object_3);

        assertThat(storage.get("some_key")).contains(object_1);
        assertThat(storage.get("some/key")).contains(object_2);
        assertThat(storage.get("some/long/key")).contains(object_3);
    }
}
