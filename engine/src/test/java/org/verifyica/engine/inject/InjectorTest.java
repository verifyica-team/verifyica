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

package org.verifyica.engine.inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Injector Tests")
public class InjectorTest {

    @Test
    @DisplayName("Should inject value into named field")
    public void shouldInjectValueIntoNamedField() {
        TestTarget target = new TestTarget();
        String value = "injected value";

        Injector.inject("testField", value, target);

        assertThat(target.testField).isEqualTo(value);
    }

    @Test
    @DisplayName("Should not inject into field with different name")
    public void shouldNotInjectIntoFieldWithDifferentName() {
        TestTarget target = new TestTarget();
        String originalValue = target.otherField;

        Injector.inject("wrongName", "new value", target);

        assertThat(target.otherField).isEqualTo(originalValue);
    }

    @Test
    @DisplayName("Should not inject into static field with instance injection")
    public void shouldNotInjectIntoStaticFieldWithInstanceInjection() {
        TestTarget target = new TestTarget();
        String originalValue = TestTarget.staticField;

        Injector.inject("staticField", "new value", target);

        assertThat(TestTarget.staticField).isEqualTo(originalValue);
    }

    @Test
    @DisplayName("Should not inject incompatible type")
    public void shouldNotInjectIncompatibleType() {
        TestTarget target = new TestTarget();
        Integer originalValue = target.integerField;

        Injector.inject("integerField", "not an integer", target);

        assertThat(target.integerField).isEqualTo(originalValue);
    }

    @Test
    @DisplayName("Should inject into inherited field")
    public void shouldInjectIntoInheritedField() {
        ExtendedTestTarget target = new ExtendedTestTarget();
        String value = "injected into inherited field";

        Injector.inject("inheritedField", value, target);

        assertThat(target.inheritedField).isEqualTo(value);
    }

    @Test
    @DisplayName("Should inject using custom annotation")
    public void shouldInjectUsingCustomAnnotation() {
        AnnotatedTestTarget target = new AnnotatedTestTarget();
        String value = "injected via annotation";

        Injector.inject(CustomInject.class, value, target);

        assertThat(target.customField).isEqualTo(value);
    }

    @Test
    @DisplayName("Should inject into static field using class target")
    public void shouldInjectIntoStaticFieldUsingClassTarget() {
        StaticTestTarget.staticField = null;
        String value = "injected into static field";

        Injector.inject(CustomInject.class, value, StaticTestTarget.class);

        assertThat(StaticTestTarget.staticField).isEqualTo(value);
    }

    @Test
    @DisplayName("Should handle injection into private field")
    public void shouldHandleInjectionIntoPrivateField() {
        PrivateFieldTarget target = new PrivateFieldTarget();
        String value = "injected into private field";

        Injector.inject("privateField", value, target);

        assertThat(target.getPrivateField()).isEqualTo(value);
    }

    @Test
    @DisplayName("Should handle multiple injections")
    public void shouldHandleMultipleInjections() {
        TestTarget target = new TestTarget();

        Injector.inject("testField", "value1", target);
        Injector.inject("otherField", "value2", target);

        assertThat(target.testField).isEqualTo("value1");
        assertThat(target.otherField).isEqualTo("value2");
    }

    @Test
    @DisplayName("Should throw exception for null target in named inject")
    public void shouldThrowExceptionForNullTargetInNamedInject() {
        assertThatCode(() -> Injector.inject("name", "value", (Object) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should throw exception for null target in annotation inject")
    public void shouldThrowExceptionForNullTargetInAnnotationInject() {
        assertThatCode(() -> Injector.inject(CustomInject.class, "value", (Object) null))
                .isInstanceOf(NullPointerException.class);
    }

    // Custom annotation for testing
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomInject {}

    // Test target classes
    public static class TestTarget {
        @Inject
        @Named("testField")
        public String testField;

        @Inject
        @Named("otherField")
        public String otherField;

        @Inject
        @Named("staticField")
        public static String staticField;

        @Inject
        @Named("integerField")
        public Integer integerField = 42;
    }

    public static class BaseTestTarget {
        @Inject
        @Named("inheritedField")
        public String inheritedField;
    }

    public static class ExtendedTestTarget extends BaseTestTarget {
        // Inherits inheritedField
    }

    public static class AnnotatedTestTarget {
        @Inject
        @CustomInject
        public String customField;
    }

    public static class StaticTestTarget {
        @Inject
        @CustomInject
        public static String staticField;
    }

    public static class PrivateFieldTarget {
        @Inject
        @Named("privateField")
        private String privateField;

        public String getPrivateField() {
            return privateField;
        }
    }
}
