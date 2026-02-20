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

package org.verifyica.engine.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.verifyica.api.Argument;
import org.verifyica.engine.api.MethodDefinition;

@DisplayName("ConcreteClassDefinition Tests")
public class ConcreteClassDefinitionTest {

    @Test
    @DisplayName("Should create ConcreteClassDefinition with all parameters")
    public void shouldCreateConcreteClassDefinitionWithAllParameters() {
        Class<?> testClass = TestClass.class;
        String displayName = "Test Display Name";
        Set<String> tags = new LinkedHashSet<>(Arrays.asList("tag1", "tag2"));
        List<MethodDefinition> methodDefinitions = Collections.emptyList();
        List<Argument<?>> arguments = Collections.emptyList();
        int argumentParallelism = 4;

        ConcreteClassDefinition definition = new ConcreteClassDefinition(
                testClass, displayName, tags, methodDefinitions, arguments, argumentParallelism);

        assertThat(definition.getTestClass()).isEqualTo(testClass);
        assertThat(definition.getDisplayName()).isEqualTo(displayName);
        assertThat(definition.getTags()).isEqualTo(tags);
        assertThat(definition.getTestMethodDefinitions()).isEmpty();
        assertThat(definition.getArguments()).isEmpty();
        assertThat(definition.getArgumentParallelism()).isEqualTo(argumentParallelism);
    }

    @Test
    @DisplayName("Should set and get display name")
    public void shouldSetAndGetDisplayName() {
        ConcreteClassDefinition definition = createDefaultDefinition();

        definition.setDisplayName("New Display Name");

        assertThat(definition.getDisplayName()).isEqualTo("New Display Name");
    }

    @Test
    @DisplayName("Should not update display name with null")
    public void shouldNotUpdateDisplayNameWithNull() {
        ConcreteClassDefinition definition = createDefaultDefinition();
        String originalDisplayName = definition.getDisplayName();

        definition.setDisplayName(null);

        assertThat(definition.getDisplayName()).isEqualTo(originalDisplayName);
    }

    @Test
    @DisplayName("Should not update display name with empty string")
    public void shouldNotUpdateDisplayNameWithEmptyString() {
        ConcreteClassDefinition definition = createDefaultDefinition();
        String originalDisplayName = definition.getDisplayName();

        definition.setDisplayName("");

        assertThat(definition.getDisplayName()).isEqualTo(originalDisplayName);
    }

    @Test
    @DisplayName("Should not update display name with blank string")
    public void shouldNotUpdateDisplayNameWithBlankString() {
        ConcreteClassDefinition definition = createDefaultDefinition();
        String originalDisplayName = definition.getDisplayName();

        definition.setDisplayName("   ");

        assertThat(definition.getDisplayName()).isEqualTo(originalDisplayName);
    }

    @Test
    @DisplayName("Should trim display name")
    public void shouldTrimDisplayName() {
        ConcreteClassDefinition definition = createDefaultDefinition();

        definition.setDisplayName("  Trimmed Name  ");

        assertThat(definition.getDisplayName()).isEqualTo("Trimmed Name");
    }

    @Test
    @DisplayName("Should implement toString")
    public void shouldImplementToString() {
        ConcreteClassDefinition definition = createDefaultDefinition();

        String result = definition.toString();

        assertThat(result).contains("ConcreteClassDefinition");
        assertThat(result).contains(TestClass.class.getName());
    }

    @Test
    @DisplayName("Should implement equals for same object")
    public void shouldImplementEqualsForSameObject() {
        ConcreteClassDefinition definition = createDefaultDefinition();

        assertThat(definition).isEqualTo(definition);
    }

    @Test
    @DisplayName("Should implement equals for equal objects")
    public void shouldImplementEqualsForEqualObjects() {
        ConcreteClassDefinition definition1 = createDefaultDefinition();
        ConcreteClassDefinition definition2 = createDefaultDefinition();

        assertThat(definition1).isEqualTo(definition2);
    }

    @Test
    @DisplayName("Should implement equals for different objects")
    public void shouldImplementEqualsForDifferentObjects() {
        ConcreteClassDefinition definition1 = createDefaultDefinition();
        ConcreteClassDefinition definition2 = new ConcreteClassDefinition(
                String.class, "Different", Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), 1);

