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

package org.verifyica.engine.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.verifyica.engine.resolver.ConcreteMethodDefinition;

/**
 * Tests for the {@link MethodDefinition} interface.
 */
@DisplayName("MethodDefinition Interface Tests")
public class MethodDefinitionTest {

    @Test
    @DisplayName("Should return the method")
    public void shouldReturnTheMethod() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("testMethod");
        MethodDefinition definition = new ConcreteMethodDefinition(method, "Test Display Name");

        // When
        Method result = definition.getMethod();

        // Then
        assertThat(result).isEqualTo(method);
    }

    @Test
    @DisplayName("Should return the display name")
    public void shouldReturnTheDisplayName() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("testMethod");
        String displayName = "Test Display Name";
        MethodDefinition definition = new ConcreteMethodDefinition(method, displayName);

        // When
        String result = definition.getDisplayName();

        // Then
        assertThat(result).isEqualTo(displayName);
    }

    @Test
    @DisplayName("Should set and get display name")
    public void shouldSetAndGetDisplayName() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("testMethod");
        MethodDefinition definition = new ConcreteMethodDefinition(method, "Original Name");

        // When
        definition.setDisplayName("New Display Name");

        // Then
        assertThat(definition.getDisplayName()).isEqualTo("New Display Name");
    }

    @Test
    @DisplayName("Should handle null display name in constructor")
    public void shouldHandleNullDisplayNameInConstructor() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("testMethod");

        // When
        MethodDefinition definition = new ConcreteMethodDefinition(method, null);

        // Then
        assertThat(definition.getDisplayName()).isNull();
    }

    @Test
    @DisplayName("Should handle method with parameters")
    public void shouldHandleMethodWithParameters() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("methodWithParameters", String.class, int.class);
        MethodDefinition definition = new ConcreteMethodDefinition(method, "Parameterized Method");

        // When
        Method result = definition.getMethod();

        // Then
        assertThat(result).isEqualTo(method);
        assertThat(result.getParameterCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle method returning value")
    public void shouldHandleMethodReturningValue() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("methodWithReturnValue");
        MethodDefinition definition = new ConcreteMethodDefinition(method, "Method With Return");

        // When
        Method result = definition.getMethod();

        // Then
        assertThat(result).isEqualTo(method);
        assertThat(result.getReturnType()).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Should handle static method")
    public void shouldHandleStaticMethod() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("staticMethod");
        MethodDefinition definition = new ConcreteMethodDefinition(method, "Static Method");

        // When
        Method result = definition.getMethod();

        // Then
        assertThat(result).isEqualTo(method);
        assertThat(java.lang.reflect.Modifier.isStatic(result.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should handle private method")
    public void shouldHandlePrivateMethod() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getDeclaredMethod("privateMethod");
        MethodDefinition definition = new ConcreteMethodDefinition(method, "Private Method");

        // When
        Method result = definition.getMethod();

        // Then
        assertThat(result).isEqualTo(method);
        assertThat(java.lang.reflect.Modifier.isPrivate(result.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should update display name multiple times")
    public void shouldUpdateDisplayNameMultipleTimes() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("testMethod");
        MethodDefinition definition = new ConcreteMethodDefinition(method, "First Name");

        // When - First update
        definition.setDisplayName("Second Name");
        assertThat(definition.getDisplayName()).isEqualTo("Second Name");

        // When - Second update
        definition.setDisplayName("Third Name");

        // Then
        assertThat(definition.getDisplayName()).isEqualTo("Third Name");
    }

    @Test
    @DisplayName("Should implement interface correctly")
    public void shouldImplementInterfaceCorrectly() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("testMethod");
        MethodDefinition definition = new ConcreteMethodDefinition(method, "Display Name");

        // Then
        assertThat(definition).isInstanceOf(MethodDefinition.class);
    }

    // Test class with various method types
    public static class TestClass {

        public void testMethod() {
            // INTENTIONALLY EMPTY
        }

        public void methodWithParameters(String str, int num) {
            // INTENTIONALLY EMPTY
        }

        public String methodWithReturnValue() {
            return "value";
        }

        public static void staticMethod() {
            // INTENTIONALLY EMPTY
        }

        private void privateMethod() {
            // INTENTIONALLY EMPTY
        }
    }
}
