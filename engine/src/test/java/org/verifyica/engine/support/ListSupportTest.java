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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ListSupport Tests")
public class ListSupportTest {

    @Test
    @DisplayName("Should reverse list with multiple elements")
    public void shouldReverseListWithMultipleElements() {
        List<String> original = Arrays.asList("a", "b", "c", "d");
        List<String> reversed = ListSupport.copyAndReverse(original);

        assertThat(reversed).containsExactly("d", "c", "b", "a");
    }

    @Test
    @DisplayName("Should handle single element list")
    public void shouldHandleSingleElementList() {
        List<String> original = Collections.singletonList("a");
        List<String> reversed = ListSupport.copyAndReverse(original);

        assertThat(reversed).containsExactly("a");
    }

    @Test
    @DisplayName("Should handle empty list")
    public void shouldHandleEmptyList() {
        List<String> original = new ArrayList<>();
        List<String> reversed = ListSupport.copyAndReverse(original);

        assertThat(reversed).isEmpty();
    }

    @Test
    @DisplayName("Should not modify original list")
    public void shouldNotModifyOriginalList() {
        List<String> original = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> originalCopy = new ArrayList<>(original);

        List<String> reversed = ListSupport.copyAndReverse(original);

        assertThat(original).isEqualTo(originalCopy);
        assertThat(reversed).containsExactly("c", "b", "a");
    }

    @Test
    @DisplayName("Should return new list instance")
    public void shouldReturnNewListInstance() {
        List<String> original = Arrays.asList("a", "b", "c");
        List<String> reversed = ListSupport.copyAndReverse(original);

        assertThat(reversed).isNotSameAs(original);
    }

    @Test
    @DisplayName("Should handle list with duplicate elements")
    public void shouldHandleListWithDuplicateElements() {
        List<String> original = Arrays.asList("a", "a", "b", "b");
        List<String> reversed = ListSupport.copyAndReverse(original);

        assertThat(reversed).containsExactly("b", "b", "a", "a");
    }

    @Test
    @DisplayName("Should handle list with null elements")
    public void shouldHandleListWithNullElements() {
        List<String> original = Arrays.asList("a", null, "c");
        List<String> reversed = ListSupport.copyAndReverse(original);

        assertThat(reversed).containsExactly("c", null, "a");
    }

    @Test
    @DisplayName("Should handle integer list")
    public void shouldHandleIntegerList() {
        List<Integer> original = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> reversed = ListSupport.copyAndReverse(original);

        assertThat(reversed).containsExactly(5, 4, 3, 2, 1);
    }
}
