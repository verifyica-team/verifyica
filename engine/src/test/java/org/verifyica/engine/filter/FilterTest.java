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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Filter Interface Tests")
public class FilterTest {

    @Nested
    @DisplayName("Type Enum Tests")
    class TypeEnumTests {

        @Test
        @DisplayName("should have UNKNOWN type")
        void shouldHaveUnknownType() {
            assertThat(Filter.Type.UNKNOWN).isNotNull();
            assertThat(Filter.Type.UNKNOWN.name()).isEqualTo("UNKNOWN");
        }

        @Test
        @DisplayName("should have INCLUDE_CLASS type")
        void shouldHaveIncludeClassType() {
            assertThat(Filter.Type.INCLUDE_CLASS).isNotNull();
            assertThat(Filter.Type.INCLUDE_CLASS.name()).isEqualTo("INCLUDE_CLASS");
        }

        @Test
        @DisplayName("should have EXCLUDE_CLASS type")
        void shouldHaveExcludeClassType() {
            assertThat(Filter.Type.EXCLUDE_CLASS).isNotNull();
            assertThat(Filter.Type.EXCLUDE_CLASS.name()).isEqualTo("EXCLUDE_CLASS");
        }

        @Test
        @DisplayName("should have INCLUDE_TAGGED_CLASS type")
        void shouldHaveIncludeTaggedClassType() {
            assertThat(Filter.Type.INCLUDE_TAGGED_CLASS).isNotNull();
            assertThat(Filter.Type.INCLUDE_TAGGED_CLASS.name()).isEqualTo("INCLUDE_TAGGED_CLASS");
        }

        @Test
        @DisplayName("should have EXCLUDE_TAGGED_CLASS type")
        void shouldHaveExcludeTaggedClassType() {
            assertThat(Filter.Type.EXCLUDE_TAGGED_CLASS).isNotNull();
            assertThat(Filter.Type.EXCLUDE_TAGGED_CLASS.name()).isEqualTo("EXCLUDE_TAGGED_CLASS");
        }

        @Test
        @DisplayName("should have exactly 5 types")
        void shouldHaveExactlyFiveTypes() {
            assertThat(Filter.Type.values()).hasSize(5);
        }

        @Test
        @DisplayName("should support valueOf")
        void shouldSupportValueOf() {
            assertThat(Filter.Type.valueOf("UNKNOWN")).isEqualTo(Filter.Type.UNKNOWN);
            assertThat(Filter.Type.valueOf("INCLUDE_CLASS")).isEqualTo(Filter.Type.INCLUDE_CLASS);
            assertThat(Filter.Type.valueOf("EXCLUDE_CLASS")).isEqualTo(Filter.Type.EXCLUDE_CLASS);
            assertThat(Filter.Type.valueOf("INCLUDE_TAGGED_CLASS")).isEqualTo(Filter.Type.INCLUDE_TAGGED_CLASS);
            assertThat(Filter.Type.valueOf("EXCLUDE_TAGGED_CLASS")).isEqualTo(Filter.Type.EXCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("should have proper ordinal values")
        void shouldHaveProperOrdinalValues() {
            assertThat(Filter.Type.UNKNOWN.ordinal()).isEqualTo(0);
            assertThat(Filter.Type.INCLUDE_CLASS.ordinal()).isEqualTo(1);
            assertThat(Filter.Type.EXCLUDE_CLASS.ordinal()).isEqualTo(2);
            assertThat(Filter.Type.INCLUDE_TAGGED_CLASS.ordinal()).isEqualTo(3);
            assertThat(Filter.Type.EXCLUDE_TAGGED_CLASS.ordinal()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Filter Contract Tests")
    class FilterContractTests {

        @Test
        @DisplayName("should return correct type for IncludeClassFilter")
        void shouldReturnCorrectTypeForIncludeClassFilter() {
            Filter filter = IncludeClassFilter.create(".*");
            assertThat(filter.getType()).isEqualTo(Filter.Type.INCLUDE_CLASS);
        }

        @Test
        @DisplayName("should return correct type for ExcludeClassFilter")
        void shouldReturnCorrectTypeForExcludeClassFilter() {
            Filter filter = ExcludeClassFilter.create(".*");
            assertThat(filter.getType()).isEqualTo(Filter.Type.EXCLUDE_CLASS);
        }

        @Test
        @DisplayName("should return correct type for IncludeTaggedClassFilter")
        void shouldReturnCorrectTypeForIncludeTaggedClassFilter() {
            Filter filter = IncludeTaggedClassFilter.create(".*");
            assertThat(filter.getType()).isEqualTo(Filter.Type.INCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("should return correct type for ExcludeTaggedClassFilter")
        void shouldReturnCorrectTypeForExcludeTaggedClassFilter() {
            Filter filter = ExcludeTaggedClassFilter.create(".*");
            assertThat(filter.getType()).isEqualTo(Filter.Type.EXCLUDE_TAGGED_CLASS);
        }
    }

    @Nested
    @DisplayName("Enum Comparison Tests")
    class EnumComparisonTests {

        @Test
        @DisplayName("should support equality comparison")
        void shouldSupportEqualityComparison() {
            assertThat(Filter.Type.UNKNOWN).isEqualTo(Filter.Type.UNKNOWN);
            assertThat(Filter.Type.INCLUDE_CLASS).isEqualTo(Filter.Type.INCLUDE_CLASS);
            assertThat(Filter.Type.UNKNOWN).isNotEqualTo(Filter.Type.INCLUDE_CLASS);
        }

        @Test
        @DisplayName("should support identity comparison")
        void shouldSupportIdentityComparison() {
            assertThat(Filter.Type.UNKNOWN == Filter.Type.UNKNOWN).isTrue();
            assertThat(Filter.Type.INCLUDE_CLASS == Filter.Type.INCLUDE_CLASS).isTrue();
            assertThat(Filter.Type.UNKNOWN == Filter.Type.INCLUDE_CLASS).isFalse();
        }

        @Test
        @DisplayName("should support switch statements")
        void shouldSupportSwitchStatements() {
            Filter.Type type = Filter.Type.INCLUDE_CLASS;
            String result;
            switch (type) {
                case UNKNOWN:
                    result = "unknown";
                    break;
                case INCLUDE_CLASS:
                    result = "include";
                    break;
                case EXCLUDE_CLASS:
                    result = "exclude";
                    break;
                case INCLUDE_TAGGED_CLASS:
                    result = "include_tagged";
                    break;
                case EXCLUDE_TAGGED_CLASS:
                    result = "exclude_tagged";
                    break;
                default:
                    result = "unknown";
                    break;
            }
            assertThat(result).isEqualTo("include");
        }
    }
}
