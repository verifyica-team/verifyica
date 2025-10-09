/*
 * Copyright (C) Verifyica project authors and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.verifyica.examples.support;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to build a block of text
 */
public class TextBlock {

    private final List<String> lines;

    /**
     * Constructor
     */
    public TextBlock() {
        this.lines = new ArrayList<>();
    }

    /**
     * Method to add a line of text
     *
     * @param line line of text
     * @return this
     */
    public TextBlock line(String line) {
        this.lines.add(line);
        return this;
    }

    /**
     * Method to add an empty line of text
     *
     * @return this
     */
    public TextBlock line() {
        return line("");
    }

    /**
     * Method to get the text block as a string
     *
     * @return the text block as a string
     */
    public String toString() {
        return String.join(System.lineSeparator(), lines);
    }
}
