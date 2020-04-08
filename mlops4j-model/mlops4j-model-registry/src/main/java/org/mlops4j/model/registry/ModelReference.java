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
package org.mlops4j.model.registry;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mlops4j.api.DataConverter;
import org.mlops4j.api.Inference;
import org.mlops4j.api.ModelEvaluation;
import org.mlops4j.data.metadata.HasMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelReference implements HasMetadata<ModelReferenceMetadata> {

    private final String name;
    private final String version;
    private final DataConverter converter;
    private final Inference inference;
    private final List<ModelEvaluation> evaluations;

    @Override
    public ModelReferenceMetadata getMetadata() {
        return new ModelReferenceMetadata(this.name, this.version, this.converter.getMetadata(), this.inference.getMetadata());
    }

    public Stream<ModelEvaluation> getEvaluations() {
        return this.evaluations.stream();
    }

    public void addEvaluation(ModelEvaluation evaluation) {
        this.evaluations.add(evaluation);
    }

    public static class Builder implements Cloneable {

        private DataConverter converter;
        private Inference inference;
        private String version;
        private String name;
        private List<ModelEvaluation> evaluations = Lists.newLinkedList();

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

        public Builder evaluations(ModelEvaluation... evaluations) {
            this.evaluations.addAll(Arrays.asList(evaluations));
            return this;
        }

        public ModelReference build() {
            this.name = Optional.ofNullable(this.name).orElseThrow(() -> new IllegalArgumentException("Name not set"));
            this.version = Optional.ofNullable(this.version).orElseThrow(() -> new IllegalArgumentException("Version not set"));
            this.converter = Optional.ofNullable(this.converter).orElseGet(INDArrayDataConverter::new);
            this.inference = Optional.ofNullable(this.inference).orElseThrow(() -> new IllegalArgumentException("Inference not set"));
            return new ModelReference(name, version, converter, inference, evaluations);
        }

    }

}
