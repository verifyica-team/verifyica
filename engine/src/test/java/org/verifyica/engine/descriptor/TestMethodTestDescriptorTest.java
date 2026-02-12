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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.*;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.verifyica.api.*;
import org.verifyica.engine.inject.Injector;

@DisplayName("TestMethodTestDescriptor Tests")
class TestMethodTestDescriptorTest {

    private EngineExecutionListener mockListener;
    private ArgumentContext mockArgumentContext;
    private ClassContext mockClassContext;
    private Configuration mockConfiguration;

    @SuppressWarnings("rawtypes")
    private Argument mockArgument;

    private Properties properties;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mockListener = mock(EngineExecutionListener.class);
        mockArgumentContext = mock(ArgumentContext.class);
        mockClassContext = mock(ClassContext.class);
        mockConfiguration = mock(Configuration.class);
        mockArgument = mock(Argument.class);
        properties = new Properties();

        when(mockArgumentContext.getClassContext()).thenReturn(mockClassContext);
        when(mockArgumentContext.getConfiguration()).thenReturn(mockConfiguration);
        when(mockArgumentContext.getArgument()).thenReturn(mockArgument);
        when(mockConfiguration.getProperties()).thenReturn(properties);
        when(mockArgument.getPayload()).thenReturn("test-payload");
        when(mockClassContext.getTestInstance()).thenReturn(new TestMethodClass());
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create descriptor with all parameters")
        void shouldCreateDescriptorWithAllParameters() throws Exception {
            UniqueId uniqueId = UniqueId.root("test", "method-id");
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");
            List<Method> beforeEachMethods = Collections.emptyList();
            List<Method> afterEachMethods = Collections.emptyList();

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    uniqueId, "Test Method", beforeEachMethods, testMethod, afterEachMethods);

