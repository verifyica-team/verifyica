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
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.verifyica.api.*;
import org.verifyica.engine.inject.Injector;

@DisplayName("TestArgumentTestDescriptor Tests")
class TestArgumentTestDescriptorTest {

    private EngineExecutionListener mockListener;
    private ArgumentContext mockArgumentContext;
    private ClassContext mockClassContext;
    private Configuration mockConfiguration;

    @SuppressWarnings("rawtypes")
    private Argument testArgument;

    private Properties properties;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mockListener = mock(EngineExecutionListener.class);
        mockArgumentContext = mock(ArgumentContext.class);
        mockClassContext = mock(ClassContext.class);
        mockConfiguration = mock(Configuration.class);
        testArgument = Argument.of("test-arg", "test-payload");
        properties = new Properties();

        when(mockArgumentContext.getClassContext()).thenReturn(mockClassContext);
        when(mockArgumentContext.getConfiguration()).thenReturn(mockConfiguration);
        when(mockArgumentContext.getArgument()).thenReturn(testArgument);
        when(mockClassContext.getConfiguration()).thenReturn(mockConfiguration);
        when(mockConfiguration.getProperties()).thenReturn(properties);
        when(mockClassContext.getTestInstance()).thenReturn(new TestArgumentClass());
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create descriptor with all parameters")
        void shouldCreateDescriptorWithAllParameters() {
            UniqueId uniqueId = UniqueId.root("test", "argument-id");
            List<Method> beforeAllMethods = new ArrayList<>();
            List<Method> afterAllMethods = new ArrayList<>();

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    uniqueId, "Test Argument", 0, testArgument, beforeAllMethods, afterAllMethods);

