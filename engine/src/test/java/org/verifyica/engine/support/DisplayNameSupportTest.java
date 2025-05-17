/*
 * Copyright (C) Verifyica project authors and contributors
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

import org.junit.jupiter.api.Test;
import org.verifyica.api.Verifyica;

public class DisplayNameSupportTest {

    @Test
    public void testDefaultDisplayName() {
        assertThat(DisplayNameSupport.getDisplayName(TestClass1.class)).isEqualTo(TestClass1.class.getName());
    }

    @Test
    public void testDisplayNameAnnotation() {
        assertThat(DisplayNameSupport.getDisplayName(TestClass2.class)).isEqualTo("CustomDisplayName");
    }

    private static class TestClass1 {
        // INTENTIONALLY BLANK
    }

    @Verifyica.DisplayName("CustomDisplayName")
    private static class TestClass2 {
        // INTENTIONALLY BLANK
    }
}
