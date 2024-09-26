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

package org.verifyica.engine.support;

import static java.lang.String.format;

/** Class to implement HierarchyTraversalMode */
public enum HierarchyTraversalMode {

    /** Top down */
    TOP_DOWN,
    /** Bottom up */
    BOTTOM_UP;

    /** Constructor */
    HierarchyTraversalMode() {}

    /**
     * Method to decode a Verifyica HierarchyTraversalMode to JUnit HierarchyTraversalMode
     *
     * @param hierarchyTraversalMode hierarchyTraversalMode
     * @return a JUnit HierarchyTraversalMode
     */
    public static org.junit.platform.commons.support.HierarchyTraversalMode decode(
            HierarchyTraversalMode hierarchyTraversalMode) {
        if (hierarchyTraversalMode == TOP_DOWN) {
            return org.junit.platform.commons.support.HierarchyTraversalMode.TOP_DOWN;
        } else if (hierarchyTraversalMode == BOTTOM_UP) {
            return org.junit.platform.commons.support.HierarchyTraversalMode.BOTTOM_UP;
        } else {
            throw new IllegalArgumentException(
                    format("Invalid hierarchyTraversalMode [%s]", hierarchyTraversalMode));
        }
    }
}
