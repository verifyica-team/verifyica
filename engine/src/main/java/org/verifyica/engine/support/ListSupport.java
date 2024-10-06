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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Class to implement ListSupport */
public class ListSupport {

    /** Constructor */
    private ListSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to copy and reverse a List
     *
     * @param list list
     * @return a reversed copy of the List
     * @param <V> the type
     */
    public static <V> List<V> reversedCopy(List<V> list) {
        List<V> reversedList = new ArrayList<>(list);
        Collections.reverse(reversedList);
        return reversedList;
    }
}