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

package org.antublue.verifyica.engine.extension.internal.engine.filter;

import java.lang.reflect.Method;

/** Class to implement ExcludeClassFilter */
public class ExcludeClassFilter extends AbstractFilter {

    /**
     * Constructor
     *
     * @param classNameRegex classNameRegex
     * @param methodNameRegex methodNameRegex
     */
    private ExcludeClassFilter(String classNameRegex, String methodNameRegex) {
        super(classNameRegex, methodNameRegex);
    }

    @Override
    public Type getType() {
        return Type.EXCLUDE_CLASS;
    }

    @Override
    public boolean matches(Class<?> testClass, Method testMethod) {
        return getClassNamePattern().matcher(testClass.getName()).find()
                && getMethodNamePattern().matcher(testMethod.getName()).find();
    }

    /**
     * Method to create an ExcludeFilter
     *
     * @param classRegex classRegex
     * @param methodRegex methodRegex
     * @return an ExcludeFilter
     */
    public static ExcludeClassFilter create(String classRegex, String methodRegex) {
        return new ExcludeClassFilter(classRegex, methodRegex);
    }
}
