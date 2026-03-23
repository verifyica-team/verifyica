/*
 * Copyright 2024-present Verifyica project authors and contributors. All rights reserved.
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
 * Interface for filters that determine whether test classes should be included or excluded.
 *
 * <p>Filters are used during test discovery to determine which test classes should be
 * executed. Each filter has a type and can match against test classes based on various criteria
 * such as class name patterns, annotations, or tags.
 */
public interface Filter {

    /**
     * Enumeration of filter types supported by the engine.
     */
    enum Type {

        /**
         * Unknown or unspecified filter type.
         */
        UNKNOWN,

        /**
         * Filter that includes classes matching specific criteria.
         */
        INCLUDE_CLASS,

        /**
         * Filter that excludes classes matching specific criteria.
         */
        EXCLUDE_CLASS,

        /**
         * Filter that includes classes with specific tags.
         */
        INCLUDE_TAGGED_CLASS,

        /**
         * Filter that excludes classes with specific tags.
         */
        EXCLUDE_TAGGED_CLASS
    }

    /**
     * Returns the type of this filter.
     *
     * @return the filter type
     */
    Type getType();

    /**
     * Determines whether this filter matches the specified test class.
     *
     * @param testClass the class to test for a match
     * @return true if the test class matches the filter criteria, false otherwise
     */
    boolean matches(Class<?> testClass);
}
