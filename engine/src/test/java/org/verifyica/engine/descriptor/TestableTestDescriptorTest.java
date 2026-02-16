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

package org.verifyica.engine.descriptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.junit.jupiter.api.*;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Configuration;
import org.verifyica.engine.common.throttle.NoopThrottle;
import org.verifyica.engine.common.throttle.RandomSleepThrottle;
import org.verifyica.engine.common.throttle.Throttle;
import org.verifyica.engine.exception.TestClassDefinitionException;

@DisplayName("TestableTestDescriptor Tests")
class TestableTestDescriptorTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create descriptor with uniqueId and displayName")
        void shouldCreateDescriptorWithUniqueIdAndDisplayName() {
            UniqueId uniqueId = UniqueId.root("test", "test-id");
            ConcreteTestableTestDescriptor descriptor = new ConcreteTestableTestDescriptor(uniqueId, "Test Display");

            assertThat(descriptor.getUniqueId()).isEqualTo(uniqueId);
            assertThat(descriptor.getDisplayName()).isEqualTo("Test Display");
        }

        @Test
        @DisplayName("Should have CONTAINER type by default")
        void shouldHaveContainerTypeByDefault() {
            UniqueId uniqueId = UniqueId.root("test", "test-id");
            ConcreteTestableTestDescriptor descriptor = new ConcreteTestableTestDescriptor(uniqueId, "Test");

            assertThat(descriptor.getType()).isEqualTo(TestDescriptor.Type.CONTAINER);
        }
    }

    @Nested
    @DisplayName("Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should return null status before test execution")
        void shouldReturnNullStatusBeforeTestExecution() {
            ConcreteTestableTestDescriptor descriptor =
                    new ConcreteTestableTestDescriptor(UniqueId.root("test", "id"), "Test");

            assertThat(descriptor.getTestDescriptorStatus()).isNull();
        }

        @Test
        @DisplayName("Should set and get test descriptor status")
        void shouldSetAndGetTestDescriptorStatus() {
            ConcreteTestableTestDescriptor descriptor =
                    new ConcreteTestableTestDescriptor(UniqueId.root("test", "id"), "Test");
            TestDescriptorStatus status = TestDescriptorStatus.passed();

            descriptor.setStatusPublic(status);

            assertThat(descriptor.getTestDescriptorStatus()).isSameAs(status);
        }
    }

    @Nested
    @DisplayName("Invoke Method Tests")
    class InvokeMethodTests {

        @Test
        @DisplayName("Should invoke method with matching Configuration parameter")
        void shouldInvokeMethodWithMatchingConfigurationParameter() throws Exception {
            TestClass testInstance = new TestClass();
            Method method = TestClass.class.getDeclaredMethod("methodWithConfiguration", Configuration.class);
            Configuration config = mock(Configuration.class);
            List<Object> arguments = Collections.singletonList(config);

            TestableTestDescriptor.invoke(method, testInstance, arguments);

            assertThat(testInstance.configReceived).isSameAs(config);
        }

        @Test
        @DisplayName("Should invoke method with matching ClassContext parameter")
        void shouldInvokeMethodWithMatchingClassContextParameter() throws Exception {
            TestClass testInstance = new TestClass();
            Method method = TestClass.class.getDeclaredMethod("methodWithClassContext", ClassContext.class);
            ClassContext context = mock(ClassContext.class);
            List<Object> arguments = Collections.singletonList(context);

            TestableTestDescriptor.invoke(method, testInstance, arguments);

            assertThat(testInstance.classContextReceived).isSameAs(context);
        }

        @Test
        @DisplayName("Should invoke method with matching ArgumentContext parameter")
        void shouldInvokeMethodWithMatchingArgumentContextParameter() throws Exception {
            TestClass testInstance = new TestClass();
            Method method = TestClass.class.getDeclaredMethod("methodWithArgumentContext", ArgumentContext.class);
            ArgumentContext context = mock(ArgumentContext.class);
            List<Object> arguments = Collections.singletonList(context);

            TestableTestDescriptor.invoke(method, testInstance, arguments);

            assertThat(testInstance.argumentContextReceived).isSameAs(context);
        }

        @Test
        @DisplayName("Should invoke no-arg method when noParameters is true")
        void shouldInvokeNoArgMethodWhenNoParametersIsTrue() throws Exception {
            TestClass testInstance = new TestClass();
            Method method = TestClass.class.getDeclaredMethod("noArgMethod");
            List<Object> arguments = Collections.emptyList();

            TestableTestDescriptor.invoke(method, testInstance, arguments, true);

            assertThat(testInstance.noArgMethodCalled).isTrue();
        }

        @Test
        @DisplayName("Should throw TestClassDefinitionException when no matching parameter type")
        void shouldThrowTestClassDefinitionExceptionWhenNoMatchingParameterType() throws Exception {
            TestClass testInstance = new TestClass();
            Method method = TestClass.class.getDeclaredMethod("methodWithConfiguration", Configuration.class);
            List<Object> arguments = Arrays.asList(new Object(), "wrong type");

            assertThatThrownBy(() -> TestableTestDescriptor.invoke(method, testInstance, arguments))
                    .isInstanceOf(TestClassDefinitionException.class)
                    .hasMessageContaining("invalid argument type");
        }

        @Test
        @DisplayName("Should invoke zero-parameter methods regardless of noParameters flag")
        void shouldInvokeZeroParameterMethodsRegardlessOfNoParametersFlag() throws Exception {
            TestClass testInstance = new TestClass();
            Method method = TestClass.class.getDeclaredMethod("noArgMethod");
            List<Object> arguments = Collections.emptyList();

            // Should not throw - 0-parameter methods should always be invoked
            assertThatCode(() -> TestableTestDescriptor.invoke(method, testInstance, arguments, false))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should find first matching parameter type in list")
        void shouldFindFirstMatchingParameterTypeInList() throws Exception {
            TestClass testInstance = new TestClass();
            Method method = TestClass.class.getDeclaredMethod("methodWithConfiguration", Configuration.class);
            Configuration config = mock(Configuration.class);
            List<Object> arguments = new ArrayList<>();
            arguments.add("wrong type 1");
            arguments.add(new Object());
            arguments.add(config);
            arguments.add(mock(Configuration.class)); // Won't be used

            TestableTestDescriptor.invoke(method, testInstance, arguments);

            assertThat(testInstance.configReceived).isSameAs(config);
        }

        @Test
        @DisplayName("Should propagate InvocationTargetException")
        void shouldPropagateInvocationTargetException() throws Exception {
            TestClass testInstance = new TestClass();
            Method method = TestClass.class.getDeclaredMethod("methodThatThrows", Configuration.class);
            Configuration config = mock(Configuration.class);
            List<Object> arguments = Collections.singletonList(config);

            assertThatThrownBy(() -> TestableTestDescriptor.invoke(method, testInstance, arguments))
                    .isInstanceOf(InvocationTargetException.class)
                    .hasCauseInstanceOf(RuntimeException.class)
                    .cause()
                    .hasMessage("Method threw exception");
        }
    }

    @Nested
    @DisplayName("Throttle Creation Tests")
    class ThrottleCreationTests {

        @Test
        @DisplayName("Should create RandomSleepThrottle with single value")
        void shouldCreateRandomSleepThrottleWithSingleValue() {
            Configuration config = mock(Configuration.class);
            Properties properties = new Properties();
            properties.setProperty("test.throttle", "100");
            when(config.getProperties()).thenReturn(properties);

            ConcreteTestableTestDescriptor descriptor =
                    new ConcreteTestableTestDescriptor(UniqueId.root("test", "id"), "Test");
            Throttle throttle = descriptor.createThrottlePublic(config, "test.throttle");

            assertThat(throttle).isInstanceOf(RandomSleepThrottle.class);
        }

        @Test
        @DisplayName("Should create RandomSleepThrottle with two values")
        void shouldCreateRandomSleepThrottleWithTwoValues() {
            Configuration config = mock(Configuration.class);
            Properties properties = new Properties();
            properties.setProperty("test.throttle", "100,200");
            when(config.getProperties()).thenReturn(properties);

            ConcreteTestableTestDescriptor descriptor =
                    new ConcreteTestableTestDescriptor(UniqueId.root("test", "id"), "Test");
            Throttle throttle = descriptor.createThrottlePublic(config, "test.throttle");

            assertThat(throttle).isInstanceOf(RandomSleepThrottle.class);
        }

        @Test
        @DisplayName("Should create RandomSleepThrottle with space-separated values")
        void shouldCreateRandomSleepThrottleWithSpaceSeparatedValues() {
            Configuration config = mock(Configuration.class);
            Properties properties = new Properties();
            properties.setProperty("test.throttle", "100 200");
            when(config.getProperties()).thenReturn(properties);

            ConcreteTestableTestDescriptor descriptor =
                    new ConcreteTestableTestDescriptor(UniqueId.root("test", "id"), "Test");
            Throttle throttle = descriptor.createThrottlePublic(config, "test.throttle");

            assertThat(throttle).isInstanceOf(RandomSleepThrottle.class);
        }

        @Test
        @DisplayName("Should create RandomSleepThrottle with 0,0 for invalid token count")
        void shouldCreateRandomSleepThrottleWithZerosForInvalidTokenCount() {
            Configuration config = mock(Configuration.class);
            Properties properties = new Properties();
            properties.setProperty("test.throttle", "100,200,300");
            when(config.getProperties()).thenReturn(properties);

            ConcreteTestableTestDescriptor descriptor =
                    new ConcreteTestableTestDescriptor(UniqueId.root("test", "id"), "Test");
            Throttle throttle = descriptor.createThrottlePublic(config, "test.throttle");

            assertThat(throttle).isInstanceOf(RandomSleepThrottle.class);
        }

        @Test
        @DisplayName("Should create NoopThrottle when property is null")
        void shouldCreateNoopThrottleWhenPropertyIsNull() {
            Configuration config = mock(Configuration.class);
            Properties properties = new Properties();
            when(config.getProperties()).thenReturn(properties);

            ConcreteTestableTestDescriptor descriptor =
                    new ConcreteTestableTestDescriptor(UniqueId.root("test", "id"), "Test");
            Throttle throttle = descriptor.createThrottlePublic(config, "nonexistent.throttle");

            assertThat(throttle).isInstanceOf(NoopThrottle.class);
        }

        @Test
        @DisplayName("Should create NoopThrottle when property is empty")
        void shouldCreateNoopThrottleWhenPropertyIsEmpty() {
            Configuration config = mock(Configuration.class);
            Properties properties = new Properties();
            properties.setProperty("test.throttle", "");
            when(config.getProperties()).thenReturn(properties);

            ConcreteTestableTestDescriptor descriptor =
                    new ConcreteTestableTestDescriptor(UniqueId.root("test", "id"), "Test");
            Throttle throttle = descriptor.createThrottlePublic(config, "test.throttle");

            assertThat(throttle).isInstanceOf(NoopThrottle.class);
        }

        @Test
        @DisplayName("Should create NoopThrottle when property is whitespace")
        void shouldCreateNoopThrottleWhenPropertyIsWhitespace() {
            Configuration config = mock(Configuration.class);
            Properties properties = new Properties();
            properties.setProperty("test.throttle", "   ");
            when(config.getProperties()).thenReturn(properties);

            ConcreteTestableTestDescriptor descriptor =
                    new ConcreteTestableTestDescriptor(UniqueId.root("test", "id"), "Test");
            Throttle throttle = descriptor.createThrottlePublic(config, "test.throttle");

            assertThat(throttle).isInstanceOf(NoopThrottle.class);
        }

        @Test
        @DisplayName("Should create NoopThrottle when parsing fails")
        void shouldCreateNoopThrottleWhenParsingFails() {
            Configuration config = mock(Configuration.class);
            Properties properties = new Properties();
            properties.setProperty("test.throttle", "invalid,notanumber");
            when(config.getProperties()).thenReturn(properties);

            ConcreteTestableTestDescriptor descriptor =
                    new ConcreteTestableTestDescriptor(UniqueId.root("test", "id"), "Test");
            Throttle throttle = descriptor.createThrottlePublic(config, "test.throttle");

            assertThat(throttle).isInstanceOf(NoopThrottle.class);
        }
    }

    @Nested
    @DisplayName("PrintStackTrace Tests")
    class PrintStackTraceTests {

        @Test
        @DisplayName("Should print stack trace to System.err")
        void shouldPrintStackTraceToSystemErr() {
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PrintStream originalErr = System.err;

            try {
                System.setErr(new PrintStream(errorStream));
                RuntimeException exception = new RuntimeException("Test exception");

                TestableTestDescriptor.printStackTrace(exception);

                String output = errorStream.toString();
                assertThat(output).contains("RuntimeException").contains("Test exception");
            } finally {
                System.setErr(originalErr);
            }
        }

        // @Test
        // @DisplayName("Should include stack trace elements")
        // TODO fix this test
        void shouldIncludeStackTraceElements() {
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PrintStream originalErr = System.err;

            try {
                System.setErr(new PrintStream(errorStream));
                RuntimeException exception = new RuntimeException("Test exception");

                TestableTestDescriptor.printStackTrace(exception);

                String output = errorStream.toString();
                assertThat(output).contains("at ");
            } finally {
                System.setErr(originalErr);
            }
        }
    }

    @Nested
    @DisplayName("Static Field Constants Tests")
    class StaticFieldConstantsTests {

        @Test
        @DisplayName("Should have correct ENGINE_EXECUTION_LISTENER constant")
        void shouldHaveCorrectEngineExecutionListenerConstant() {
            assertThat(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER).isEqualTo("engineExecutionListener");
        }

        @Test
        @DisplayName("Should have correct CLASS_INTERCEPTORS constant")
        void shouldHaveCorrectClassInterceptorsConstant() {
            assertThat(TestableTestDescriptor.CLASS_INTERCEPTORS).isEqualTo("classInterceptors");
        }

        @Test
        @DisplayName("Should have correct CLASS_INTERCEPTORS_REVERSED constant")
        void shouldHaveCorrectClassInterceptorsReversedConstant() {
            assertThat(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED).isEqualTo("classInterceptorsReversed");
        }

        @Test
        @DisplayName("Should have correct TEST_ARGUMENT_EXECUTOR_SERVICE constant")
        void shouldHaveCorrectTestArgumentExecutorServiceConstant() {
            assertThat(TestableTestDescriptor.TEST_ARGUMENT_EXECUTOR_SERVICE).isEqualTo("argumentExecutorService");
        }

        @Test
        @DisplayName("Should have correct ARGUMENT_EXECUTOR_POOL constant")
        void shouldHaveCorrectArgumentExecutorPoolConstant() {
            assertThat(TestableTestDescriptor.ARGUMENT_EXECUTOR_POOL).isEqualTo("argumentExecutorPool");
        }

        @Test
        @DisplayName("Should have correct ENGINE_CONTEXT constant")
        void shouldHaveCorrectEngineContextConstant() {
            assertThat(TestableTestDescriptor.ENGINE_CONTEXT).isEqualTo("engineContext");
        }

        @Test
        @DisplayName("Should have correct CLASS_CONTEXT constant")
        void shouldHaveCorrectClassContextConstant() {
            assertThat(TestableTestDescriptor.CLASS_CONTEXT).isEqualTo("classContext");
        }

        @Test
        @DisplayName("Should have correct ARGUMENT_CONTEXT constant")
        void shouldHaveCorrectArgumentContextConstant() {
            assertThat(TestableTestDescriptor.ARGUMENT_CONTEXT).isEqualTo("argumentContext");
        }
    }

    @Nested
    @DisplayName("Predicate and Mapper Tests")
    class PredicateAndMapperTests {

        @Test
        @DisplayName("TESTABLE_TEST_DESCRIPTOR_FILTER should accept TestableTestDescriptor")
        void testableTestDescriptorFilterShouldAcceptTestableTestDescriptor() {
            TestDescriptor descriptor = new ConcreteTestableTestDescriptor(UniqueId.root("test", "id"), "Test");

            boolean result = TestableTestDescriptor.TESTABLE_TEST_DESCRIPTOR_FILTER.test(descriptor);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("TESTABLE_TEST_DESCRIPTOR_FILTER should reject non-TestableTestDescriptor")
        void testableTestDescriptorFilterShouldRejectNonTestableTestDescriptor() {
            TestDescriptor descriptor = mock(TestDescriptor.class);

            boolean result = TestableTestDescriptor.TESTABLE_TEST_DESCRIPTOR_FILTER.test(descriptor);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("TESTABLE_TEST_DESCRIPTOR_MAPPER should map TestDescriptor to TestableTestDescriptor")
        void testableTestDescriptorMapperShouldMapTestDescriptorToTestableTestDescriptor() {
            ConcreteTestableTestDescriptor original =
                    new ConcreteTestableTestDescriptor(UniqueId.root("test", "id"), "Test");

            TestableTestDescriptor mapped = TestableTestDescriptor.TESTABLE_TEST_DESCRIPTOR_MAPPER.apply(original);

            assertThat(mapped).isSameAs(original);
        }

        @Test
        @DisplayName("Filter and mapper should work together in stream")
        void filterAndMapperShouldWorkTogetherInStream() {
            ConcreteTestableTestDescriptor testable1 =
                    new ConcreteTestableTestDescriptor(UniqueId.root("test", "id1"), "Test1");
            ConcreteTestableTestDescriptor testable2 =
                    new ConcreteTestableTestDescriptor(UniqueId.root("test", "id2"), "Test2");
            TestDescriptor nonTestable = mock(TestDescriptor.class);

            List<TestDescriptor> descriptors = Arrays.asList(testable1, nonTestable, testable2);

            List<TestableTestDescriptor> result = descriptors.stream()
                    .filter(TestableTestDescriptor.TESTABLE_TEST_DESCRIPTOR_FILTER)
                    .map(TestableTestDescriptor.TESTABLE_TEST_DESCRIPTOR_MAPPER)
                    .collect(Collectors.toList());

            assertThat(result).containsExactly(testable1, testable2);
        }
    }

    // Concrete implementation for testing
    private static class ConcreteTestableTestDescriptor extends TestableTestDescriptor {
        public ConcreteTestableTestDescriptor(UniqueId uniqueId, String displayName) {
            super(uniqueId, displayName);
        }

        @Override
        public TestableTestDescriptor test() {
            return this;
        }

        @Override
        public void skip() {
            // No-op for testing
        }

        public void setStatusPublic(TestDescriptorStatus status) {
            setTestDescriptorStatus(status);
        }

        public Throttle createThrottlePublic(Configuration config, String name) {
            return createThrottle(config, name);
        }
    }

    // Test helper class
    @SuppressWarnings("unused")
    private static class TestClass {
        Configuration configReceived;
        ClassContext classContextReceived;
        ArgumentContext argumentContextReceived;
        boolean noArgMethodCalled;

        public void methodWithConfiguration(Configuration config) {
            this.configReceived = config;
        }

        public void methodWithClassContext(ClassContext context) {
            this.classContextReceived = context;
        }

        public void methodWithArgumentContext(ArgumentContext context) {
            this.argumentContextReceived = context;
        }

        public void noArgMethod() {
            this.noArgMethodCalled = true;
        }

        public void methodThatThrows(Configuration config) {
            throw new RuntimeException("Method threw exception");
        }
    }
}
