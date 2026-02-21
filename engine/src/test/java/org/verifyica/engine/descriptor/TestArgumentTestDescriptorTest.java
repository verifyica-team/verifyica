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
public class TestArgumentTestDescriptorTest {

    private EngineExecutionListener mockListener;
    private ArgumentContext mockArgumentContext;
    private ClassContext mockClassContext;
    private Configuration mockConfiguration;

    @SuppressWarnings("rawtypes")
    private Argument testArgument;

    private Properties properties;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
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
    public class ConstructorTests {

        @Test
        @DisplayName("Should create descriptor with all parameters")
        public void shouldCreateDescriptorWithAllParameters() {
            final UniqueId uniqueId = UniqueId.root("test", "argument-id");
            final List<Method> beforeAllMethods = new ArrayList<>();
            final List<Method> afterAllMethods = new ArrayList<>();

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    uniqueId, "Test Argument", 0, testArgument, beforeAllMethods, afterAllMethods);

            assertThat(descriptor).satisfies(d -> {
                assertThat(d.getUniqueId()).isEqualTo(uniqueId);
                assertThat(d.getDisplayName()).isEqualTo("Test Argument");
                assertThat(d.getTestArgument()).isSameAs(testArgument);
            });
        }

        @Test
        @DisplayName("Should handle different argument indices")
        public void shouldHandleDifferentArgumentIndices() {
            final TestArgumentTestDescriptor descriptor1 = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id1"),
                    "Arg 0",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            final TestArgumentTestDescriptor descriptor2 = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id2"),
                    "Arg 5",
                    5,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            assertThat(descriptor1.getTestArgument()).isSameAs(testArgument);
            assertThat(descriptor2.getTestArgument()).isSameAs(testArgument);
        }

