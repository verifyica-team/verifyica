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

package org.verifyica.engine.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.regex.PatternSyntaxException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("IncludeClassFilter Tests")
public class IncludeClassFilterTest {

    @Nested
    @DisplayName("Factory Method Tests")
    public class FactoryMethodTests {

        @Test
        @DisplayName("should create filter with valid regex")
        public void shouldCreateFilterWithValidRegex() {
            IncludeClassFilter filter = IncludeClassFilter.create(".*Test");
            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("should throw exception for null regex")
        public void shouldThrowExceptionForNullRegex() {
            assertThatThrownBy(() -> IncludeClassFilter.create(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw exception for invalid regex")
        public void shouldThrowExceptionForInvalidRegex() {
            assertThatThrownBy(() -> IncludeClassFilter.create("[invalid")).isInstanceOf(PatternSyntaxException.class);
        }

        @Test
        @DisplayName("should create filter with empty string regex")
        public void shouldCreateFilterWithEmptyStringRegex() {
            IncludeClassFilter filter = IncludeClassFilter.create("");
            assertThat(filter).isNotNull();
        }
    }

    @Nested
    @DisplayName("Type Tests")
    public class TypeTests {

        @Test
        @DisplayName("should return INCLUDE_CLASS type")
        public void shouldReturnIncludeClassType() {
            IncludeClassFilter filter = IncludeClassFilter.create(".*");
            assertThat(filter.getType()).isEqualTo(Filter.Type.INCLUDE_CLASS);
        }
    }

    @Nested
    @DisplayName("Matches Tests")
    public class MatchesTests {

        @Test
        @DisplayName("should match public class with simple name pattern")
        public void shouldMatchClassWithSimpleNamePattern() {
            // Use pattern that matches after '$' to target inner public class simple names only
            IncludeClassFilter filter = IncludeClassFilter.create("\\$.*Test.*Class");
            assertThat(filter.matches(TestClass.class)).isTrue();
            assertThat(filter.matches(MyTestClass.class)).isTrue();
            assertThat(filter.matches(SampleClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match public class with full package pattern")
        public void shouldMatchClassWithFullPackagePattern() {
            IncludeClassFilter filter = IncludeClassFilter.create("org\\.verifyica\\.engine\\.filter\\..*");
            assertThat(filter.matches(TestClass.class)).isTrue();
            assertThat(filter.matches(String.class)).isFalse();
        }

        @Test
        @DisplayName("should match public class with wildcard pattern")
        public void shouldMatchClassWithWildcardPattern() {
            IncludeClassFilter filter = IncludeClassFilter.create(".*");
            assertThat(filter.matches(TestClass.class)).isTrue();
            assertThat(filter.matches(String.class)).isTrue();
            assertThat(filter.matches(Integer.class)).isTrue();
        }

        @Test
        @DisplayName("should match public class with suffix pattern")
        public void shouldMatchClassWithSuffixPattern() {
            IncludeClassFilter filter = IncludeClassFilter.create(".*Test$");
            assertThat(filter.matches(MyTest.class)).isTrue();
            assertThat(filter.matches(TestClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match public class with prefix pattern")
        public void shouldMatchClassWithPrefixPattern() {
            IncludeClassFilter filter = IncludeClassFilter.create("^org\\.verifyica\\..*");
            assertThat(filter.matches(TestClass.class)).isTrue();
            assertThat(filter.matches(String.class)).isFalse();
        }

        @Test
        @DisplayName("should not match public class when pattern does not match")
        public void shouldNotMatchClassWhenPatternDoesNotMatch() {
            IncludeClassFilter filter = IncludeClassFilter.create("^com\\.example\\..*");
            assertThat(filter.matches(TestClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match public class with case-sensitive pattern")
        public void shouldMatchClassWithCaseSensitivePattern() {
            IncludeClassFilter filter = IncludeClassFilter.create(".*TestClass");
            assertThat(filter.matches(MyTestClass.class)).isTrue();
            assertThat(filter.matches(MyTestclass.class)).isFalse();
        }

        @Test
        @DisplayName("should match public class with OR pattern")
        public void shouldMatchClassWithOrPattern() {
            // Pattern targets inner public class simple names after '$' to apublic void outer public class name
            IncludeClassFilter filter = IncludeClassFilter.create("\\$(TestClass|MyIT|SampleClass)$");
            assertThat(filter.matches(TestClass.class)).isTrue();
            assertThat(filter.matches(MyIT.class)).isTrue();
            assertThat(filter.matches(SampleClass.class)).isTrue();
            assertThat(filter.matches(OtherClass.class)).isFalse();
        }

        @Test
        @DisplayName("should handle null public class gracefully")
        public void shouldHandleNullClassGracefully() {
            IncludeClassFilter filter = IncludeClassFilter.create(".*");
            assertThatThrownBy(() -> filter.matches(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should match nested class")
        public void shouldMatchNestedClass() {
            IncludeClassFilter filter = IncludeClassFilter.create(".*\\$Nested.*");
            assertThat(filter.matches(NestedContainer.NestedClass.class)).isTrue();
        }

        @Test
        @DisplayName("should match with empty pattern")
        public void shouldMatchWithEmptyPattern() {
            IncludeClassFilter filter = IncludeClassFilter.create("");
            assertThat(filter.matches(TestClass.class)).isTrue();
        }

        @Test
        @DisplayName("should match with character public class pattern")
        public void shouldMatchWithCharacterClassPattern() {
            // Pattern matches inner public class with capital letter followed by "Test" in simple name
            IncludeClassFilter filter = IncludeClassFilter.create("\\$My.*Test.*");
            assertThat(filter.matches(MyTestClass.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    public class IntegrationTests {

        @Test
        @DisplayName("should work with multiple filters")
        public void shouldWorkWithMultipleFilters() {
            // Use patterns that target inner public class simple names to apublic void outer public class name
            IncludeClassFilter filter1 = IncludeClassFilter.create("\\$.*TestClass$");
            IncludeClassFilter filter2 = IncludeClassFilter.create("\\$.*IT$");

            assertThat(filter1.matches(TestClass.class)).isTrue();
            assertThat(filter2.matches(TestClass.class)).isFalse();
            assertThat(filter1.matches(MyIT.class)).isFalse();
            assertThat(filter2.matches(MyIT.class)).isTrue();
        }

        @Test
        @DisplayName("should be reusable across multiple matches")
        public void shouldBeReusableAcrossMultipleMatches() {
            IncludeClassFilter filter = IncludeClassFilter.create(".*Test.*");

            assertThat(filter.matches(TestClass.class)).isTrue();
            assertThat(filter.matches(MyTestClass.class)).isTrue();
            assertThat(filter.matches(TestClass.class)).isTrue();
        }
    }

    private static class TestClass {
        // INTENTIONALLY EMPTY
    }

    private static class MyTestClass {
        // INTENTIONALLY EMPTY
    }

    private static class SampleClass {
        // INTENTIONALLY EMPTY
    }

    private static class MyTest {
        // INTENTIONALLY EMPTY
    }

    private static class MyTestclass {
        // INTENTIONALLY EMPTY
    }

    private static class MyIT {
        // INTENTIONALLY EMPTY
    }

    private static class OtherClass {
        // INTENTIONALLY EMPTY
    }

    private static class NestedContainer {

        private static class NestedClass {
            // INTENTIONALLY EMPTY
        }
    }
}
