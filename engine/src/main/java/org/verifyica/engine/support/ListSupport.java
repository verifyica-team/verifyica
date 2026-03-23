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

package org.verifyica.engine.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ListSupport provides utility methods for working with lists.
 */
public class ListSupport {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ListSupport() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Copies and reverses a List.
     *
     * @param list the list to copy and reverse
     * @return a reversed copy of the List
     * @param <V> the type of elements in the list
     */
    public static <V> List<V> copyAndReverse(List<V> list) {
        List<V> reversedList = new ArrayList<>(list);
        Collections.reverse(reversedList);

        return reversedList;
    }
}
