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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.exception.EngineConfigurationException;

@DisplayName("FilterFactory Tests")
public class FilterFactoryTest {

    private Path tempDirectory;

    @BeforeEach
    void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("filter-factory-test");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDirectory != null && Files.exists(tempDirectory)) {
            Files.walk(tempDirectory).sorted((a, b) -> b.compareTo(a)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    // Ignore
                }
            });
        }
        ConcreteConfiguration.getInstance().getProperties().remove(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME);
    }

    @Nested
    @DisplayName("Load Filters Tests")
    class LoadFiltersTests {

        @Test
        @DisplayName("should return empty list when no filter file configured")
        void shouldReturnEmptyListWhenNoFilterFileConfigured() {
            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).isNotNull();
            assertThat(filters).isEmpty();
        }

        @Test
        @DisplayName("should return empty list when filter filename is empty")
        void shouldReturnEmptyListWhenFilterFilenameIsEmpty() {
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, "");

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).isNotNull();
            assertThat(filters).isEmpty();
        }

        @Test
        @DisplayName("should return empty list when filter filename is whitespace")
        void shouldReturnEmptyListWhenFilterFilenameIsWhitespace() {
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, "   ");

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).isNotNull();
            assertThat(filters).isEmpty();
        }

        @Test
        @DisplayName("should load IncludeClass filter")
        void shouldLoadIncludeClassFilter() throws IOException {
            String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \".*Test\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
            assertThat(filters.get(0)).isInstanceOf(IncludeClassFilter.class);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.INCLUDE_CLASS);
        }

        @Test
        @DisplayName("should load ExcludeClass filter")
        void shouldLoadExcludeClassFilter() throws IOException {
            String yaml = "- type: ExcludeClass\n" + "  enabled: true\n" + "  classRegex: \".*Abstract.*\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
            assertThat(filters.get(0)).isInstanceOf(ExcludeClassFilter.class);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.EXCLUDE_CLASS);
        }

        @Test
        @DisplayName("should load IncludeTaggedClass filter")
        void shouldLoadIncludeTaggedClassFilter() throws IOException {
            String yaml = "- type: IncludeTaggedClass\n" + "  enabled: true\n" + "  classTagRegex: \"fast\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
            assertThat(filters.get(0)).isInstanceOf(IncludeTaggedClassFilter.class);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.INCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("should load ExcludeTaggedClass filter")
        void shouldLoadExcludeTaggedClassFilter() throws IOException {
            String yaml = "- type: ExcludeTaggedClass\n" + "  enabled: true\n" + "  classTagRegex: \"slow\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
            assertThat(filters.get(0)).isInstanceOf(ExcludeTaggedClassFilter.class);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.EXCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("should load multiple filters")
        void shouldLoadMultipleFilters() throws IOException {
            String yaml = "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Test\"\n"
                    + "- type: ExcludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Abstract.*\"\n"
                    + "- type: IncludeTaggedClass\n"
                    + "  enabled: true\n"
                    + "  classTagRegex: \"fast\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(3);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.INCLUDE_CLASS);
            assertThat(filters.get(1).getType()).isEqualTo(Filter.Type.EXCLUDE_CLASS);
            assertThat(filters.get(2).getType()).isEqualTo(Filter.Type.INCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("should skip disabled filters")
        void shouldSkipDisabledFilters() throws IOException {
            String yaml = "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Test\"\n"
                    + "- type: ExcludeClass\n"
                    + "  enabled: false\n"
                    + "  classRegex: \".*Abstract.*\"\n"
                    + "- type: IncludeTaggedClass\n"
                    + "  enabled: true\n"
                    + "  classTagRegex: \"fast\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(2);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.INCLUDE_CLASS);
            assertThat(filters.get(1).getType()).isEqualTo(Filter.Type.INCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("should skip filters without enabled field")
        void shouldSkipFiltersWithoutEnabledField() throws IOException {
            String yaml = "- type: IncludeClass\n" + "  classRegex: \".*Test\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).isEmpty();
        }

        @Test
        @DisplayName("should return empty list for empty YAML file")
        void shouldReturnEmptyListForEmptyYamlFile() throws IOException {
            String yaml = "";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            assertThatThrownBy(() -> FilterFactory.loadFilters()).isInstanceOf(EngineConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should throw exception for non-existent file")
        void shouldThrowExceptionForNonExistentFile() {
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, "/nonexistent/file.yaml");

            assertThatThrownBy(() -> FilterFactory.loadFilters())
                    .isInstanceOf(EngineConfigurationException.class)
                    .hasMessageContaining("Exception loading filter definition file");
        }

        @Test
        @DisplayName("should throw exception for invalid YAML")
        void shouldThrowExceptionForInvalidYaml() throws IOException {
            String yaml = "invalid: yaml: content: [unclosed";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            assertThatThrownBy(() -> FilterFactory.loadFilters())
                    .isInstanceOf(EngineConfigurationException.class)
                    .hasMessageContaining("Invalid filter definition file");
        }

        @Test
        @DisplayName("should throw exception for unknown filter type")
        void shouldThrowExceptionForUnknownFilterType() throws IOException {
            String yaml = "- type: UnknownFilter\n" + "  enabled: true\n" + "  classRegex: \".*Test\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            assertThatThrownBy(() -> FilterFactory.loadFilters())
                    .isInstanceOf(EngineConfigurationException.class)
                    .hasMessageContaining("Invalid filter type");
        }

        @Test
        @DisplayName("should throw exception for invalid regex in filter")
        void shouldThrowExceptionForInvalidRegexInFilter() throws IOException {
            String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \"[invalid\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            assertThatThrownBy(() -> FilterFactory.loadFilters()).isInstanceOf(EngineConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle complex regex patterns")
        void shouldHandleComplexRegexPatterns() throws IOException {
            String yaml = "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \"^org\\\\.verifyica\\\\.(.*Test|.*IT)$\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
        }

        @Test
        @DisplayName("should handle YAML with comments")
        void shouldHandleYamlWithComments() throws IOException {
            String yaml = "# This is a comment\n"
                    + "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Test\"  # Include all test classes\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
        }

        @Test
        @DisplayName("should handle filters with special characters in regex")
        void shouldHandleFiltersWithSpecialCharactersInRegex() throws IOException {
            String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \".*\\\\$Inner.*\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
        }

        @Test
        @DisplayName("should handle empty regex")
        void shouldHandleEmptyRegex() throws IOException {
            String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \"\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should load all filter types in one file")
        void shouldLoadAllFilterTypesInOneFile() throws IOException {
            String yaml = "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Test\"\n"
                    + "- type: ExcludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Abstract.*\"\n"
                    + "- type: IncludeTaggedClass\n"
                    + "  enabled: true\n"
                    + "  classTagRegex: \"fast\"\n"
                    + "- type: ExcludeTaggedClass\n"
                    + "  enabled: true\n"
                    + "  classTagRegex: \"slow\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(4);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.INCLUDE_CLASS);
            assertThat(filters.get(1).getType()).isEqualTo(Filter.Type.EXCLUDE_CLASS);
            assertThat(filters.get(2).getType()).isEqualTo(Filter.Type.INCLUDE_TAGGED_CLASS);
            assertThat(filters.get(3).getType()).isEqualTo(Filter.Type.EXCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("should preserve filter order")
        void shouldPreserveFilterOrder() throws IOException {
            String yaml = "- type: ExcludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Abstract.*\"\n"
                    + "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Test\"\n"
                    + "- type: ExcludeTaggedClass\n"
                    + "  enabled: true\n"
                    + "  classTagRegex: \"slow\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(3);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.EXCLUDE_CLASS);
            assertThat(filters.get(1).getType()).isEqualTo(Filter.Type.INCLUDE_CLASS);
            assertThat(filters.get(2).getType()).isEqualTo(Filter.Type.EXCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("should be reusable across multiple calls")
        void shouldBeReusableAcrossMultipleCalls() throws IOException {
            String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \".*Test\"\n";

            Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            List<Filter> filters1 = FilterFactory.loadFilters();
            List<Filter> filters2 = FilterFactory.loadFilters();

            assertThat(filters1).hasSize(1);
            assertThat(filters2).hasSize(1);
        }
    }

    private Path createFilterFile(String content) throws IOException {
        Path file = tempDirectory.resolve("filters.yaml");
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }
}
