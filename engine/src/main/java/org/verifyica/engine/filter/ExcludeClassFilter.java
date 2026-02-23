/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

/**
 * ExcludeClassFilter is a filter that excludes test classes based on their class names.
 * It uses a regular expression to match the class names and determines if a test class
 * should be excluded from execution.
 */
public class ExcludeClassFilter extends AbstractFilter {

    /**
     * Constructor for ExcludeClassFilter that takes a regular expression to match class names.
     *
     * @param classNameRegex classNameRegex
     */
    private ExcludeClassFilter(String classNameRegex) {
        super(classNameRegex);
    }

    @Override
    public Type getType() {
        return Type.EXCLUDE_CLASS;
    }

    @Override
    public boolean matches(Class<?> testClass) {
        return getClassNamePattern().matcher(testClass.getName()).find();
    }

    /**
     * Creates an ExcludeClassFilter with the specified regular expression for class names.
     *
     * @param classRegex classRegex
     * @return an ExcludeFilter
     */
    public static ExcludeClassFilter create(String classRegex) {
        return new ExcludeClassFilter(classRegex);
    }
}