            assertThat(descriptor).satisfies(d -> {
                assertThat(d.getUniqueId()).isEqualTo(uniqueId);
                assertThat(d.getDisplayName()).isEqualTo("Test Method");
                assertThat(d.getTestMethod()).isEqualTo(testMethod);
            });
        }

        @Test
        @DisplayName("Should have TEST type")
        void shouldHaveTestType() throws Exception {
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");
            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            assertThat(descriptor.getType()).isEqualTo(TestDescriptor.Type.TEST);
        }
    }

    @Nested
    @DisplayName("Source Tests")
    class SourceTests {

        @Test
        @DisplayName("Should return MethodSource")
        void shouldReturnMethodSource() throws Exception {
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");
            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            assertThat(descriptor.getSource())
                    .isPresent()
                    .get()
                    .isInstanceOf(MethodSource.class)
                    .extracting(source -> ((MethodSource) source).getJavaMethod())
                    .isEqualTo(testMethod);
        }
    }

    @Nested
    @DisplayName("Test Method Tests")
    class TestMethodTests {

        @Test
        @DisplayName("Should return test method")
        void shouldReturnTestMethod() throws Exception {
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");
            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            assertThat(descriptor.getTestMethod()).isEqualTo(testMethod);
        }
    }

    @Nested
    @DisplayName("Test Execution Tests")
    class TestExecutionTests {

        @Test
        @DisplayName("Should execute successful test")
        void shouldExecuteSuccessfulTest() throws Exception {
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");
            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isSuccess()).isTrue();
            });
        }

        @Test
        @DisplayName("Should execute beforeEach methods")
        void shouldExecuteBeforeEachMethods() throws Exception {
            Method beforeEach = TestMethodClass.class.getDeclaredMethod("beforeEachMethod");
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");
            List<Method> beforeEachMethods = Collections.singletonList(beforeEach);

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", beforeEachMethods, testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }

        @Test
        @DisplayName("Should execute afterEach methods")
        void shouldExecuteAfterEachMethods() throws Exception {
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");
            Method afterEach = TestMethodClass.class.getDeclaredMethod("afterEachMethod");
            List<Method> afterEachMethods = Collections.singletonList(afterEach);

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, afterEachMethods);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }

        @Test
        @DisplayName("Should handle test method failure")
        void shouldHandleTestMethodFailure() throws Exception {
            Method testMethod = TestMethodClass.class.getDeclaredMethod("failingTestMethod");
            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isFailure()).isTrue();
                assertThat(status.getThrowable()).isNotNull();
            });
        }

        @Test
        @DisplayName("Should handle ExecutionSkippedException in test method")
        void shouldHandleExecutionSkippedExceptionInTestMethod() throws Exception {
            Method testMethod = TestMethodClass.class.getDeclaredMethod("skippedTestMethod");
            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionSkipped(descriptor, "Skipped");
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isSkipped()).isTrue();
            });
        }

        @Test
        @DisplayName("Should handle ExecutionSkippedException in beforeEach")
        void shouldHandleExecutionSkippedExceptionInBeforeEach() throws Exception {
            Method beforeEach = TestMethodClass.class.getDeclaredMethod("skippedBeforeEachMethod");
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");
            List<Method> beforeEachMethods = Collections.singletonList(beforeEach);

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", beforeEachMethods, testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionSkipped(descriptor, "Skipped");
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isSkipped()).isTrue();
            });
        }

        @Test
        @DisplayName("Should ignore ExecutionSkippedException in afterEach")
        void shouldIgnoreExecutionSkippedExceptionInAfterEach() throws Exception {
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");
            Method afterEach = TestMethodClass.class.getDeclaredMethod("skippedAfterEachMethod");
            List<Method> afterEachMethods = Collections.singletonList(afterEach);

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, afterEachMethods);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isSuccess()).isTrue();
            });
        }

        @Test
        @DisplayName("Should handle exception in beforeEach")
        void shouldHandleExceptionInBeforeEach() throws Exception {
            Method beforeEach = TestMethodClass.class.getDeclaredMethod("failingBeforeEachMethod");
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");
            List<Method> beforeEachMethods = Collections.singletonList(beforeEach);

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", beforeEachMethods, testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isFailure()).isTrue();
            });
        }

        @Test
        @DisplayName("Should handle exception in afterEach")
        void shouldHandleExceptionInAfterEach() throws Exception {
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");
            Method afterEach = TestMethodClass.class.getDeclaredMethod("failingAfterEachMethod");
            List<Method> afterEachMethods = Collections.singletonList(afterEach);

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, afterEachMethods);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isFailure()).isTrue();
            });
        }
    }

    @Nested
    @DisplayName("Skip Tests")
    class SkipTests {

        @Test
        @DisplayName("Should skip test and notify listener")
        void shouldSkipTestAndNotifyListener() throws Exception {
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");
            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);

            descriptor.skip();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionSkipped(descriptor, "Skipped");
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isSkipped()).isTrue();
            });
        }
    }

    @Nested
    @DisplayName("Interceptor Integration Tests")
    class InterceptorIntegrationTests {

        @Test
        @DisplayName("Should invoke preBeforeEach interceptor")
        void shouldInvokePreBeforeEachInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(
                    TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.singletonList(interceptor), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(interceptor).preBeforeEach(mockArgumentContext);
        }

        @Test
        @DisplayName("Should invoke postBeforeEach interceptor")
        void shouldInvokePostBeforeEachInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(
                    TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED,
                    Collections.singletonList(interceptor),
                    descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(interceptor).postBeforeEach(mockArgumentContext, null);
        }

        @Test
        @DisplayName("Should invoke preTest interceptor")
        void shouldInvokePreTestInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(
                    TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.singletonList(interceptor), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(interceptor).preTest(mockArgumentContext, testMethod);
        }

        @Test
        @DisplayName("Should invoke postTest interceptor")
        void shouldInvokePostTestInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(
                    TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED,
                    Collections.singletonList(interceptor),
                    descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(interceptor).postTest(mockArgumentContext, testMethod, null);
        }

        @Test
        @DisplayName("Should invoke preAfterEach interceptor")
        void shouldInvokePreAfterEachInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(
                    TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.singletonList(interceptor), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(interceptor).preAfterEach(mockArgumentContext);
        }

        @Test
        @DisplayName("Should invoke postAfterEach interceptor")
        void shouldInvokePostAfterEachInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(
                    TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED,
                    Collections.singletonList(interceptor),
                    descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(interceptor).postAfterEach(mockArgumentContext, null);
        }

        @Test
        @DisplayName("Should pass throwable to postBeforeEach on failure")
        void shouldPassThrowableToPostBeforeEachOnFailure() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            Method beforeEach = TestMethodClass.class.getDeclaredMethod("failingBeforeEachMethod");
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");

            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.singletonList(beforeEach),
                    testMethod,
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(
                    TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED,
                    Collections.singletonList(interceptor),
                    descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(interceptor).postBeforeEach(eq(mockArgumentContext), any(RuntimeException.class));
        }
    }

    @Nested
    @DisplayName("State Machine Tests")
    class StateMachineTests {

        @Test
        @DisplayName("Should follow state machine START to BEFORE_EACH to TEST to AFTER_EACH to END")
        void shouldFollowStateMachineStartToBeforeEachToTestToAfterEachToEnd() throws Exception {
            Method testMethod = TestMethodClass.class.getDeclaredMethod("testMethod");
            TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(
                    UniqueId.root("test", "id"), "Test", Collections.emptyList(), testMethod, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isSuccess()).isTrue();
        }
    }

    // Test helper class
    @SuppressWarnings("unused")
    public static class TestMethodClass {
        public void beforeEachMethod() {}

        public void testMethod() {}

        public void afterEachMethod() {}

        public void failingTestMethod() {
            throw new RuntimeException("Test failed");
        }

        public void skippedTestMethod() {
            throw new Execution.ExecutionSkippedException("Test skipped");
        }

        public void skippedBeforeEachMethod() {
            throw new Execution.ExecutionSkippedException("BeforeEach skipped");
        }

        public void skippedAfterEachMethod() {
            throw new Execution.ExecutionSkippedException("AfterEach skipped");
        }

        public void failingBeforeEachMethod() {
            throw new RuntimeException("BeforeEach failed");
        }

        public void failingAfterEachMethod() {
            throw new RuntimeException("AfterEach failed");
        }
    }
}
