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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.verifyica.api.Verifyica;

@DisplayName("DisplayNameSupport Tests")
public class DisplayNameSupportTest {

    @Test
    @DisplayName("Should return class name when no @DisplayName annotation is present")
    public void testDefaultDisplayName() {
        assertThat(DisplayNameSupport.getDisplayName(TestClass1.class)).isEqualTo(TestClass1.class.getName());
    }

    @Test
    @DisplayName("Should return custom display name when @DisplayName annotation is present")
    public void testDisplayNameAnnotation() {
        assertThat(DisplayNameSupport.getDisplayName(TestClass2.class)).isEqualTo("CustomDisplayName");
    }

    private static class TestClass1 {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.DisplayName("CustomDisplayName")
    private static class TestClass2 {
        // INTENTIONALLY EMPTY
    }
}
