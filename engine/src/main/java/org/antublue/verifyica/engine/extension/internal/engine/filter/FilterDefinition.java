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

import java.util.regex.Pattern;

/** Interface to implement Filter */
public interface Processor {

    /** Filter type */
    enum Type {
        /** IncludeClassNameFilter */
        INCLUDE_CLASS_NAME_FILTER,
        /** ExcludeClassNameFilter */
        EXCLUDE_CLASS_NAME_FILTER
    }

    /**
     * Method to get the Filter type
     *
     * @return the Filter type
     */
    Type getType();

    /**
     * Method to get the Filter pattern
     *
     * @return the Filter Pattern
     */
    Pattern getPattern();
}
