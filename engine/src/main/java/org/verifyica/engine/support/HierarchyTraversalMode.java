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

package org.verifyica.engine.support;

import static java.lang.String.format;

/**
 * HierarchyTraversalMode provides enumeration values for hierarchy traversal modes.
 */
public enum HierarchyTraversalMode {

    /**
     * Top down traversal mode.
     */
    TOP_DOWN,

    /**
     * Bottom up traversal mode.
     */
    BOTTOM_UP;

    /**
     * Private constructor to prevent instantiation of this enum.
     */
    HierarchyTraversalMode() {}

    /**
     * Decodes a Verifyica HierarchyTraversalMode to JUnit HierarchyTraversalMode.
     *
     * @param hierarchyTraversalMode the Verifyica HierarchyTraversalMode to decode
     * @return the corresponding JUnit HierarchyTraversalMode
     */
    public static org.junit.platform.commons.support.HierarchyTraversalMode decode(
            HierarchyTraversalMode hierarchyTraversalMode) {
        if (hierarchyTraversalMode == TOP_DOWN) {
            return org.junit.platform.commons.support.HierarchyTraversalMode.TOP_DOWN;
        } else if (hierarchyTraversalMode == BOTTOM_UP) {
            return org.junit.platform.commons.support.HierarchyTraversalMode.BOTTOM_UP;
        } else {
            throw new IllegalArgumentException(format("Invalid hierarchyTraversalMode [%s]", hierarchyTraversalMode));
        }
    }
}
