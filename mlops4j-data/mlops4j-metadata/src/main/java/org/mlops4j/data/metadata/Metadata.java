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
package org.mlops4j.data.metadata;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * @param <TYPE>
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public abstract class Metadata<TYPE> implements Serializable {

    protected String component;
    protected Map<String, Serializable> parameters;

    protected Metadata(String component, Map<String, Serializable> parameters) {
        this.component = component;
        this.parameters = parameters;
    }

    protected Metadata() {
    }

    protected ComponentBuilder<TYPE> getBuilder() {
        try {
            Class<? extends ComponentBuilder> builderClass = (Class<? extends ComponentBuilder>) this.getClass().forName(this.component);
            if (!(ComponentBuilder.class.isAssignableFrom(builderClass))) {
                throw new IllegalArgumentException(String.format("No builder %s found", this.component));
            }

            ComponentBuilder<TYPE> builder = builderClass.getConstructor().newInstance().fromParameters(this.parameters);
            return builder;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Cannot build component " + this.component, exception);
        }
    }

    public TYPE construct() {
        ComponentBuilder<TYPE> builder = this.getBuilder();
        return builder.build();
    }

    public static class BareMetadata<TYPE> extends Metadata<TYPE> {
        public BareMetadata(Class<? extends ComponentBuilder<TYPE>> component, Map<String, Serializable> parameters) {
            super(component.getName(), parameters);
        }

        public BareMetadata(Class<? extends ComponentBuilder<TYPE>> component) {
            super(component.getName(), Collections.EMPTY_MAP);
        }
    }
}
