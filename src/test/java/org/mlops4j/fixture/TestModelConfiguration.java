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

package org.mlops4j.fixture;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mlops4j.api.Representation;
import org.mlops4j.model.api.ModelConfiguration;
import org.mlops4j.api.ComponentBuilder;
import org.mlops4j.storage.api.Metadata;
import org.mlops4j.storage.api.exception.DurabilityException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestModelConfiguration implements ModelConfiguration {

    private ThirdPartyModelRepresentation rep;

    @Override
    public Representation getModelRepresentation() {
        return Representation.of(rep);
    }

    @Override
    public Metadata getMetadata() throws DurabilityException {
        Metadata metadata = new Metadata(this)
                .withParameter("error",
                        // TODO should accept byte array as binary
                        new ByteArrayInputStream(ByteBuffer.allocate(4).putFloat(rep.error).array())
                );
        return metadata;
    }

    @Override
    public ComponentBuilder getBuilder() {
        return new Builder();
    }

    public static class Builder implements ComponentBuilder<ModelConfiguration> {

        private Float error = 1.0f;

        public Builder error(Float error) {
            this.error = error;
            return this;
        }

        public Builder error(InputStream error) throws IOException {
            this.error = ByteBuffer.wrap(error.readAllBytes()).getFloat();
            return this;
        }

        public ModelConfiguration build() {
            return new TestModelConfiguration(new ThirdPartyModelRepresentation(error));
        }
    }
}