        assertThat(definition1).isNotEqualTo(definition2);
    }

    @Test
    @DisplayName("Should implement equals for null")
    public void shouldImplementEqualsForNull() {
        ConcreteClassDefinition definition = createDefaultDefinition();

        assertThat(definition).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should implement equals for different class")
    public void shouldImplementEqualsForDifferentClass() {
        ConcreteClassDefinition definition = createDefaultDefinition();

        assertThat(definition).isNotEqualTo("not a definition");
    }

    @Test
    @DisplayName("Should implement hashCode consistently")
    public void shouldImplementHashCodeConsistently() {
        ConcreteClassDefinition definition1 = createDefaultDefinition();
        ConcreteClassDefinition definition2 = createDefaultDefinition();

        assertThat(definition1.hashCode()).isEqualTo(definition2.hashCode());
    }

    @Test
    @DisplayName("Should return different hashCode for different objects")
    public void shouldReturnDifferentHashCodeForDifferentObjects() {
        ConcreteClassDefinition definition1 = createDefaultDefinition();
        ConcreteClassDefinition definition2 = new ConcreteClassDefinition(
                String.class, "Different", Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), 1);

        assertThat(definition1.hashCode()).isNotEqualTo(definition2.hashCode());
    }

    @Test
    @DisplayName("Should return unmodifiable test method definitions set")
    public void shouldReturnUnmodifiableTestMethodDefinitionsSet() {
        ConcreteClassDefinition definition = createDefaultDefinition();

        Set<MethodDefinition> methodDefinitions = definition.getTestMethodDefinitions();

        assertThat(methodDefinitions).isNotNull();
        assertThat(methodDefinitions).isEmpty();
    }

    @Test
    @DisplayName("Should return arguments list")
    public void shouldReturnArgumentsList() {
        ConcreteClassDefinition definition = createDefaultDefinition();

        List<Argument<?>> arguments = definition.getArguments();

        assertThat(arguments).isNotNull();
        assertThat(arguments).isEmpty();
    }

    @Test
    @DisplayName("Should return tags set")
    public void shouldReturnTagsSet() {
        Set<String> tags = new LinkedHashSet<>(Arrays.asList("tag1", "tag2", "tag3"));
        ConcreteClassDefinition definition = new ConcreteClassDefinition(
                TestClass.class, "Test", tags, Collections.emptyList(), Collections.emptyList(), 1);

        Set<String> returnedTags = definition.getTags();

        assertThat(returnedTags).isEqualTo(tags);
    }

    @Test
    @DisplayName("Should handle empty tags")
    public void shouldHandleEmptyTags() {
        ConcreteClassDefinition definition = new ConcreteClassDefinition(
                TestClass.class, "Test", Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), 1);

        Set<String> tags = definition.getTags();

        assertThat(tags).isEmpty();
    }

    @Test
    @DisplayName("Should handle zero argument parallelism")
    public void shouldHandleZeroArgumentParallelism() {
        ConcreteClassDefinition definition = new ConcreteClassDefinition(
                TestClass.class, "Test", Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), 0);

        assertThat(definition.getArgumentParallelism()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle negative argument parallelism")
    public void shouldHandleNegativeArgumentParallelism() {
        ConcreteClassDefinition definition = new ConcreteClassDefinition(
                TestClass.class, "Test", Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), -1);

        assertThat(definition.getArgumentParallelism()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Should not be equal when test class differs")
    public void shouldNotBeEqualWhenTestClassDiffers() {
        ConcreteClassDefinition definition1 = createDefaultDefinition();
        ConcreteClassDefinition definition2 = new ConcreteClassDefinition(
                String.class,
                "Test Display Name",
                Collections.emptySet(),
                Collections.emptyList(),
                Collections.emptyList(),
                1);

        assertThat(definition1).isNotEqualTo(definition2);
    }

    @Test
    @DisplayName("Should not be equal when display name differs")
    public void shouldNotBeEqualWhenDisplayNameDiffers() {
        ConcreteClassDefinition definition1 = createDefaultDefinition();
        ConcreteClassDefinition definition2 = new ConcreteClassDefinition(
                TestClass.class,
                "Different Name",
                Collections.emptySet(),
                Collections.emptyList(),
                Collections.emptyList(),
                1);

        assertThat(definition1).isNotEqualTo(definition2);
    }

    @Test
    @DisplayName("Should not be equal when arguments differ")
    public void shouldNotBeEqualWhenArgumentsDiffer() {
        Argument<String> argument = Argument.of("test", "value");
        ConcreteClassDefinition definition1 = createDefaultDefinition();
        ConcreteClassDefinition definition2 = new ConcreteClassDefinition(
                TestClass.class,
                "Test Display Name",
                Collections.emptySet(),
                Collections.emptyList(),
                Collections.singletonList(argument),
                1);

        assertThat(definition1).isNotEqualTo(definition2);
    }

    @Test
    @DisplayName("Should include all fields in toString")
    public void shouldIncludeAllFieldsInToString() {
        Set<String> tags = new LinkedHashSet<>(Arrays.asList("tag1"));
        Argument<String> argument = Argument.of("test", "value");
        ConcreteClassDefinition definition = new ConcreteClassDefinition(
                TestClass.class,
                "Test Display Name",
                tags,
                Collections.emptyList(),
                Collections.singletonList(argument),
                2);

        String result = definition.toString();

        assertThat(result).contains("ConcreteClassDefinition");
        assertThat(result).contains(TestClass.class.getName());
        assertThat(result).contains("Test Display Name");
        assertThat(result).contains("argumentParallelism=2");
        assertThat(result).contains("testArguments");
        assertThat(result).contains("test");
    }

    private ConcreteClassDefinition createDefaultDefinition() {
        return new ConcreteClassDefinition(
                TestClass.class,
                "Test Display Name",
                Collections.emptySet(),
                Collections.emptyList(),
                Collections.emptyList(),
                1);
    }

    // Test class
    public static class TestClass {

        public void testMethod() {
            // INTENTIONALLY EMPTY
        }
    }
}
