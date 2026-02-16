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
import org.verifyica.api.Verifyica;

@DisplayName("IncludeTaggedClassFilter Tests")
public class IncludeTaggedClassFilterTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create filter with valid regex")
        void shouldCreateFilterWithValidRegex() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create(".*test.*");
            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("should throw exception for null regex")
        void shouldThrowExceptionForNullRegex() {
            assertThatThrownBy(() -> IncludeTaggedClassFilter.create(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw exception for invalid regex")
        void shouldThrowExceptionForInvalidRegex() {
            assertThatThrownBy(() -> IncludeTaggedClassFilter.create("[invalid"))
                    .isInstanceOf(PatternSyntaxException.class);
        }

        @Test
        @DisplayName("should create filter with empty string regex")
        void shouldCreateFilterWithEmptyStringRegex() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("");
            assertThat(filter).isNotNull();
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("should return INCLUDE_TAGGED_CLASS type")
        void shouldReturnIncludeTaggedClassType() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create(".*");
            assertThat(filter.getType()).isEqualTo(Filter.Type.INCLUDE_TAGGED_CLASS);
        }
    }

    @Nested
    @DisplayName("Matches Tests")
    class MatchesTests {

        @Test
        @DisplayName("should match class with single tag")
        void shouldMatchClassWithSingleTag() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("fast");
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should match class with multiple tags")
        void shouldMatchClassWithMultipleTags() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("integration");
            assertThat(filter.matches(MultipleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should not match untagged class")
        void shouldNotMatchUntaggedClass() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("fast");
            assertThat(filter.matches(UntaggedClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match class with wildcard pattern")
        void shouldMatchClassWithWildcardPattern() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create(".*");
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
            assertThat(filter.matches(MultipleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should match class with regex pattern")
        void shouldMatchClassWithRegexPattern() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("fast|slow");
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
            assertThat(filter.matches(SlowTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should match class with partial tag match")
        void shouldMatchClassWithPartialTagMatch() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create(".*test.*");
            assertThat(filter.matches(IntegrationTestTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should not match when tag pattern does not match")
        void shouldNotMatchWhenTagPatternDoesNotMatch() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("database");
            assertThat(filter.matches(SingleTagClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match any tag in multiple tags")
        void shouldMatchAnyTagInMultipleTags() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("integration");
            assertThat(filter.matches(MultipleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should handle case-sensitive tag matching")
        void shouldHandleCaseSensitiveTagMatching() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("Fast");
            assertThat(filter.matches(SingleTagClass.class)).isFalse();
        }

        @Test
        @DisplayName("should handle null class gracefully")
        void shouldHandleNullClassGracefully() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create(".*");
            assertThatThrownBy(() -> filter.matches(null)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should match class with trimmed tag")
        void shouldMatchClassWithTrimmedTag() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("trimmed");
            assertThat(filter.matches(TrimmedTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should not match class with empty tag")
        void shouldNotMatchClassWithEmptyTag() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create(".*");
            assertThat(filter.matches(EmptyTagClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match with prefix pattern")
        void shouldMatchWithPrefixPattern() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("^fast$");
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
            assertThat(filter.matches(FastTestTagClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match multiple classes with same tag")
        void shouldMatchMultipleClassesWithSameTag() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("fast");
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
            assertThat(filter.matches(MultipleTagClass.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty string pattern")
        void shouldHandleEmptyStringPattern() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("");
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should handle special regex characters in tag")
        void shouldHandleSpecialRegexCharactersInTag() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("\\$special.*");
            assertThat(filter.matches(SpecialCharTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should handle tag with numbers")
        void shouldHandleTagWithNumbers() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create(".*[0-9]+.*");
            assertThat(filter.matches(NumericTagClass.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should work with multiple filters")
        void shouldWorkWithMultipleFilters() {
            IncludeTaggedClassFilter filter1 = IncludeTaggedClassFilter.create("fast");
            IncludeTaggedClassFilter filter2 = IncludeTaggedClassFilter.create("integration");

            assertThat(filter1.matches(SingleTagClass.class)).isTrue();
            assertThat(filter2.matches(SingleTagClass.class)).isFalse();
            assertThat(filter1.matches(MultipleTagClass.class)).isTrue();
            assertThat(filter2.matches(MultipleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should be reusable across multiple matches")
        void shouldBeReusableAcrossMultipleMatches() {
            IncludeTaggedClassFilter filter = IncludeTaggedClassFilter.create("fast");

            assertThat(filter.matches(SingleTagClass.class)).isTrue();
            assertThat(filter.matches(MultipleTagClass.class)).isTrue();
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
        }
    }

    @Verifyica.Tag("fast")
    private static class SingleTagClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("fast")
    @Verifyica.Tag("integration")
    private static class MultipleTagClass {
        // INTENTIONALLY EMPTY
    }

    private static class UntaggedClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("slow")
    private static class SlowTagClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("integration-test")
    private static class IntegrationTestTagClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("  trimmed  ")
    private static class TrimmedTagClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("")
    private static class EmptyTagClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("fast-test")
    private static class FastTestTagClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("$special-tag")
    private static class SpecialCharTagClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("test123")
    private static class NumericTagClass {
        // INTENTIONALLY EMPTY
    }
}
