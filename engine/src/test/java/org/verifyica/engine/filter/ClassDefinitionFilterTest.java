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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.api.ClassDefinition;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.Constants;

@DisplayName("ClassDefinitionFilter Tests")
public class ClassDefinitionFilterTest {

    private Path tempDirectory;

    @BeforeEach
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("class-definition-filter-test");
    }

    @AfterEach
    public void tearDown() throws IOException {
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
    @DisplayName("Filter Method Tests")
    public class FilterMethodTests {

        @Test
        @DisplayName("Should not modify list when no filters configured")
        public void shouldNotModifyListWhenNoFiltersConfigured() {
            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(TestClass1.class));
            classDefinitions.add(createClassDefinition(TestClass2.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(2);
        }

        @Test
        @DisplayName("Should handle empty list")
        public void shouldHandleEmptyList() {
            List<ClassDefinition> classDefinitions = new ArrayList<>();

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).isEmpty();
        }

        @Test
        @DisplayName("Should exclude classes matching ExcludeClass filter")
        public void shouldExcludeClassesMatchingExcludeClassFilter() throws IOException {
            String yaml = "- type: ExcludeClass\n" + "  enabled: true\n" + "  classRegex: \".*Abstract.*\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(TestClass1.class));
            classDefinitions.add(createClassDefinition(AbstractTestClass.class));
            classDefinitions.add(createClassDefinition(TestClass2.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(2);
            assertThat(classDefinitions)
                    .extracting(cd -> cd.getTestClass().getSimpleName())
                    .containsExactly("TestClass1", "TestClass2");
        }

        @Test
        @DisplayName("Should include only classes matching IncludeClass filter")
        public void shouldIncludeOnlyClassesMatchingIncludeClassFilter() throws IOException {
            String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \".*TestClass1\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(TestClass1.class));
            classDefinitions.add(createClassDefinition(TestClass2.class));
            classDefinitions.add(createClassDefinition(OtherClass.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(1);
            assertThat(classDefinitions.get(0).getTestClass()).isEqualTo(TestClass1.class);
        }

        @Test
        @DisplayName("Should exclude classes matching ExcludeTaggedClass filter")
        public void shouldExcludeClassesMatchingExcludeTaggedClassFilter() throws IOException {
            String yaml = "- type: ExcludeTaggedClass\n" + "  enabled: true\n" + "  classTagRegex: \"slow\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(FastClass.class));
            classDefinitions.add(createClassDefinition(SlowClass.class));
            classDefinitions.add(createClassDefinition(TestClass1.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(2);
            assertThat(classDefinitions)
                    .extracting(cd -> cd.getTestClass().getSimpleName())
                    .containsExactlyInAnyOrder("FastClass", "TestClass1");
        }

        @Test
        @DisplayName("Should include only classes matching IncludeTaggedClass filter")
        public void shouldIncludeOnlyClassesMatchingIncludeTaggedClassFilter() throws IOException {
            String yaml = "- type: IncludeTaggedClass\n" + "  enabled: true\n" + "  classTagRegex: \"fast\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(FastClass.class));
            classDefinitions.add(createClassDefinition(SlowClass.class));
            classDefinitions.add(createClassDefinition(TestClass1.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(1);
            assertThat(classDefinitions.get(0).getTestClass()).isEqualTo(FastClass.class);
        }

        @Test
        @DisplayName("Should apply multiple filters")
        public void shouldApplyMultipleFilters() throws IOException {
            String yaml = "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \"\\\\$.*Test.*\"\n"
                    + "- type: ExcludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Abstract.*\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(TestClass1.class));
            classDefinitions.add(createClassDefinition(AbstractTestClass.class));
            classDefinitions.add(createClassDefinition(TestClass2.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(2);
            assertThat(classDefinitions.get(0).getTestClass()).isEqualTo(TestClass1.class);
            assertThat(classDefinitions.get(1).getTestClass()).isEqualTo(TestClass2.class);
        }

        @Test
        @DisplayName("Should remove all when exclude filter matches all")
        public void shouldRemoveAllWhenExcludeFilterMatchesAll() throws IOException {
            String yaml = "- type: ExcludeClass\n" + "  enabled: true\n" + "  classRegex: \".*\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(TestClass1.class));
            classDefinitions.add(createClassDefinition(TestClass2.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).isEmpty();
        }

        @Test
        @DisplayName("Should keep all when include filter matches all")
        public void shouldKeepAllWhenIncludeFilterMatchesAll() throws IOException {
            String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \".*\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(TestClass1.class));
            classDefinitions.add(createClassDefinition(TestClass2.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Filter Combination Tests")
    public class FilterCombinationTests {

        @Test
        @DisplayName("Should apply include and exclude class filters together")
        public void shouldApplyIncludeAndExcludeClassFiltersTogether() throws IOException {
            String yaml = "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \"\\\\$.*Test.*\"\n"
                    + "- type: ExcludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*2\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(TestClass1.class));
            classDefinitions.add(createClassDefinition(TestClass2.class));
            classDefinitions.add(createClassDefinition(OtherClass.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(1);
            assertThat(classDefinitions.get(0).getTestClass()).isEqualTo(TestClass1.class);
        }

        @Test
        @DisplayName("Should apply include and exclude tagged filters together")
        public void shouldApplyIncludeAndExcludeTaggedFiltersTogether() throws IOException {
            String yaml = "- type: IncludeTaggedClass\n"
                    + "  enabled: true\n"
                    + "  classTagRegex: \".*test.*\"\n"
                    + "- type: ExcludeTaggedClass\n"
                    + "  enabled: true\n"
                    + "  classTagRegex: \"slow\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(FastClass.class));
            classDefinitions.add(createClassDefinition(SlowClass.class));
            classDefinitions.add(createClassDefinition(IntegrationTestClass.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(2);
            assertThat(classDefinitions.get(0).getTestClass()).isEqualTo(FastClass.class);
            assertThat(classDefinitions.get(1).getTestClass()).isEqualTo(IntegrationTestClass.class);
        }

        @Test
        @DisplayName("Should apply all four filter types together")
        public void shouldApplyAllFourFilterTypesTogether() throws IOException {
            String yaml = "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \"\\\\$.*Test.*\"\n"
                    + "- type: ExcludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Abstract.*\"\n"
                    + "- type: IncludeTaggedClass\n"
                    + "  enabled: true\n"
                    + "  classTagRegex: \"test\"\n"
                    + "- type: ExcludeTaggedClass\n"
                    + "  enabled: true\n"
                    + "  classTagRegex: \"slow\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(FastClass.class));
            classDefinitions.add(createClassDefinition(SlowClass.class));
            classDefinitions.add(createClassDefinition(AbstractTestClass.class));
            classDefinitions.add(createClassDefinition(IntegrationTestClass.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(2);
            assertThat(classDefinitions.get(0).getTestClass()).isEqualTo(FastClass.class);
            assertThat(classDefinitions.get(1).getTestClass()).isEqualTo(IntegrationTestClass.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    public class EdgeCaseTests {

        @Test
        @DisplayName("Should handle duplicate class definitions")
        public void shouldHandleDuplicateClassDefinitions() throws IOException {
            String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \"\\\\$.*Test.*\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(TestClass1.class));
            classDefinitions.add(createClassDefinition(TestClass1.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(2);

            assertThat(classDefinitions.get(0).getTestClass()).isEqualTo(TestClass1.class);
            assertThat(classDefinitions.get(1).getTestClass()).isEqualTo(TestClass1.class);
        }

        @Test
        @DisplayName("Should maintain insertion order")
        public void shouldMaintainInsertionOrder() throws IOException {
            String yaml = "- type: IncludeClass\n" + "  enabled: true\n" + "  classRegex: \"\\\\$.*Test.*\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(TestClass2.class));
            classDefinitions.add(createClassDefinition(TestClass1.class));
            classDefinitions.add(createClassDefinition(OtherClass.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(2);
            assertThat(classDefinitions.get(0).getTestClass()).isEqualTo(TestClass2.class);
            assertThat(classDefinitions.get(1).getTestClass()).isEqualTo(TestClass1.class);
        }

        @Test
        @DisplayName("Should handle disabled filters")
        public void shouldHandleDisabledFilters() throws IOException {
            String yaml = "- type: ExcludeClass\n" + "  enabled: false\n" + "  classRegex: \".*\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(TestClass1.class));
            classDefinitions.add(createClassDefinition(TestClass2.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    public class IntegrationTests {

        @Test
        @DisplayName("Should filter real test scenario")
        public void shouldFilterRealTestScenario() throws IOException {
            String yaml = "- type: IncludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \"\\\\$.*Test.*\"\n"
                    + "- type: ExcludeClass\n"
                    + "  enabled: true\n"
                    + "  classRegex: \".*Abstract.*\"\n"
                    + "- type: ExcludeTaggedClass\n"
                    + "  enabled: true\n"
                    + "  classTagRegex: \"slow\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(TestClass1.class));
            classDefinitions.add(createClassDefinition(TestClass2.class));
            classDefinitions.add(createClassDefinition(AbstractTestClass.class));
            classDefinitions.add(createClassDefinition(FastClass.class));
            classDefinitions.add(createClassDefinition(SlowClass.class));
            classDefinitions.add(createClassDefinition(OtherClass.class));

            ClassDefinitionFilter.filter(classDefinitions);

            assertThat(classDefinitions).hasSize(2);
            assertThat(classDefinitions)
                    .extracting(cd -> cd.getTestClass().getSimpleName())
                    .containsExactlyInAnyOrder("TestClass1", "TestClass2");
        }

        @Test
        @DisplayName("Should work with consecutive filter calls")
        public void shouldWorkWithConsecutiveFilterCalls() throws IOException {
            String yaml = "- type: ExcludeClass\n" + "  enabled: true\n" + "  classRegex: \".*Abstract.*\"\n";

            setupFilterFile(yaml);

            List<ClassDefinition> classDefinitions = new ArrayList<>();
            classDefinitions.add(createClassDefinition(TestClass1.class));
            classDefinitions.add(createClassDefinition(AbstractTestClass.class));

            ClassDefinitionFilter.filter(classDefinitions);
            assertThat(classDefinitions).hasSize(1);

            classDefinitions.add(createClassDefinition(TestClass2.class));
            ClassDefinitionFilter.filter(classDefinitions);
            assertThat(classDefinitions).hasSize(2);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ClassDefinition createClassDefinition(Class<?> testClass) {
        ClassDefinition classDefinition = mock(ClassDefinition.class);
        when(classDefinition.getTestClass()).thenReturn((Class) testClass);
        return classDefinition;
    }

    private void setupFilterFile(String yaml) throws IOException {
        Path filterFile = tempDirectory.resolve("filters.yaml");
        Files.write(filterFile, yaml.getBytes(StandardCharsets.UTF_8));
        ConcreteConfiguration.getInstance()
                .getProperties()
                .setProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, filterFile.toString());
    }

    private static class TestClass1 {
        // INTENTIONALLY EMPTY
    }

    private static class TestClass2 {
        // INTENTIONALLY EMPTY
    }

    private abstract static class AbstractTestClass {
        // INTENTIONALLY EMPTY
    }

    private static class OtherClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("fast-test")
    private static class FastClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("slow")
    private static class SlowClass {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Tag("integration-test")
    private static class IntegrationTestClass {
        // INTENTIONALLY EMPTY
    }
}
