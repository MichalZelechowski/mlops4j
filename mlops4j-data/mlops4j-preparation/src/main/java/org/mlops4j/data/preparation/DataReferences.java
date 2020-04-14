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

package org.mlops4j.data.preparation;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DataReferences {
    private final LinkedHashMap<String, DataSet> references;

    public Optional<DataSet> getReference(String name) {
        return Optional.ofNullable(this.references.get(name));
    }

    public List<DataSet> getReferences() {
        return Lists.newLinkedList(this.references.values());
    }

    public static class Builder {
        private LinkedHashMap<String, DataSet> references = new LinkedHashMap<>();

        public Builder reference(String name, DataSet reference) {
            this.references.put(name, reference);
            return this;
        }

        public DataReferences build() {
            return new DataReferences(this.references);
        }

    }
}
