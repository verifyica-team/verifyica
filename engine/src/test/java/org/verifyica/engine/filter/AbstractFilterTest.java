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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AbstractFilter Tests")
public class AbstractFilterTest {

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create filter with valid regex pattern")
        public void shouldCreateFilterWithValidRegexPattern() {
            TestFilter filter = new TestFilter(".*Test.*");
            assertThat(filter.getClassNamePattern()).isNotNull();
            assertThat(filter.getClassNamePattern().pattern()).isEqualTo(".*Test.*");
        }

        @Test
        @DisplayName("Should create filter with simple string pattern")
        public void shouldCreateFilterWithSimpleStringPattern() {
            TestFilter filter = new TestFilter("MyTest");
            assertThat(filter.getClassNamePattern()).isNotNull();
            assertThat(filter.getClassNamePattern().pattern()).isEqualTo("MyTest");
        }

        @Test
        @DisplayName("Should create filter with complex regex pattern")
        public void shouldCreateFilterWithComplexRegexPattern() {
            TestFilter filter = new TestFilter("^org\\.verifyica\\.(.*Test|.*IT)$");
            assertThat(filter.getClassNamePattern()).isNotNull();
            assertThat(filter.getClassNamePattern().pattern()).isEqualTo("^org\\.verifyica\\.(.*Test|.*IT)$");
        }

        @Test
        @DisplayName("Should create filter with wildcard pattern")
        public void shouldCreateFilterWithWildcardPattern() {
            TestFilter filter = new TestFilter(".*");
            assertThat(filter.getClassNamePattern()).isNotNull();
            assertThat(filter.getClassNamePattern().pattern()).isEqualTo(".*");
        }

        @Test
        @DisplayName("Should throw exception for null regex pattern")
        public void shouldThrowExceptionForNullRegexPattern() {
            assertThatThrownBy(() -> new TestFilter(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception for invalid regex pattern")
        public void shouldThrowExceptionForInvalidRegexPattern() {
            assertThatThrownBy(() -> new TestFilter("[invalid")).isInstanceOf(PatternSyntaxException.class);
        }

        @Test
        @DisplayName("Should create filter with empty string pattern")
        public void shouldCreateFilterWithEmptyStringPattern() {
            TestFilter filter = new TestFilter("");
            assertThat(filter.getClassNamePattern()).isNotNull();
            assertThat(filter.getClassNamePattern().pattern()).isEqualTo("");
        }

        @Test
        @DisplayName("Should create filter with special characters")
        public void shouldCreateFilterWithSpecialCharacters() {
            TestFilter filter = new TestFilter("\\$.*Test\\$$");
            assertThat(filter.getClassNamePattern()).isNotNull();
            assertThat(filter.getClassNamePattern().pattern()).isEqualTo("\\$.*Test\\$$");
        }
    }

    @Nested
    @DisplayName("Pattern Access Tests")
    public class PatternAccessTests {

        @Test
        @DisplayName("Should return same pattern instance on multiple calls")
        public void shouldReturnSamePatternInstanceOnMultipleCalls() {
            TestFilter filter = new TestFilter(".*Test.*");
            Pattern pattern1 = filter.getClassNamePattern();
            Pattern pattern2 = filter.getClassNamePattern();
            assertThat(pattern1).isSameAs(pattern2);
        }

        @Test
        @DisplayName("Should return immutable pattern")
        public void shouldReturnImmutablePattern() {
            TestFilter filter = new TestFilter(".*Test.*");
            Pattern pattern = filter.getClassNamePattern();
            assertThat(pattern).isNotNull();
            assertThat(pattern.pattern()).isEqualTo(".*Test.*");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    public class EdgeCaseTests {

        @Test
        @DisplayName("Should handle pattern with line breaks")
        public void shouldHandlePatternWithLineBreaks() {
            TestFilter filter = new TestFilter("Test(?s).*");
            assertThat(filter.getClassNamePattern()).isNotNull();
        }

        @Test
        @DisplayName("Should handle pattern with unicode characters")
        public void shouldHandlePatternWithUnicodeCharacters() {
            TestFilter filter = new TestFilter(".*\\u00E9.*");
            assertThat(filter.getClassNamePattern()).isNotNull();
            assertThat(filter.getClassNamePattern().pattern()).isEqualTo(".*\\u00E9.*");
        }

        @Test
        @DisplayName("Should handle case-sensitive patterns")
        public void shouldHandleCaseSensitivePatterns() {
            TestFilter filter = new TestFilter("Test");
            Pattern pattern = filter.getClassNamePattern();
            assertThat(pattern.matcher("Test").find()).isTrue();
            assertThat(pattern.matcher("test").find()).isFalse();
        }

        @Test
        @DisplayName("Should handle pattern with word boundaries")
        public void shouldHandlePatternWithWordBoundaries() {
            TestFilter filter = new TestFilter("\\bTest\\b");
            assertThat(filter.getClassNamePattern()).isNotNull();
            assertThat(filter.getClassNamePattern().pattern()).isEqualTo("\\bTest\\b");
        }
    }

    private static class TestFilter extends AbstractFilter {

        protected TestFilter(String classNameRegex) {
            super(classNameRegex);
        }

        @Override
        public Type getType() {
            return Type.UNKNOWN;
        }

        @Override
        public boolean matches(Class<?> testClass) {
            return false;
        }
    }
}
