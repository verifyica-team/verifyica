/*
 * Copyright (C) 2024 The Verifyica project authors
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

package org.antublue.verifyica.engine.interceptor.internal.engine.filter;

import java.util.regex.Pattern;

/** Class to implement AbstractFilter */
public abstract class AbstractFilter implements Filter {

    private final Pattern classNamePattern;
    private final Pattern methodNamePattern;

    /**
     * Constructor
     *
     * @param classNameRegex classNameRegex
     * @param methodNameRegex methodNameRegex
     */
    protected AbstractFilter(String classNameRegex, String methodNameRegex) {
        this.classNamePattern = Pattern.compile(classNameRegex);
        this.methodNamePattern = Pattern.compile(methodNameRegex);
    }

    /**
     * Method to get the class name Pattern
     *
     * @return the class name Pattern
     */
    protected Pattern getClassNamePattern() {
        return classNamePattern;
    }

    /**
     * Method to get the test method Pattern
     *
     * @return the test method Pattern
     */
    protected Pattern getMethodNamePattern() {
        return methodNamePattern;
    }
}
