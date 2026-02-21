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
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("filter-factory-test");
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (tempDirectory != null && Files.exists(tempDirectory)) {
            Files.walk(tempDirectory).sorted((a, b) -> b.compareTo(a)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (final IOException e) {
                    // Ignore
                }
            });
        }
        ConcreteConfiguration.getInstance().getProperties().remove(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME);
    }

    @Nested
    @DisplayName("Load Filters Tests")
    public class LoadFiltersTests {

        @Test
        @DisplayName("Should return empty list when no filter file configured")
        public void shouldReturnEmptyListWhenNoFilterFileConfigured() {
            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).isNotNull();
            assertThat(filters).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when filter filename is empty")
        public void shouldReturnEmptyListWhenFilterFilenameIsEmpty() {
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, "");

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).isNotNull();
            assertThat(filters).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when filter filename is whitespace")
        public void shouldReturnEmptyListWhenFilterFilenameIsWhitespace() {
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, "   ");

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).isNotNull();
            assertThat(filters).isEmpty();
        }

        @Test
        @DisplayName("Should load IncludeClass filter")
        public void shouldLoadIncludeClassFilter() throws IOException {
            final String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \".*Test\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
            assertThat(filters.get(0)).isInstanceOf(IncludeClassFilter.class);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.INCLUDE_CLASS);
        }

        @Test
        @DisplayName("Should load ExcludeClass filter")
        public void shouldLoadExcludeClassFilter() throws IOException {
            final String yaml = "- type: ExcludeClass\n" + "  enabled: true\n" + "  classRegex: \".*Abstract.*\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
            assertThat(filters.get(0)).isInstanceOf(ExcludeClassFilter.class);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.EXCLUDE_CLASS);
        }

        @Test
        @DisplayName("Should load IncludeTaggedClass filter")
        public void shouldLoadIncludeTaggedClassFilter() throws IOException {
            final String yaml = "- type: IncludeTaggedClass\n" + "  enabled: true\n" + "  classTagRegex: \"fast\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
            assertThat(filters.get(0)).isInstanceOf(IncludeTaggedClassFilter.class);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.INCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("Should load ExcludeTaggedClass filter")
        public void shouldLoadExcludeTaggedClassFilter() throws IOException {
            final String yaml = "- type: ExcludeTaggedClass\n" + "  enabled: true\n" + "  classTagRegex: \"slow\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
            assertThat(filters.get(0)).isInstanceOf(ExcludeTaggedClassFilter.class);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.EXCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("Should load multiple filters")
        public void shouldLoadMultipleFilters() throws IOException {
            final String yaml = "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Test\"\n"
                    + "- type: ExcludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Abstract.*\"\n"
                    + "- type: IncludeTaggedClass\n"
                    + "  enabled: true\n"
                    + "  classTagRegex: \"fast\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(3);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.INCLUDE_CLASS);
            assertThat(filters.get(1).getType()).isEqualTo(Filter.Type.EXCLUDE_CLASS);
            assertThat(filters.get(2).getType()).isEqualTo(Filter.Type.INCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("Should skip disabled filters")
        public void shouldSkipDisabledFilters() throws IOException {
            final String yaml = "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Test\"\n"
                    + "- type: ExcludeClass\n"
                    + "  enabled: false\n"
                    + "  classRegex: \".*Abstract.*\"\n"
                    + "- type: IncludeTaggedClass\n"
                    + "  enabled: true\n"
                    + "  classTagRegex: \"fast\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(2);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.INCLUDE_CLASS);
            assertThat(filters.get(1).getType()).isEqualTo(Filter.Type.INCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("Should skip filters without enabled field")
        public void shouldSkipFiltersWithoutEnabledField() throws IOException {
            final String yaml = "- type: IncludeClass\n" + "  classRegex: \".*Test\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for empty YAML file")
        public void shouldReturnEmptyListForEmptyYamlFile() throws IOException {
            final String yaml = "";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            assertThatThrownBy(() -> FilterFactory.loadFilters()).isInstanceOf(EngineConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    public class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw exception for non-existent file")
        public void shouldThrowExceptionForNonExistentFile() {
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, "/nonexistent/file.yaml");

            assertThatThrownBy(() -> FilterFactory.loadFilters())
                    .isInstanceOf(EngineConfigurationException.class)
                    .hasMessageContaining("Exception loading filter definition file");
        }

        @Test
        @DisplayName("Should throw exception for invalid YAML")
        public void shouldThrowExceptionForInvalidYaml() throws IOException {
            final String yaml = "invalid: yaml: content: [unclosed";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            assertThatThrownBy(() -> FilterFactory.loadFilters())
                    .isInstanceOf(EngineConfigurationException.class)
                    .hasMessageContaining("Invalid filter definition file");
        }

        @Test
        @DisplayName("Should throw exception for unknown filter type")
        public void shouldThrowExceptionForUnknownFilterType() throws IOException {
            final String yaml = "- type: UnknownFilter\n" + "  enabled: true\n" + "  classRegex: \".*Test\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            assertThatThrownBy(() -> FilterFactory.loadFilters())
                    .isInstanceOf(EngineConfigurationException.class)
                    .hasMessageContaining("Invalid filter type");
        }

        @Test
        @DisplayName("Should throw exception for invalid regex in filter")
        public void shouldThrowExceptionForInvalidRegexInFilter() throws IOException {
            final String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \"[invalid\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            assertThatThrownBy(() -> FilterFactory.loadFilters()).isInstanceOf(EngineConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    public class EdgeCaseTests {

        @Test
        @DisplayName("Should handle complex regex patterns")
        public void shouldHandleComplexRegexPatterns() throws IOException {
            final String yaml = "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \"^org\\\\.verifyica\\\\.(.*Test|.*IT)$\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
        }

        @Test
        @DisplayName("Should handle YAML with comments")
        public void shouldHandleYamlWithComments() throws IOException {
            final String yaml = "# This is a comment\n"
                    + "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Test\"  # Include all test classes\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
        }

        @Test
        @DisplayName("Should handle filters with special characters in regex")
        public void shouldHandleFiltersWithSpecialCharactersInRegex() throws IOException {
            final String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \".*\\\\$Inner.*\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
        }

        @Test
        @DisplayName("Should handle empty regex")
        public void shouldHandleEmptyRegex() throws IOException {
            final String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \"\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    public class IntegrationTests {

        @Test
        @DisplayName("Should load all filter types in one file")
        public void shouldLoadAllFilterTypesInOneFile() throws IOException {
            final String yaml = "- type: IncludeClass\n"
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

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(4);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.INCLUDE_CLASS);
            assertThat(filters.get(1).getType()).isEqualTo(Filter.Type.EXCLUDE_CLASS);
            assertThat(filters.get(2).getType()).isEqualTo(Filter.Type.INCLUDE_TAGGED_CLASS);
            assertThat(filters.get(3).getType()).isEqualTo(Filter.Type.EXCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("Should preserve filter order")
        public void shouldPreserveFilterOrder() throws IOException {
            final String yaml = "- type: ExcludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Abstract.*\"\n"
                    + "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Test\"\n"
                    + "- type: ExcludeTaggedClass\n"
                    + "  enabled: true\n"
                    + "  classTagRegex: \"slow\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters = FilterFactory.loadFilters();
            assertThat(filters).hasSize(3);
            assertThat(filters.get(0).getType()).isEqualTo(Filter.Type.EXCLUDE_CLASS);
            assertThat(filters.get(1).getType()).isEqualTo(Filter.Type.INCLUDE_CLASS);
            assertThat(filters.get(2).getType()).isEqualTo(Filter.Type.EXCLUDE_TAGGED_CLASS);
        }

        @Test
        @DisplayName("Should be reusable across multiple calls")
        public void shouldBeReusableAcrossMultipleCalls() throws IOException {
            final String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \".*Test\"\n";

            final Path filterFile = createFilterFile(yaml);
            ConcreteConfiguration.getInstance()
                    .getProperties()
                    .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());

            final List<Filter> filters1 = FilterFactory.loadFilters();
            final List<Filter> filters2 = FilterFactory.loadFilters();

            assertThat(filters1).hasSize(1);
            assertThat(filters2).hasSize(1);
        }
    }

    private Path createFilterFile(final String content) throws IOException {
        final Path file = tempDirectory.resolve("filters.yaml");
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }
}
