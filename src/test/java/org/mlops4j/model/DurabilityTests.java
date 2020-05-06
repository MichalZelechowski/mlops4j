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

package org.mlops4j.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.mlops4j.api.ComponentBuilder;
import org.mlops4j.api.DurabilityException;
import org.mlops4j.api.Durable;
import org.mlops4j.api.Metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class DurabilityTests {
    @Test
    public void simplePropertiesPersist() throws DurabilityException {
        Durable durable = new SimpleClass("some string", 100, Instant.now());
        Metadata<SimpleClass> metadata = durable.getMetadata();
        SimpleClass retrieved = metadata.getDurable();

        assertThat(retrieved).isNotSameAs(durable);
        assertThat(retrieved).isEqualTo(durable);
    }

    @Test
    public void nestedPropertiesPersist() throws DurabilityException {
        SimpleClass nestedObject = new SimpleClass("some string", 100, Instant.now());
        Durable container = new ContainerClass(nestedObject);
        Metadata<ContainerClass> metadata = container.getMetadata();
        ContainerClass retrieved = metadata.getDurable();

        assertThat(retrieved).isNotSameAs(container);
        assertThat(retrieved).isEqualTo(container);
    }

    @Test
    public void binaryContentPropertiesPersist() throws DurabilityException {
        byte[] content = new byte[10000];
        for (int i = 0; i < 10000; i++) {
            content[i] = (byte)i;
        }
        ClassWithBinaryContent binaryContent = new ClassWithBinaryContent(content);
        Metadata<ClassWithBinaryContent> metadata = binaryContent.getMetadata();
        ClassWithBinaryContent retrieved = metadata.getDurable();

        assertThat(retrieved).isNotSameAs(binaryContent);
        assertThat(retrieved).isEqualTo(binaryContent);
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ClassWithBinaryContent implements Durable<ClassWithBinaryContent> {
        private final byte[] content;

        @Override
        public Metadata<ClassWithBinaryContent> getMetadata() throws DurabilityException {
            return new Metadata<>(this.getBuilder()).withParameter("content", new ByteArrayInputStream(content));
        }

        @Override
        public ComponentBuilder<ClassWithBinaryContent> getBuilder() {
            return new Builder();
        }

        public static class Builder implements ComponentBuilder<ClassWithBinaryContent> {
            private byte[] content;

            public Builder content(InputStream content) throws IOException {
                this.content = content.readAllBytes();
                return this;
            }

            @Override
            public ClassWithBinaryContent build() {
                return new ClassWithBinaryContent(content);
            }
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ContainerClass implements Durable<ContainerClass> {
        private final SimpleClass nestedObject;

        @Override
        public Metadata<ContainerClass> getMetadata() throws DurabilityException {
            return new Metadata<>(this.getBuilder()).withParameter("nestedObject", nestedObject);
        }

        @Override
        public ComponentBuilder<ContainerClass> getBuilder() {
            return new Builder();
        }

        public static class Builder implements ComponentBuilder<ContainerClass> {
            private SimpleClass nestedObject;

            public Builder nestedObject(SimpleClass nestedObject) {
                this.nestedObject = nestedObject;
                return this;
            }

            @Override
            public ContainerClass build() {
                return new ContainerClass(nestedObject);
            }
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    public static class SimpleClass implements Durable<SimpleClass> {
        private String stringProperty;
        private Integer intProperty;
        private Instant timestampProperty;

        @Override
        public Metadata getMetadata() throws DurabilityException {
            return new Metadata(this.getBuilder())
                    .withParameter("stringProperty", stringProperty)
                    .withParameter("intProperty", intProperty)
                    .withParameter("timestampProperty", timestampProperty);
        }

        @Override
        public ComponentBuilder getBuilder() {
            return new Builder();
        }

        public static class Builder implements ComponentBuilder<SimpleClass> {
            private String stringProperty;
            private Integer intProperty;
            private Instant timestampProperty;

            public Builder stringProperty(String property) {
                this.stringProperty = property;
                return this;
            }

            public Builder intProperty(Integer property) {
                this.intProperty = property;
                return this;
            }

            public Builder timestampProperty(Instant property) {
                this.timestampProperty = property;
                return this;
            }

            public SimpleClass build() {
                return new SimpleClass(stringProperty, intProperty, timestampProperty);
            }
        }
    }
}