            assertThat(descriptor).satisfies(d -> {
                assertThat(d.getUniqueId()).isEqualTo(uniqueId);
                assertThat(d.getDisplayName()).isEqualTo("Test Argument");
                assertThat(d.getTestArgument()).isSameAs(testArgument);
            });
        }

        @Test
        @DisplayName("Should handle different argument indices")
        void shouldHandleDifferentArgumentIndices() {
            TestArgumentTestDescriptor descriptor1 = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id1"),
                    "Arg 0",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            TestArgumentTestDescriptor descriptor2 = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id2"),
                    "Arg 5",
                    5,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            assertThat(descriptor1.getTestArgument()).isSameAs(testArgument);
            assertThat(descriptor2.getTestArgument()).isSameAs(testArgument);
        }
    }

    @Nested
    @DisplayName("Test Argument Tests")
    class TestArgumentTests {

        @Test
        @DisplayName("Should return test argument")
        void shouldReturnTestArgument() {
            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            assertThat(descriptor.getTestArgument()).isSameAs(testArgument);
        }

        @Test
        @DisplayName("Should handle different argument types")
        void shouldHandleDifferentArgumentTypes() {
            Argument<String> stringArg = Argument.of("string-arg", "string");
            Argument<Integer> intArg = Argument.of("int-arg", 42);
            Argument<List<String>> listArg = Argument.of("list-arg", Arrays.asList("a", "b", "c"));

            TestArgumentTestDescriptor stringDescriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id1"),
                    "String",
                    0,
                    stringArg,
                    Collections.emptyList(),
                    Collections.emptyList());

            TestArgumentTestDescriptor intDescriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id2"), "Int", 1, intArg, Collections.emptyList(), Collections.emptyList());

            TestArgumentTestDescriptor listDescriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id3"), "List", 2, listArg, Collections.emptyList(), Collections.emptyList());

            assertThat(stringDescriptor.getTestArgument()).isSameAs(stringArg);
            assertThat(intDescriptor.getTestArgument()).isSameAs(intArg);
            assertThat(listDescriptor.getTestArgument()).isSameAs(listArg);
        }
    }

    @Nested
    @DisplayName("Test Execution Tests")
    class TestExecutionTests {

        @Test
        @DisplayName("Should execute successful test lifecycle")
        void shouldExecuteSuccessfulTestLifecycle() {
            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isSuccess()).isTrue();
            });
        }

        @Test
        @DisplayName("Should execute beforeAll methods")
        void shouldExecuteBeforeAllMethods() throws Exception {
            Method beforeAll = TestArgumentClass.class.getDeclaredMethod("beforeAllMethod");
            List<Method> beforeAllMethods = Collections.singletonList(beforeAll);

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, testArgument, beforeAllMethods, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }

        @Test
        @DisplayName("Should execute afterAll methods")
        void shouldExecuteAfterAllMethods() throws Exception {
            Method afterAll = TestArgumentClass.class.getDeclaredMethod("afterAllMethod");
            List<Method> afterAllMethods = Collections.singletonList(afterAll);

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, testArgument, Collections.emptyList(), afterAllMethods);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }

        @Test
        @DisplayName("Should handle ExecutionSkippedException in beforeAll")
        void shouldHandleExecutionSkippedExceptionInBeforeAll() throws Exception {
            Method beforeAll = TestArgumentClass.class.getDeclaredMethod("skippedBeforeAllMethod");
            List<Method> beforeAllMethods = Collections.singletonList(beforeAll);

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, testArgument, beforeAllMethods, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionSkipped(descriptor, "Skipped");
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isSkipped()).isTrue();
            });
        }

        @Test
        @DisplayName("Should ignore ExecutionSkippedException in afterAll")
        void shouldIgnoreExecutionSkippedExceptionInAfterAll() throws Exception {
            Method afterAll = TestArgumentClass.class.getDeclaredMethod("skippedAfterAllMethod");
            List<Method> afterAllMethods = Collections.singletonList(afterAll);

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, testArgument, Collections.emptyList(), afterAllMethods);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isSuccess()).isTrue();
            });
        }

        @Test
        @DisplayName("Should handle exception in beforeAll")
        void shouldHandleExceptionInBeforeAll() throws Exception {
            Method beforeAll = TestArgumentClass.class.getDeclaredMethod("failingBeforeAllMethod");
            List<Method> beforeAllMethods = Collections.singletonList(beforeAll);

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, testArgument, beforeAllMethods, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isFailure()).isTrue();
            });
        }

        @Test
        @DisplayName("Should handle exception in afterAll")
        void shouldHandleExceptionInAfterAll() throws Exception {
            Method afterAll = TestArgumentClass.class.getDeclaredMethod("failingAfterAllMethod");
            List<Method> afterAllMethods = Collections.singletonList(afterAll);

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, testArgument, Collections.emptyList(), afterAllMethods);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

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
        void shouldSkipTestAndNotifyListener() {
            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.skip();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionSkipped(descriptor, "Skipped");
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isSkipped()).isTrue();
            });
        }

        @Test
        @DisplayName("Should skip children when parent is skipped")
        void shouldSkipChildrenWhenParentIsSkipped() throws Exception {
            TestArgumentTestDescriptor parent = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "parent-id"),
                    "Parent",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            TestMethodTestDescriptor child = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child-id"),
                    "Child",
                    Collections.emptyList(),
                    TestArgumentClass.class.getDeclaredMethod("testMethod"),
                    Collections.emptyList());
            parent.addChild(child);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, parent);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), parent);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), parent);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, parent);

            parent.skip();

            verify(mockListener).executionStarted(parent);
            verify(mockListener).executionSkipped(parent, "Skipped");
            assertThat(child.getTestDescriptorStatus().isSkipped()).isTrue();
        }
    }

    @Nested
    @DisplayName("Child Test Execution Tests")
    class ChildTestExecutionTests {

        @Test
        @DisplayName("Should execute all child tests when no failures")
        void shouldExecuteAllChildTestsWhenNoFailures() throws Exception {
            TestArgumentTestDescriptor parent = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "parent-id"),
                    "Parent",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Method testMethod = TestArgumentClass.class.getDeclaredMethod("testMethod");

            TestMethodTestDescriptor child1 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child1-id"),
                    "Child1",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());
            TestMethodTestDescriptor child2 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child2-id"),
                    "Child2",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());
            TestMethodTestDescriptor child3 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child3-id"),
                    "Child3",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());

            // Inject dependencies into children
            for (TestMethodTestDescriptor child : Arrays.asList(child1, child2, child3)) {
                Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, child);
                Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), child);
                Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), child);
                Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, child);
            }

            parent.addChild(child1);
            parent.addChild(child2);
            parent.addChild(child3);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, parent);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), parent);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), parent);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, parent);

            parent.test();

            assertThat(child1.getTestDescriptorStatus().isSuccess()).isTrue();
            assertThat(child2.getTestDescriptorStatus().isSuccess()).isTrue();
            assertThat(child3.getTestDescriptorStatus().isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should skip remaining tests after first failure")
        void shouldSkipRemainingTestsAfterFirstFailure() throws Exception {
            TestArgumentTestDescriptor parent = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "parent-id"),
                    "Parent",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Method testMethod = TestArgumentClass.class.getDeclaredMethod("testMethod");
            Method failingTestMethod = TestArgumentClass.class.getDeclaredMethod("failingTestMethod");

            TestMethodTestDescriptor child1 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child1-id"),
                    "Child1",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());

            TestMethodTestDescriptor child2 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child2-id"),
                    "Child2",
                    Collections.emptyList(),
                    failingTestMethod,
                    Collections.emptyList());

            TestMethodTestDescriptor child3 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child3-id"),
                    "Child3",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());

            // Inject dependencies into children BEFORE adding to parent
            for (TestMethodTestDescriptor child : Arrays.asList(child1, child2, child3)) {
                Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, child);
                Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), child);
                Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), child);
                Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, child);
            }

            parent.addChild(child1);
            parent.addChild(child2);
            parent.addChild(child3);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, parent);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), parent);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), parent);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, parent);

            parent.test();

            assertThat(child1.getTestDescriptorStatus().isSuccess()).isTrue();
            assertThat(child2.getTestDescriptorStatus().isFailure()).isTrue();
            assertThat(child3.getTestDescriptorStatus().isSkipped()).isTrue();
        }

        // @Test
        // @DisplayName("Should continue after skipped test")
        // TODO fix test
        void shouldContinueAfterSkippedTest() throws Exception {
            TestArgumentTestDescriptor parent = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "parent-id"),
                    "Parent",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Method testMethod = TestArgumentClass.class.getDeclaredMethod("testMethod");

            TestMethodTestDescriptor child1 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child1-id"),
                    "Child1",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());

            TestMethodTestDescriptor child2 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child2-id"),
                    "Child2",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());

            TestMethodTestDescriptor child3 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child3-id"),
                    "Child3",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());

            // Inject dependencies into children BEFORE adding to parent
            for (TestMethodTestDescriptor child : Arrays.asList(child1, child2, child3)) {
                Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, child);
                Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), child);
                Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), child);
                Injector.inject(TestableTestDescriptor.ARGUMENT_CONTEXT, mockArgumentContext, child);
            }

            parent.addChild(child1);
            parent.addChild(child2);
            parent.addChild(child3);

            // Manually skip child2 before parent test
            child2.skip();

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, parent);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), parent);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), parent);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, parent);

            parent.test();

            assertThat(child1.getTestDescriptorStatus().isSuccess()).isTrue();
            assertThat(child2.getTestDescriptorStatus().isSkipped()).isTrue();
            assertThat(child3.getTestDescriptorStatus().isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("Interceptor Integration Tests")
    class InterceptorIntegrationTests {

        @Test
        @DisplayName("Should invoke preBeforeAll interceptor")
        void shouldInvokePreBeforeAllInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(
                    TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.singletonList(interceptor), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(interceptor).preBeforeAll(any(ArgumentContext.class));
        }

        @Test
        @DisplayName("Should invoke postBeforeAll interceptor")
        void shouldInvokePostBeforeAllInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(
                    TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED,
                    Collections.singletonList(interceptor),
                    descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(interceptor).postBeforeAll(any(ArgumentContext.class), isNull());
        }

        @Test
        @DisplayName("Should invoke preAfterAll interceptor")
        void shouldInvokePreAfterAllInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(
                    TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.singletonList(interceptor), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(interceptor).preAfterAll(any(ArgumentContext.class));
        }

        @Test
        @DisplayName("Should invoke postAfterAll interceptor")
        void shouldInvokePostAfterAllInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(
                    TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED,
                    Collections.singletonList(interceptor),
                    descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(interceptor).postAfterAll(any(ArgumentContext.class), isNull());
        }

        @Test
        @DisplayName("Should pass throwable to postBeforeAll on failure")
        void shouldPassThrowableToPostBeforeAllOnFailure() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            Method beforeAll = TestArgumentClass.class.getDeclaredMethod("failingBeforeAllMethod");

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    testArgument,
                    Collections.singletonList(beforeAll),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(
                    TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED,
                    Collections.singletonList(interceptor),
                    descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(interceptor).postBeforeAll(any(ArgumentContext.class), any(RuntimeException.class));
        }
    }

    @Nested
    @DisplayName("AutoCloseable Tests")
    class AutoCloseableTests {

        @Test
        @DisplayName("Should close AutoCloseable argument")
        void shouldCloseAutoCloseableArgument() {
            AutoCloseableArgument closeableArg = new AutoCloseableArgument();
            Argument<AutoCloseableArgument> argument = Argument.of("closeable-arg", closeableArg);

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, argument, Collections.emptyList(), Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }

        @Test
        @DisplayName("Should not fail when argument is not AutoCloseable")
        void shouldNotFailWhenArgumentIsNotAutoCloseable() {
            Argument<String> argument = Argument.of("string-arg", "not-closeable");

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, argument, Collections.emptyList(), Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("State Machine Tests")
    class StateMachineTests {

        @Test
        @DisplayName("Should follow state machine START to BEFORE_ALL to TEST to AFTER_ALL to CLOSE to CLEAN_UP to END")
        void shouldFollowStateMachineStartToBeforeAllToTestToAfterAllToCloseToCleanUpToEnd() {
            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should transition to SKIP on beforeAll failure")
        void shouldTransitionToSkipOnBeforeAllFailure() throws Exception {
            Method beforeAll = TestArgumentClass.class.getDeclaredMethod("failingBeforeAllMethod");

            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    testArgument,
                    Collections.singletonList(beforeAll),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("Argument Context Cleanup Tests")
    class ArgumentContextCleanupTests {

        @Test
        @DisplayName("Should clean up argument context map")
        void shouldCleanUpArgumentContextMap() {
            TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }
    }

    // Test helper classes
    @SuppressWarnings("unused")
    public static class TestArgumentClass {
        public void beforeAllMethod() {}

        public void afterAllMethod() {}

        public void testMethod() {}

        public void failingTestMethod() {
            throw new RuntimeException("Test failed");
        }

        public void skippedBeforeAllMethod() {
            throw new Execution.ExecutionSkippedException("BeforeAll skipped");
        }

        public void skippedAfterAllMethod() {
            throw new Execution.ExecutionSkippedException("AfterAll skipped");
        }

        public void failingBeforeAllMethod() {
            throw new RuntimeException("BeforeAll failed");
        }

        public void failingAfterAllMethod() {
            throw new RuntimeException("AfterAll failed");
        }
    }

    public static class AutoCloseableArgument implements AutoCloseable {
        public boolean closed = false;

        @Override
        public void close() {
            closed = true;
        }
    }
}
