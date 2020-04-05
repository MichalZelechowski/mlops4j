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

import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelReference implements HasMetadata {

    private final String name;
    private final String version;
    private final DataConverter converter;
    private final Inference inference;

    @Override
    public ModelReferenceMetadata getMetadata() {
        return new ModelReferenceMetadata(this.name, this.version, this.converter.getMetadata(), this.inference.getMetadata());
    }

    public static class Builder implements Cloneable {

        private DataConverter converter;
        private Inference inference;
        private String version;
        private String name;

        public Builder converter(DataConverter converter) {
            this.converter = converter;
            return this;
        }

        public Builder inference(Inference inference) {
            this.inference = inference;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public ModelReference build() {
            this.name = Optional.ofNullable(this.name).orElseThrow(() -> new IllegalArgumentException("Name not set"));
            this.version = Optional.ofNullable(this.version).orElseThrow(() -> new IllegalArgumentException("Version not set"));
            this.converter = Optional.ofNullable(this.converter).orElseGet(() -> new INDArrayDataConverter());
            this.inference = Optional.ofNullable(this.inference).orElseThrow(() -> new IllegalArgumentException("Inference not set"));
            return new ModelReference(name, version, converter, inference);
        }

    }

}
