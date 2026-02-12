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

@DisplayName("ExcludeClassFilter Tests")
public class ExcludeClassFilterTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create filter with valid regex")
        void shouldCreateFilterWithValidRegex() {
            ExcludeClassFilter filter = ExcludeClassFilter.create(".*Test");
            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("should throw exception for null regex")
        void shouldThrowExceptionForNullRegex() {
            assertThatThrownBy(() -> ExcludeClassFilter.create(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw exception for invalid regex")
        void shouldThrowExceptionForInvalidRegex() {
            assertThatThrownBy(() -> ExcludeClassFilter.create("[invalid")).isInstanceOf(PatternSyntaxException.class);
        }

        @Test
        @DisplayName("should create filter with empty string regex")
        void shouldCreateFilterWithEmptyStringRegex() {
            ExcludeClassFilter filter = ExcludeClassFilter.create("");
            assertThat(filter).isNotNull();
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("should return EXCLUDE_CLASS type")
        void shouldReturnExcludeClassType() {
            ExcludeClassFilter filter = ExcludeClassFilter.create(".*");
            assertThat(filter.getType()).isEqualTo(Filter.Type.EXCLUDE_CLASS);
        }
    }

    @Nested
    @DisplayName("Matches Tests")
    class MatchesTests {

        @Test
        @DisplayName("should match class with simple name pattern")
        void shouldMatchClassWithSimpleNamePattern() {
            // Use pattern that matches after '$' to target inner class simple names only
            ExcludeClassFilter filter = ExcludeClassFilter.create("\\$.*Test.*Class");
            assertThat(filter.matches(TestClass.class)).isTrue();
            assertThat(filter.matches(MyTestClass.class)).isTrue();
            assertThat(filter.matches(SampleClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match class with full package pattern")
        void shouldMatchClassWithFullPackagePattern() {
            ExcludeClassFilter filter = ExcludeClassFilter.create("org\\.verifyica\\.engine\\.filter\\..*");
            assertThat(filter.matches(TestClass.class)).isTrue();
            assertThat(filter.matches(String.class)).isFalse();
        }

        @Test
        @DisplayName("should match class with wildcard pattern")
        void shouldMatchClassWithWildcardPattern() {
            ExcludeClassFilter filter = ExcludeClassFilter.create(".*");
            assertThat(filter.matches(TestClass.class)).isTrue();
            assertThat(filter.matches(String.class)).isTrue();
            assertThat(filter.matches(Integer.class)).isTrue();
        }

        @Test
        @DisplayName("should match class with suffix pattern")
        void shouldMatchClassWithSuffixPattern() {
            ExcludeClassFilter filter = ExcludeClassFilter.create(".*Test$");
            assertThat(filter.matches(MyTest.class)).isTrue();
            assertThat(filter.matches(TestClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match class with prefix pattern")
        void shouldMatchClassWithPrefixPattern() {
            ExcludeClassFilter filter = ExcludeClassFilter.create("^org\\.verifyica\\..*");
            assertThat(filter.matches(TestClass.class)).isTrue();
            assertThat(filter.matches(String.class)).isFalse();
        }

        @Test
        @DisplayName("should not match class when pattern does not match")
        void shouldNotMatchClassWhenPatternDoesNotMatch() {
            ExcludeClassFilter filter = ExcludeClassFilter.create("^com\\.example\\..*");
            assertThat(filter.matches(TestClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match class with case-sensitive pattern")
        void shouldMatchClassWithCaseSensitivePattern() {
            ExcludeClassFilter filter = ExcludeClassFilter.create(".*TestClass");
            assertThat(filter.matches(MyTestClass.class)).isTrue();
            assertThat(filter.matches(MyTestclass.class)).isFalse();
        }

        @Test
        @DisplayName("should match class with OR pattern")
        void shouldMatchClassWithOrPattern() {
            // Pattern targets inner class simple names after '$' to avoid outer class name
            ExcludeClassFilter filter = ExcludeClassFilter.create("\\$(TestClass|MyIT|SampleClass)$");
            assertThat(filter.matches(TestClass.class)).isTrue();
            assertThat(filter.matches(MyIT.class)).isTrue();
            assertThat(filter.matches(SampleClass.class)).isTrue();
            assertThat(filter.matches(OtherClass.class)).isFalse();
        }

        @Test
        @DisplayName("should handle null class gracefully")
        void shouldHandleNullClassGracefully() {
            ExcludeClassFilter filter = ExcludeClassFilter.create(".*");
            assertThatThrownBy(() -> filter.matches(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should match nested class")
        void shouldMatchNestedClass() {
            ExcludeClassFilter filter = ExcludeClassFilter.create(".*\\$Nested.*");
            assertThat(filter.matches(NestedContainer.NestedClass.class)).isTrue();
        }

        @Test
        @DisplayName("should match with empty pattern")
        void shouldMatchWithEmptyPattern() {
            ExcludeClassFilter filter = ExcludeClassFilter.create("");
            assertThat(filter.matches(TestClass.class)).isTrue();
        }

        @Test
        @DisplayName("should match with character class pattern")
        void shouldMatchWithCharacterClassPattern() {
            // Pattern matches inner class with capital letter followed by "Test" in simple name
            ExcludeClassFilter filter = ExcludeClassFilter.create("\\$My.*Test.*");
            assertThat(filter.matches(MyTestClass.class)).isTrue();
        }

        @Test
        @DisplayName("should exclude abstract classes")
        void shouldExcludeAbstractClasses() {
            ExcludeClassFilter filter = ExcludeClassFilter.create(".*Abstract.*");
            assertThat(filter.matches(AbstractTestClass.class)).isTrue();
            assertThat(filter.matches(TestClass.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should work with multiple filters")
        void shouldWorkWithMultipleFilters() {
            // Use patterns that target inner class simple names to avoid outer class name
            ExcludeClassFilter filter1 = ExcludeClassFilter.create("\\$.*TestClass$");
            ExcludeClassFilter filter2 = ExcludeClassFilter.create("\\$.*IT$");

            assertThat(filter1.matches(TestClass.class)).isTrue();
            assertThat(filter2.matches(TestClass.class)).isFalse();
            assertThat(filter1.matches(MyIT.class)).isFalse();
            assertThat(filter2.matches(MyIT.class)).isTrue();
        }

        @Test
        @DisplayName("should be reusable across multiple matches")
        void shouldBeReusableAcrossMultipleMatches() {
            ExcludeClassFilter filter = ExcludeClassFilter.create(".*Test.*");

            assertThat(filter.matches(TestClass.class)).isTrue();
            assertThat(filter.matches(MyTestClass.class)).isTrue();
            assertThat(filter.matches(TestClass.class)).isTrue();
        }

        @Test
        @DisplayName("should work complementary to include filter")
        void shouldWorkComplementaryToIncludeFilter() {
            ExcludeClassFilter excludeFilter = ExcludeClassFilter.create(".*Abstract.*");
            IncludeClassFilter includeFilter = IncludeClassFilter.create(".*Test.*");

            assertThat(includeFilter.matches(TestClass.class)).isTrue();
            assertThat(excludeFilter.matches(TestClass.class)).isFalse();

            assertThat(includeFilter.matches(AbstractTestClass.class)).isTrue();
            assertThat(excludeFilter.matches(AbstractTestClass.class)).isTrue();
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

    private abstract static class AbstractTestClass {
        // INTENTIONALLY EMPTY
    }

    private static class NestedContainer {
        private static class NestedClass {
            // INTENTIONALLY EMPTY
        }
    }
}
