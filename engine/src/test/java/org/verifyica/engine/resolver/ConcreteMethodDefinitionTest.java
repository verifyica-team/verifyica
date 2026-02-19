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

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ConcreteMethodDefinition Tests")
public class ConcreteMethodDefinitionTest {

    @Test
    @DisplayName("Should create ConcreteMethodDefinition with method and display name")
    public void shouldCreateConcreteMethodDefinitionWithMethodAndDisplayName() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        String displayName = "Test Method Display Name";

        ConcreteMethodDefinition definition = new ConcreteMethodDefinition(method, displayName);

        assertThat(definition.getMethod()).isEqualTo(method);
        assertThat(definition.getDisplayName()).isEqualTo(displayName);
    }

    @Test
    @DisplayName("Should set and get display name")
    public void shouldSetAndGetDisplayName() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        ConcreteMethodDefinition definition = new ConcreteMethodDefinition(method, "Original Name");

        definition.setDisplayName("New Display Name");

        assertThat(definition.getDisplayName()).isEqualTo("New Display Name");
    }

    @Test
    @DisplayName("Should not update display name with null")
    public void shouldNotUpdateDisplayNameWithNull() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        ConcreteMethodDefinition definition = new ConcreteMethodDefinition(method, "Original Name");

        definition.setDisplayName(null);

        assertThat(definition.getDisplayName()).isEqualTo("Original Name");
    }

    @Test
    @DisplayName("Should not update display name with empty string")
    public void shouldNotUpdateDisplayNameWithEmptyString() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        ConcreteMethodDefinition definition = new ConcreteMethodDefinition(method, "Original Name");

        definition.setDisplayName("");

        assertThat(definition.getDisplayName()).isEqualTo("Original Name");
    }

    @Test
    @DisplayName("Should not update display name with blank string")
    public void shouldNotUpdateDisplayNameWithBlankString() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        ConcreteMethodDefinition definition = new ConcreteMethodDefinition(method, "Original Name");

        definition.setDisplayName("   ");

        assertThat(definition.getDisplayName()).isEqualTo("Original Name");
    }

    @Test
    @DisplayName("Should trim display name")
    public void shouldTrimDisplayName() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        ConcreteMethodDefinition definition = new ConcreteMethodDefinition(method, "Original Name");

        definition.setDisplayName("  Trimmed Name  ");

        assertThat(definition.getDisplayName()).isEqualTo("Trimmed Name");
    }

    @Test
    @DisplayName("Should implement toString")
    public void shouldImplementToString() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        ConcreteMethodDefinition definition = new ConcreteMethodDefinition(method, "Display Name");

        String result = definition.toString();

        assertThat(result).contains("ConcreteMethodDefinition");
        assertThat(result).contains("testMethod");
    }

    @Test
    @DisplayName("Should implement equals for same object")
    public void shouldImplementEqualsForSameObject() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        ConcreteMethodDefinition definition = new ConcreteMethodDefinition(method, "Display Name");

        assertThat(definition).isEqualTo(definition);
    }

    @Test
    @DisplayName("Should implement equals for equal objects")
    public void shouldImplementEqualsForEqualObjects() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        ConcreteMethodDefinition definition1 = new ConcreteMethodDefinition(method, "Display Name");
        ConcreteMethodDefinition definition2 = new ConcreteMethodDefinition(method, "Display Name");

        assertThat(definition1).isEqualTo(definition2);
    }

    @Test
    @DisplayName("Should implement equals for different methods")
    public void shouldImplementEqualsForDifferentMethods() throws NoSuchMethodException {
        Method method1 = TestClass.class.getMethod("testMethod");
        Method method2 = TestClass.class.getMethod("anotherMethod");
        ConcreteMethodDefinition definition1 = new ConcreteMethodDefinition(method1, "Display Name");
        ConcreteMethodDefinition definition2 = new ConcreteMethodDefinition(method2, "Display Name");

        assertThat(definition1).isNotEqualTo(definition2);
    }

    @Test
    @DisplayName("Should implement equals for different display names")
    public void shouldImplementEqualsForDifferentDisplayNames() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        ConcreteMethodDefinition definition1 = new ConcreteMethodDefinition(method, "Name 1");
        ConcreteMethodDefinition definition2 = new ConcreteMethodDefinition(method, "Name 2");

        assertThat(definition1).isNotEqualTo(definition2);
    }

    @Test
    @DisplayName("Should implement equals for null")
    public void shouldImplementEqualsForNull() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        ConcreteMethodDefinition definition = new ConcreteMethodDefinition(method, "Display Name");

        assertThat(definition).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should implement equals for different class")
    public void shouldImplementEqualsForDifferentClass() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        ConcreteMethodDefinition definition = new ConcreteMethodDefinition(method, "Display Name");

        assertThat(definition).isNotEqualTo("not a definition");
    }

    @Test
    @DisplayName("Should implement hashCode consistently")
    public void shouldImplementHashCodeConsistently() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        ConcreteMethodDefinition definition1 = new ConcreteMethodDefinition(method, "Display Name");
        ConcreteMethodDefinition definition2 = new ConcreteMethodDefinition(method, "Display Name");

        assertThat(definition1.hashCode()).isEqualTo(definition2.hashCode());
    }

    // Test class
    public static class TestClass {

        public void testMethod() {
            // INTENTIONALLY EMPTY
        }

        public void anotherMethod() {
            // INTENTIONALLY EMPTY
        }
    }
}
