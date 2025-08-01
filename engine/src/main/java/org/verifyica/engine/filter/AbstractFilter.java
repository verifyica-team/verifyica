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

package org.verifyica.engine.filter;

import java.util.regex.Pattern;

/**
 * Class to implement AbstractFilter
 */
public abstract class AbstractFilter implements Filter {

    private final Pattern classNamePattern;

    /**
     * Constructor
     *
     * @param classNameRegex classNameRegex
     */
    protected AbstractFilter(String classNameRegex) {
        this.classNamePattern = Pattern.compile(classNameRegex);
    }

    /**
     * Method to get the class name Pattern
     *
     * @return the class name Pattern
     */
    protected Pattern getClassNamePattern() {
        return classNamePattern;
    }
}
