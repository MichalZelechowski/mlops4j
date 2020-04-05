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

/**
 *
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public class PredictionService {

    protected final Serving serving;

    protected PredictionService(Serving serving) {
        this.serving = serving;
    }

    public Response predict(Request request) {
        return new Response(this.serving.infer(request.getInput()));
    }

    public static enum Mode {
        LOCAL
    }

    public static class Builder implements Cloneable {

        private Serving serving;
        private Mode mode;

        public Builder serving(Serving serving) {
            this.serving = serving;
            return this;
        }

        public Builder local() {
            this.mode = Mode.LOCAL;
            return this;
        }

        public PredictionService build() {
            if (this.serving == null) {
                throw new NullPointerException("Serving is not specified");
            }
            if (this.mode == Mode.LOCAL) {
                return new LocalPredictionService(serving);
            }
            throw new IllegalArgumentException("Mode " + this.mode + " is not supported");
        }
    }
}