        @Test
        @DisplayName("Should handle zero argument index")
        public void shouldHandleZeroArgumentIndex() {
            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Arg 0",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            assertThat(descriptor.getTestDescriptorStatus().isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should handle large argument index")
        public void shouldHandleLargeArgumentIndex() {
            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Arg 999",
                    999,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            assertThat(descriptor.getTestDescriptorStatus().isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("Test Argument Tests")
    public class TestArgumentTests {

        @Test
        @DisplayName("Should return test argument")
        public void shouldReturnTestArgument() {
            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldHandleDifferentArgumentTypes() {
            final Argument<String> stringArg = Argument.of("string-arg", "string");
            final Argument<Integer> intArg = Argument.of("int-arg", 42);
            final Argument<List<String>> listArg = Argument.of("list-arg", Arrays.asList("a", "b", "c"));

            final TestArgumentTestDescriptor stringDescriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id1"),
                    "String",
                    0,
                    stringArg,
                    Collections.emptyList(),
                    Collections.emptyList());

            final TestArgumentTestDescriptor intDescriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id2"), "Int", 1, intArg, Collections.emptyList(), Collections.emptyList());

            final TestArgumentTestDescriptor listDescriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id3"), "List", 2, listArg, Collections.emptyList(), Collections.emptyList());

            assertThat(stringDescriptor.getTestArgument()).isSameAs(stringArg);
            assertThat(intDescriptor.getTestArgument()).isSameAs(intArg);
            assertThat(listDescriptor.getTestArgument()).isSameAs(listArg);
        }
    }

    @Nested
    @DisplayName("Test Execution Tests")
    public class TestExecutionTests {

        @Test
        @DisplayName("Should execute successful test lifecycle")
        public void shouldExecuteSuccessfulTestLifecycle() {
            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldExecuteBeforeAllMethods() throws Exception {
            final Method beforeAll = TestArgumentClass.class.getDeclaredMethod("beforeAllMethod");
            final List<Method> beforeAllMethods = Collections.singletonList(beforeAll);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldExecuteAfterAllMethods() throws Exception {
            final Method afterAll = TestArgumentClass.class.getDeclaredMethod("afterAllMethod");
            final List<Method> afterAllMethods = Collections.singletonList(afterAll);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldHandleExecutionSkippedExceptionInBeforeAll() throws Exception {
            final Method beforeAll = TestArgumentClass.class.getDeclaredMethod("skippedBeforeAllMethod");
            final List<Method> beforeAllMethods = Collections.singletonList(beforeAll);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldIgnoreExecutionSkippedExceptionInAfterAll() throws Exception {
            final Method afterAll = TestArgumentClass.class.getDeclaredMethod("skippedAfterAllMethod");
            final List<Method> afterAllMethods = Collections.singletonList(afterAll);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldHandleExceptionInBeforeAll() throws Exception {
            final Method beforeAll = TestArgumentClass.class.getDeclaredMethod("failingBeforeAllMethod");
            final List<Method> beforeAllMethods = Collections.singletonList(beforeAll);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldHandleExceptionInAfterAll() throws Exception {
            final Method afterAll = TestArgumentClass.class.getDeclaredMethod("failingAfterAllMethod");
            final List<Method> afterAllMethods = Collections.singletonList(afterAll);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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

        @Test
        @DisplayName("Should execute multiple beforeAll methods")
        public void shouldExecuteMultipleBeforeAllMethods() throws Exception {
            final Method beforeAll1 = TestArgumentClass.class.getDeclaredMethod("beforeAllMethod1");
            final Method beforeAll2 = TestArgumentClass.class.getDeclaredMethod("beforeAllMethod2");
            final List<Method> beforeAllMethods = Arrays.asList(beforeAll1, beforeAll2);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, testArgument, beforeAllMethods, Collections.emptyList());

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
        @DisplayName("Should execute multiple afterAll methods")
        public void shouldExecuteMultipleAfterAllMethods() throws Exception {
            final Method afterAll1 = TestArgumentClass.class.getDeclaredMethod("afterAllMethod1");
            final Method afterAll2 = TestArgumentClass.class.getDeclaredMethod("afterAllMethod2");
            final List<Method> afterAllMethods = Arrays.asList(afterAll1, afterAll2);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, testArgument, Collections.emptyList(), afterAllMethods);

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
        @DisplayName("Should stop beforeAll execution on first failure")
        public void shouldStopBeforeAllExecutionOnFirstFailure() throws Exception {
            final Method beforeAll1 = TestArgumentClass.class.getDeclaredMethod("failingBeforeAllMethod");
            final Method beforeAll2 = TestArgumentClass.class.getDeclaredMethod("beforeAllMethod1");
            final List<Method> beforeAllMethods = Arrays.asList(beforeAll1, beforeAll2);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, testArgument, beforeAllMethods, Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }

        @Test
        @DisplayName("Should continue afterAll execution despite failure")
        public void shouldContinueAfterAllExecutionDespiteFailure() throws Exception {
            final Method afterAll1 = TestArgumentClass.class.getDeclaredMethod("failingAfterAllMethod");
            final Method afterAll2 = TestArgumentClass.class.getDeclaredMethod("afterAllMethod1");
            final List<Method> afterAllMethods = Arrays.asList(afterAll1, afterAll2);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, testArgument, Collections.emptyList(), afterAllMethods);

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
    @DisplayName("Skip Tests")
    public class SkipTests {

        @Test
        @DisplayName("Should skip test and notify listener")
        public void shouldSkipTestAndNotifyListener() {
            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldSkipChildrenWhenParentIsSkipped() throws Exception {
            final TestArgumentTestDescriptor parent = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "parent-id"),
                    "Parent",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            final TestMethodTestDescriptor child = new TestMethodTestDescriptor(
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
    public class ChildTestExecutionTests {

        @Test
        @DisplayName("Should execute all child tests when no failures")
        public void shouldExecuteAllChildTestsWhenNoFailures() throws Exception {
            final TestArgumentTestDescriptor parent = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "parent-id"),
                    "Parent",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            final Method testMethod = TestArgumentClass.class.getDeclaredMethod("testMethod");

            final TestMethodTestDescriptor child1 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child1-id"),
                    "Child1",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());
            final TestMethodTestDescriptor child2 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child2-id"),
                    "Child2",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());
            final TestMethodTestDescriptor child3 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child3-id"),
                    "Child3",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());

            // Inject dependencies into children
            for (final TestMethodTestDescriptor child : Arrays.asList(child1, child2, child3)) {
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
        public void shouldSkipRemainingTestsAfterFirstFailure() throws Exception {
            final TestArgumentTestDescriptor parent = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "parent-id"),
                    "Parent",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            final Method testMethod = TestArgumentClass.class.getDeclaredMethod("testMethod");
            final Method failingTestMethod = TestArgumentClass.class.getDeclaredMethod("failingTestMethod");

            final TestMethodTestDescriptor child1 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child1-id"),
                    "Child1",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());

            final TestMethodTestDescriptor child2 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child2-id"),
                    "Child2",
                    Collections.emptyList(),
                    failingTestMethod,
                    Collections.emptyList());

            final TestMethodTestDescriptor child3 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child3-id"),
                    "Child3",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());

            // Inject dependencies into children BEFORE adding to parent
            for (final TestMethodTestDescriptor child : Arrays.asList(child1, child2, child3)) {
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
        public void shouldContinueAfterSkippedTest() throws Exception {
            final TestArgumentTestDescriptor parent = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "parent-id"),
                    "Parent",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            final Method testMethod = TestArgumentClass.class.getDeclaredMethod("testMethod");

            final TestMethodTestDescriptor child1 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child1-id"),
                    "Child1",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());

            final TestMethodTestDescriptor child2 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child2-id"),
                    "Child2",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());

            final TestMethodTestDescriptor child3 = new TestMethodTestDescriptor(
                    UniqueId.root("test", "child3-id"),
                    "Child3",
                    Collections.emptyList(),
                    testMethod,
                    Collections.emptyList());

            // Inject dependencies into children BEFORE adding to parent
            for (final TestMethodTestDescriptor child : Arrays.asList(child1, child2, child3)) {
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
    public class InterceptorIntegrationTests {

        @Test
        @DisplayName("Should invoke preBeforeAll interceptor")
        public void shouldInvokePreBeforeAllInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldInvokePostBeforeAllInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldInvokePreAfterAllInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldInvokePostAfterAllInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldPassThrowableToPostBeforeAllOnFailure() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            final Method beforeAll = TestArgumentClass.class.getDeclaredMethod("failingBeforeAllMethod");

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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

        @Test
        @DisplayName("Should handle exception in preBeforeAll interceptor")
        public void shouldHandleExceptionInPreBeforeAllInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            doThrow(new RuntimeException("preBeforeAll failed"))
                    .when(interceptor)
                    .preBeforeAll(any(ArgumentContext.class));

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }

        @Test
        @DisplayName("Should handle exception in postBeforeAll interceptor")
        public void shouldHandleExceptionInPostBeforeAllInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            doThrow(new RuntimeException("postBeforeAll failed"))
                    .when(interceptor)
                    .postBeforeAll(any(ArgumentContext.class), any());

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }

        @Test
        @DisplayName("Should handle exception in preAfterAll interceptor")
        public void shouldHandleExceptionInPreAfterAllInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            doThrow(new RuntimeException("preAfterAll failed"))
                    .when(interceptor)
                    .preAfterAll(any(ArgumentContext.class));

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }

        @Test
        @DisplayName("Should handle exception in postAfterAll interceptor")
        public void shouldHandleExceptionInPostAfterAllInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            doThrow(new RuntimeException("postAfterAll failed"))
                    .when(interceptor)
                    .postAfterAll(any(ArgumentContext.class), any());

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }

        @Test
        @DisplayName("Should pass throwable to postAfterAll on afterAll failure")
        public void shouldPassThrowableToPostAfterAllOnAfterAllFailure() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            final Method afterAll = TestArgumentClass.class.getDeclaredMethod("failingAfterAllMethod");

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.singletonList(afterAll));

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(
                    TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED,
                    Collections.singletonList(interceptor),
                    descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(interceptor).postAfterAll(any(ArgumentContext.class), any(RuntimeException.class));
        }

        @Test
        @DisplayName("Should invoke multiple interceptors in order")
        public void shouldInvokeMultipleInterceptorsInOrder() throws Throwable {
            final ClassInterceptor interceptor1 = mock(ClassInterceptor.class);
            final ClassInterceptor interceptor2 = mock(ClassInterceptor.class);
            final List<ClassInterceptor> interceptors = Arrays.asList(interceptor1, interceptor2);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, interceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, interceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(interceptor1).preBeforeAll(any(ArgumentContext.class));
            verify(interceptor2).preBeforeAll(any(ArgumentContext.class));
            verify(interceptor1).preAfterAll(any(ArgumentContext.class));
            verify(interceptor2).preAfterAll(any(ArgumentContext.class));
        }
    }

    @Nested
    @DisplayName("AutoCloseable Tests")
    public class AutoCloseableTests {

        @Test
        @DisplayName("Should close AutoCloseable argument")
        public void shouldCloseAutoCloseableArgument() {
            final AutoCloseableArgument closeableArg = new AutoCloseableArgument();
            final Argument<AutoCloseableArgument> argument = Argument.of("closeable-arg", closeableArg);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldNotFailWhenArgumentIsNotAutoCloseable() {
            final Argument<String> argument = Argument.of("string-arg", "not-closeable");

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, argument, Collections.emptyList(), Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should close AutoCloseable payload")
        public void shouldCloseAutoCloseablePayload() {
            final AutoCloseableArgument closeablePayload = new AutoCloseableArgument();
            final Argument<AutoCloseableArgument> argument = Argument.of("closeable-payload", closeablePayload);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, argument, Collections.emptyList(), Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(closeablePayload.closed).isTrue();
        }

        @Test
        @DisplayName("Should close both payload and argument when both are AutoCloseable and different")
        public void shouldCloseBothPayloadAndArgumentWhenBothAreAutoCloseableAndDifferent() {
            final AutoCloseableArgument closeablePayload = new AutoCloseableArgument();
            final AutoCloseableWrapperArgument wrapperArgument = new AutoCloseableWrapperArgument(closeablePayload);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    0,
                    wrapperArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(wrapperArgument.closed).isTrue();
            assertThat(closeablePayload.closed).isTrue();
        }

        @Test
        @DisplayName("Should handle exception when closing AutoCloseable payload")
        public void shouldHandleExceptionWhenClosingAutoCloseablePayload() {
            final FailingAutoCloseable failingPayload = new FailingAutoCloseable();
            final Argument<FailingAutoCloseable> argument = Argument.of("failing-payload", failingPayload);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, argument, Collections.emptyList(), Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(failingPayload.closeAttempted).isTrue();
        }
    }

    @Nested
    @DisplayName("State Machine Tests")
    public class StateMachineTests {

        @Test
        @DisplayName("Should follow state machine START to BEFORE_ALL to TEST to AFTER_ALL to CLOSE to CLEAN_UP to END")
        public void shouldFollowStateMachineStartToBeforeAllToTestToAfterAllToCloseToCleanUpToEnd() {
            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
        public void shouldTransitionToSkipOnBeforeAllFailure() throws Exception {
            final Method beforeAll = TestArgumentClass.class.getDeclaredMethod("failingBeforeAllMethod");

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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
    public class ArgumentContextCleanupTests {

        @Test
        @DisplayName("Should clean up argument context map")
        public void shouldCleanUpArgumentContextMap() {
            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
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

        @Test
        @DisplayName("Should close AutoCloseable values in argument context map")
        public void shouldCloseAutoCloseableValuesInArgumentContextMap() {
            final CloseableResource closeableResource = new CloseableResource();
            final Argument<CloseableResource> argument = Argument.of("resource-arg", closeableResource);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, argument, Collections.emptyList(), Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(closeableResource.closed).isTrue();
        }

        @Test
        @DisplayName("Should handle exception when closing AutoCloseable in argument context map")
        public void shouldHandleExceptionWhenClosingAutoCloseableInArgumentContextMap() {
            final FailingCloseableResource failingResource = new FailingCloseableResource();
            final Argument<FailingCloseableResource> argument = Argument.of("failing-arg", failingResource);

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, argument, Collections.emptyList(), Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_CONTEXT, mockClassContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(failingResource.closeAttempted).isTrue();
        }

        @Test
        @DisplayName("Should close multiple AutoCloseable values in argument context map")
        public void shouldCloseMultipleAutoCloseableValuesInArgumentContextMap() {
            final CloseableResource resource1 = new CloseableResource();
            final CloseableResource resource2 = new CloseableResource();
            final Argument<String> argument = Argument.of("test-arg", "test-payload");

            final TestArgumentTestDescriptor descriptor = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "id"), "Test", 0, argument, Collections.emptyList(), Collections.emptyList());

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

        public void beforeAllMethod() {
            // INTENTIONALLY EMPTY
        }

        public void afterAllMethod() {
            // INTENTIONALLY EMPTY
        }

        public void testMethod() {
            // INTENTIONALLY EMPTY
        }

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

        public void beforeAllMethod1() {
            // INTENTIONALLY EMPTY
        }

        public void beforeAllMethod2() {
            // INTENTIONALLY EMPTY
        }

        public void afterAllMethod1() {
            // INTENTIONALLY EMPTY
        }

        public void afterAllMethod2() {
            // INTENTIONALLY EMPTY
        }
    }

    public static class AutoCloseableArgument implements AutoCloseable {

        public boolean closed = false;

        @Override
        public void close() {
            closed = true;
        }
    }

    public static class AutoCloseableWrapperArgument implements Argument<AutoCloseableArgument>, AutoCloseable {

        private final AutoCloseableArgument payload;
        public boolean closed = false;

        public AutoCloseableWrapperArgument(final AutoCloseableArgument payload) {
            this.payload = payload;
        }

        @Override
        public String getName() {
            return "wrapper-arg";
        }

        @Override
        public AutoCloseableArgument getPayload() {
            return payload;
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    public static class FailingAutoCloseable implements AutoCloseable {

        public boolean closeAttempted = false;

        @Override
        public void close() throws Exception {
            closeAttempted = true;
            throw new RuntimeException("Close failed");
        }
    }

    public static class CloseableResource implements AutoCloseable {

        public boolean closed = false;

        @Override
        public void close() {
            closed = true;
        }
    }

    public static class FailingCloseableResource implements AutoCloseable {

        public boolean closeAttempted = false;

        @Override
        public void close() throws Exception {
            closeAttempted = true;
            throw new RuntimeException("Close failed");
        }
    }
}
