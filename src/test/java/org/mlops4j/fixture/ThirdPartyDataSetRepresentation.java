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

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */
public class ThirdPartyDataSetRepresentation {

    private int size;
    private int counter;

    public ThirdPartyDataSetRepresentation(int size) {
        this.size = size;
        this.counter = size;
    }

    public boolean hasNext() {
        counter--;
        boolean result = counter != 0;
        if (!result) {
            counter = size;
        }
        return result;
    }
}
