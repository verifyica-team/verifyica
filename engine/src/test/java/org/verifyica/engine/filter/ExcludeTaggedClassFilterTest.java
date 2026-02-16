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

@DisplayName("ExcludeTaggedClassFilter Tests")
public class ExcludeTaggedClassFilterTest {

    @Nested
    @DisplayName("Factory Method Tests")
    public class FactoryMethodTests {

        @Test
        @DisplayName("should create filter with valid regex")
        public void shouldCreateFilterWithValidRegex() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create(".*test.*");
            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("should throw exception for null regex")
        public void shouldThrowExceptionForNullRegex() {
            assertThatThrownBy(() -> ExcludeTaggedClassFilter.create(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw exception for invalid regex")
        public void shouldThrowExceptionForInvalidRegex() {
            assertThatThrownBy(() -> ExcludeTaggedClassFilter.create("[invalid"))
                    .isInstanceOf(PatternSyntaxException.class);
        }

        @Test
        @DisplayName("should create filter with empty string regex")
        public void shouldCreateFilterWithEmptyStringRegex() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("");
            assertThat(filter).isNotNull();
        }
    }

    @Nested
    @DisplayName("Type Tests")
    public class TypeTests {

        @Test
        @DisplayName("should return EXCLUDE_TAGGED_CLASS type")
        public void shouldReturnExcludeTaggedClassType() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create(".*");
            assertThat(filter.getType()).isEqualTo(Filter.Type.EXCLUDE_TAGGED_CLASS);
        }
    }

    @Nested
    @DisplayName("Matches Tests")
    public class MatchesTests {

        @Test
        @DisplayName("should match public class with single tag")
        public void shouldMatchClassWithSingleTag() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("slow");
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should match public class with multiple tags")
        public void shouldMatchClassWithMultipleTags() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("flaky");
            assertThat(filter.matches(MultipleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should not match untagged class")
        public void shouldNotMatchUntaggedClass() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("slow");
            assertThat(filter.matches(UntaggedClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match public class with wildcard pattern")
        public void shouldMatchClassWithWildcardPattern() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create(".*");
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
            assertThat(filter.matches(MultipleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should match public class with regex pattern")
        public void shouldMatchClassWithRegexPattern() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("slow|flaky");
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
            assertThat(filter.matches(FlakyTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should match public class with partial tag match")
        public void shouldMatchClassWithPartialTagMatch() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create(".*manual.*");
            assertThat(filter.matches(ManualTestTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should not match when tag pattern does not match")
        public void shouldNotMatchWhenTagPatternDoesNotMatch() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("database");
            assertThat(filter.matches(SingleTagClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match any tag in multiple tags")
        public void shouldMatchAnyTagInMultipleTags() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("flaky");
            assertThat(filter.matches(MultipleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should handle case-sensitive tag matching")
        public void shouldHandleCaseSensitiveTagMatching() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("Slow");
            assertThat(filter.matches(SingleTagClass.class)).isFalse();
        }

        @Test
        @DisplayName("should handle null public class gracefully")
        public void shouldHandleNullClassGracefully() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create(".*");
            assertThatThrownBy(() -> filter.matches(null)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should match public class with trimmed tag")
        public void shouldMatchClassWithTrimmedTag() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("trimmed");
            assertThat(filter.matches(TrimmedTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should not match public class with empty tag")
        public void shouldNotMatchClassWithEmptyTag() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create(".*");
            assertThat(filter.matches(EmptyTagClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match with exact pattern")
        public void shouldMatchWithExactPattern() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("^slow$");
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
            assertThat(filter.matches(SlowTestTagClass.class)).isFalse();
        }

        @Test
        @DisplayName("should match multiple classes with same tag")
        public void shouldMatchMultipleClassesWithSameTag() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("slow");
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
            assertThat(filter.matches(MultipleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should exclude integration tests")
        public void shouldExcludeIntegrationTests() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create(".*integration.*");
            assertThat(filter.matches(IntegrationTagClass.class)).isTrue();
            assertThat(filter.matches(SingleTagClass.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    public class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty string pattern")
        public void shouldHandleEmptyStringPattern() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("");
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should handle special regex characters in tag")
        public void shouldHandleSpecialRegexCharactersInTag() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("\\$special.*");
            assertThat(filter.matches(SpecialCharTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should handle tag with numbers")
        public void shouldHandleTagWithNumbers() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create(".*[0-9]+.*");
            assertThat(filter.matches(NumericTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should handle tag with hyphens")
        public void shouldHandleTagWithHyphens() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create(".*-.*");
            assertThat(filter.matches(HyphenTagClass.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    public class IntegrationTests {

        @Test
        @DisplayName("should work with multiple filters")
        public void shouldWorkWithMultipleFilters() {
            ExcludeTaggedClassFilter filter1 = ExcludeTaggedClassFilter.create("slow");
            ExcludeTaggedClassFilter filter2 = ExcludeTaggedClassFilter.create("flaky");

            assertThat(filter1.matches(SingleTagClass.class)).isTrue();
            assertThat(filter2.matches(SingleTagClass.class)).isFalse();
            assertThat(filter1.matches(MultipleTagClass.class)).isTrue();
            assertThat(filter2.matches(MultipleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should be reusable across multiple matches")
        public void shouldBeReusableAcrossMultipleMatches() {
            ExcludeTaggedClassFilter filter = ExcludeTaggedClassFilter.create("slow");

            assertThat(filter.matches(SingleTagClass.class)).isTrue();
            assertThat(filter.matches(MultipleTagClass.class)).isTrue();
            assertThat(filter.matches(SingleTagClass.class)).isTrue();
        }

        @Test
        @DisplayName("should work complementary to include filter")
        public void shouldWorkComplementaryToIncludeFilter() {
            ExcludeTaggedClassFilter excludeFilter = ExcludeTaggedClassFilter.create("slow");
            IncludeTaggedClassFilter includeFilter = IncludeTaggedClassFilter.create("fast");

            assertThat(excludeFilter.matches(SingleTagClass.class)).isTrue();
            assertThat(includeFilter.matches(SingleTagClass.class)).isFalse();

            assertThat(excludeFilter.matches(FastTagClass.class)).isFalse();
            assertThat(includeFilter.matches(FastTagClass.class)).isTrue();
        }
    }

    @Verifyica.Tag("slow")
    private static class SingleTagClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("slow")
    @Verifyica.Tag("flaky")
    private static class MultipleTagClass {
        // INTENTIONALLY EMPTY
    }

    private static class UntaggedClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("flaky")
    private static class FlakyTagClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("manual-test")
    private static class ManualTestTagClass {
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

    @Verifyica.Tag("slow-test")
    private static class SlowTestTagClass {
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

    @Verifyica.Tag("integration")
    private static class IntegrationTagClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("fast")
    private static class FastTagClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("end-to-end")
    private static class HyphenTagClass {
        // INTENTIONALLY EMPTY
    }
}
