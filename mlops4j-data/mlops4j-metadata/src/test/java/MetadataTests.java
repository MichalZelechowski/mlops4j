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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.mlops4j.data.metadata.ComponentBuilder;
import org.mlops4j.data.metadata.HasMetadata;
import org.mlops4j.data.metadata.Metadata;

import java.io.Serializable;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class MetadataTests {

    @Test
    public void createComponent() {
        MetadataOwner component = new MetadataOwner();
        MetadataOwner copy = component.getMetadata().construct();
        assertThat(copy).isInstanceOf(component.getClass());
    }


    @Test
    public void createComponentWithParamters() {
        MetadataOwner component = new MetadataOwner("some_param");
        MetadataOwner copy = component.getMetadata().construct();
        assertThat(copy).isInstanceOf(component.getClass());
    }

    @Test
    public void createCompoundComponent() {
        MetadataOwner component = new MetadataOwner("some_param");
        CompoundComponent compoundComponent = new CompoundComponent(component);
        CompoundComponent copy = compoundComponent.getMetadata().construct();
        assertThat(copy).isInstanceOf(compoundComponent.getClass());
        assertThat(copy.innerComponent).isInstanceOf(component.getClass());
        assertThat(copy.innerComponent.param).isEqualTo("some_param");
    }

    public static class MetadataOwner implements HasMetadata<Metadata.BareMetadata<MetadataOwner>> {

        private final String param;

        public MetadataOwner(String param) {
            this.param = param;
        }

        public MetadataOwner() {
            this.param = null;
        }

        @Override
        public Metadata.BareMetadata<MetadataOwner> getMetadata() {
            return new Metadata.BareMetadata<>(OwnerBuilder.class, ImmutableMap.of("param", this.param));
        }
    }

    public static class OwnerBuilder implements ComponentBuilder<MetadataOwner> {

        private String param;

        @Override
        public MetadataOwner build() {
            return new MetadataOwner(this.param);
        }

        @Override
        public ComponentBuilder<MetadataOwner> fromParameters(Map<String, Serializable> parameters) {
            if (parameters.size() == 1) {
                this.param = (String) parameters.values().iterator().next();
            }
            return this;
        }
    }

    public static class CompoundComponent implements HasMetadata<Metadata.BareMetadata<CompoundComponent>> {

        private final MetadataOwner innerComponent;

        public CompoundComponent(MetadataOwner innerComponent) {
            this.innerComponent = innerComponent;
        }

        @Override
        public Metadata.BareMetadata<CompoundComponent> getMetadata() {
            return new Metadata.BareMetadata<>(CompoundComponentBuilder.class,
                    ImmutableMap.of("innerComponent", this.innerComponent.getMetadata()));
        }
    }

    public static class CompoundComponentBuilder implements ComponentBuilder<CompoundComponent> {
        private MetadataOwner innerComponent;

        @Override
        public CompoundComponent build() {
            return new CompoundComponent(this.innerComponent);
        }

        @Override
        public ComponentBuilder<CompoundComponent> fromParameters(Map<String, Serializable> parameters) {
            Metadata<MetadataOwner> metadata = (Metadata<MetadataOwner>) parameters.values().iterator().next();
            this.innerComponent = metadata.construct();
            return this;
        }
    }
}