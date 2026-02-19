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

package org.verifyica.engine.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ObjectSupport Tests")
public class ObjectSupportTest {

    @Test
    @DisplayName("Should create object from class")
    public void shouldCreateObjectFromClass() throws Throwable {
        Object object = ObjectSupport.createObject(TestClass.class);

        assertThat(object).isNotNull();
        assertThat(object).isInstanceOf(TestClass.class);
    }

    @Test
    @DisplayName("Should throw exception for null class")
    public void shouldThrowExceptionForNullClass() {
        assertThatThrownBy(() -> ObjectSupport.createObject(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("clazz is null");
    }

    @Test
    @DisplayName("Should throw exception for class without default constructor")
    public void shouldThrowExceptionForClassWithoutDefaultConstructor() {
        assertThatThrownBy(() -> ObjectSupport.createObject(ClassWithoutDefaultConstructor.class))
                .isInstanceOf(NoSuchMethodException.class);
    }

    @Test
    @DisplayName("Should throw exception for abstract class")
    public void shouldThrowExceptionForAbstractClass() {
        assertThatThrownBy(() -> ObjectSupport.createObject(AbstractTestClass.class))
                .isInstanceOf(InstantiationException.class);
    }

    @Test
    @DisplayName("Should convert single method to string")
    public void shouldConvertSingleMethodToString() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        String result = ObjectSupport.toString(method);

        assertThat(result).isEqualTo("testMethod");
    }

    @Test
    @DisplayName("Should convert multiple methods to string")
    public void shouldConvertMultipleMethodsToString() throws NoSuchMethodException {
        Method method1 = TestClass.class.getMethod("testMethod");
        Method method2 = TestClass.class.getMethod("anotherMethod");
        String result = ObjectSupport.toString(method1, method2);

        assertThat(result).isEqualTo("testMethod, anotherMethod");
    }

    @Test
    @DisplayName("Should convert method list to string")
    public void shouldConvertMethodListToString() throws NoSuchMethodException {
        List<Method> methods =
                Arrays.asList(TestClass.class.getMethod("testMethod"), TestClass.class.getMethod("anotherMethod"));
        String result = ObjectSupport.toString(methods);

        assertThat(result).isEqualTo("testMethod, anotherMethod");
    }

    @Test
    @DisplayName("Should return empty string for empty method array")
    public void shouldReturnEmptyStringForEmptyMethodArray() {
        String result = ObjectSupport.toString(new Method[0]);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty string for empty method list")
    public void shouldReturnEmptyStringForEmptyMethodList() {
        String result = ObjectSupport.toString(new ArrayList<Method>());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception for null method array")
    public void shouldThrowExceptionForNullMethodArray() {
        assertThatThrownBy(() -> ObjectSupport.toString((Method[]) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("methods is null");
    }

    @Test
    @DisplayName("Should throw exception for null method list")
    public void shouldThrowExceptionForNullMethodList() {
        assertThatThrownBy(() -> ObjectSupport.toString((List<Method>) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("methods is null");
    }

    @Test
    @DisplayName("Should handle single element list")
    public void shouldHandleSingleElementList() throws NoSuchMethodException {
        List<Method> methods = Collections.singletonList(TestClass.class.getMethod("testMethod"));
        String result = ObjectSupport.toString(methods);

        assertThat(result).isEqualTo("testMethod");
    }

    // Test classes

    public static class TestClass {
        public TestClass() {
            // Default constructor
        }

        public void testMethod() {
            // INTENTIONALLY EMPTY
        }

        public void anotherMethod() {
            // INTENTIONALLY EMPTY
        }
    }

    public static class ClassWithoutDefaultConstructor {
        public ClassWithoutDefaultConstructor(String arg) {
            // INTENTIONALLY EMPTY
        }
    }

    public abstract static class AbstractTestClass {
        // INTENTIONALLY EMPTY
    }
}
