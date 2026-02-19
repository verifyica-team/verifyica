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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("HierarchyTraversalMode Tests")
public class HierarchyTraversalModeTest {

    @Test
    @DisplayName("Should have TOP_DOWN value")
    public void shouldHaveTopDownValue() {
        assertThat(HierarchyTraversalMode.TOP_DOWN).isNotNull();
    }

    @Test
    @DisplayName("Should have BOTTOM_UP value")
    public void shouldHaveBottomUpValue() {
        assertThat(HierarchyTraversalMode.BOTTOM_UP).isNotNull();
    }

    @Test
    @DisplayName("Should decode TOP_DOWN to JUnit TOP_DOWN")
    public void shouldDecodeTopDownToJUnitTopDown() {
        org.junit.platform.commons.support.HierarchyTraversalMode result =
                HierarchyTraversalMode.decode(HierarchyTraversalMode.TOP_DOWN);

        assertThat(result).isEqualTo(org.junit.platform.commons.support.HierarchyTraversalMode.TOP_DOWN);
    }

    @Test
    @DisplayName("Should decode BOTTOM_UP to JUnit BOTTOM_UP")
    public void shouldDecodeBottomUpToJUnitBottomUp() {
        org.junit.platform.commons.support.HierarchyTraversalMode result =
                HierarchyTraversalMode.decode(HierarchyTraversalMode.BOTTOM_UP);

        assertThat(result).isEqualTo(org.junit.platform.commons.support.HierarchyTraversalMode.BOTTOM_UP);
    }

    @Test
    @DisplayName("Should throw exception for null input")
    public void shouldThrowExceptionForNullInput() {
        assertThatThrownBy(() -> HierarchyTraversalMode.decode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid hierarchyTraversalMode [null]");
    }

    @Test
    @DisplayName("Should have exactly two enum values")
    public void shouldHaveExactlyTwoEnumValues() {
        HierarchyTraversalMode[] values = HierarchyTraversalMode.values();

        assertThat(values).hasSize(2);
        assertThat(values).contains(HierarchyTraversalMode.TOP_DOWN, HierarchyTraversalMode.BOTTOM_UP);
    }

    @Test
    @DisplayName("Should return correct enum from valueOf")
    public void shouldReturnCorrectEnumFromValueOf() {
        assertThat(HierarchyTraversalMode.valueOf("TOP_DOWN")).isEqualTo(HierarchyTraversalMode.TOP_DOWN);
        assertThat(HierarchyTraversalMode.valueOf("BOTTOM_UP")).isEqualTo(HierarchyTraversalMode.BOTTOM_UP);
    }
}
