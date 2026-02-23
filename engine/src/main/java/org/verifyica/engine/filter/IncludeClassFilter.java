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
 * IncludeClassFilter provides filtering to include classes by regex pattern.
 */
public class IncludeClassFilter extends AbstractFilter {

    /**
     * Private constructor to prevent direct instantiation.
     *
     * @param classNameRegex the regex pattern for matching class names
     */
    private IncludeClassFilter(String classNameRegex) {
        super(classNameRegex);
    }

    @Override
    public Type getType() {
        return Type.INCLUDE_CLASS;
    }

    @Override
    public boolean matches(Class<?> testClass) {
        return getClassNamePattern().matcher(testClass.getName()).find();
    }

    /**
     * Creates an IncludeFilter with the specified class regex.
     *
     * @param classRegex the regex pattern for matching class names
     * @return a new IncludeClassFilter instance
     */
    public static IncludeClassFilter create(String classRegex) {
        return new IncludeClassFilter(classRegex);
    }
}
