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

package org.antublue.verifyica.engine.filter;

import java.lang.reflect.Method;

/** Interface to implement Filter */
public interface Filter {

    /** Filter type */
    enum Type {
        /** Unknown */
        UNKNOWN,
        /** IncludeClass */
        INCLUDE_CLASS,
        /** ExcludeClass */
        EXCLUDE_CLASS,
        /** IncludeTaggedClass */
        INCLUDE_TAGGED_CLASS,
        /** ExcludeTaggedClass */
        EXCLUDE_TAGGED_CLASS
    }

    /**
     * Method to get the Filter Type
     *
     * @return the Filter Type
     */
    Type getType();

    /**
     * Method to return if a Filter matches a Class and Method
     *
     * @param testClass testClass
     * @param testMethod testMethod
     * @return true if the test class and test method match, else false
     */
    boolean matches(Class<?> testClass, Method testMethod);
}
