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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.junit.jupiter.api.*;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.verifyica.api.Argument;
import org.verifyica.api.ClassContext;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.Configuration;
import org.verifyica.api.EngineContext;
import org.verifyica.api.Execution;
import org.verifyica.engine.inject.Injector;

@DisplayName("TestClassTestDescriptor Tests")
public class TestClassTestDescriptorTest {

    private EngineExecutionListener mockListener;
    private EngineContext mockEngineContext;
    private Configuration mockConfiguration;
    private ExecutorService mockExecutorService;
    private List<ClassInterceptor> classInterceptors;
    private List<ClassInterceptor> classInterceptorsReversed;
    private Properties properties;

    @BeforeEach
    public void setUp() {
        mockListener = mock(EngineExecutionListener.class);
        mockEngineContext = mock(EngineContext.class);
        mockConfiguration = mock(Configuration.class);
        mockExecutorService = mock(ExecutorService.class);
        properties = new Properties();

        when(mockEngineContext.getConfiguration()).thenReturn(mockConfiguration);
        when(mockConfiguration.getProperties()).thenReturn(properties);

        classInterceptors = new ArrayList<>();
        classInterceptorsReversed = new ArrayList<>();
    }

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create descriptor with all parameters")
        public void shouldCreateDescriptorWithAllParameters() {
            final UniqueId uniqueId = UniqueId.root("test", "class-id");
            final Set<String> tags = new HashSet<>(Arrays.asList("tag1", "tag2"));
            final List<Method> prepareMethods = new ArrayList<>();
            final List<Method> concludeMethods = new ArrayList<>();

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    uniqueId, "Test Class Display", tags, SimpleTestClass.class, 4, prepareMethods, concludeMethods);

            assertThat(descriptor).satisfies(d -> {
                assertThat(d.getUniqueId()).isEqualTo(uniqueId);
                assertThat(d.getDisplayName()).isEqualTo("Test Class Display");
                assertThat(d.getTestClass()).isEqualTo(SimpleTestClass.class);
            });
        }

        @Test
        @DisplayName("Should enforce minimum parallelism of 1")
        public void shouldEnforceMinimumParallelismOfOne() {
            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    0,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
        }

        @Test
        @DisplayName("Should handle negative parallelism")
        public void shouldHandleNegativeParallelism() {
            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    -5,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
        }
    }

    @Nested
    @DisplayName("Source Tests")
    public class SourceTests {

        @Test
        @DisplayName("Should return ClassSource")
        public void shouldReturnClassSource() {
            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            assertThat(descriptor.getSource())
                    .isPresent()
                    .get()
                    .isInstanceOf(ClassSource.class)
                    .extracting(source -> ((ClassSource) source).getJavaClass())
                    .isEqualTo(SimpleTestClass.class);
        }
    }

    @Nested
    @DisplayName("Test Class Tests")
    public class TestClassTests {

        @Test
        @DisplayName("Should return test class")
        public void shouldReturnTestClass() {
            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            assertThat(descriptor.getTestClass()).isEqualTo(SimpleTestClass.class);
        }
    }

    @Nested
    @DisplayName("Test Execution Tests")
    public class TestExecutionTests {

        @Test
        @DisplayName("Should execute successful test lifecycle")
        public void shouldExecuteSuccessfulTestLifecycle() {
            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isSuccess()).isTrue();
            });
        }

        @Test
        @DisplayName("Should handle prepare method invocation")
        public void shouldHandlePrepareMethodInvocation() throws Exception {
            final Method prepareMethod = TestClassWithMethods.class.getDeclaredMethod("prepare");
            final List<Method> prepareMethods = Collections.singletonList(prepareMethod);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    TestClassWithMethods.class,
                    1,
                    prepareMethods,
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }

        @Test
        @DisplayName("Should handle conclude method invocation")
        public void shouldHandleConcludeMethodInvocation() throws Exception {
            final Method concludeMethod = TestClassWithMethods.class.getDeclaredMethod("conclude");
            final List<Method> concludeMethods = Collections.singletonList(concludeMethod);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    TestClassWithMethods.class,
                    1,
                    Collections.emptyList(),
                    concludeMethods);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }
    }

    @Nested
    @DisplayName("Skip Tests")
    public class SkipTests {

        @Test
        @DisplayName("Should skip test and notify listener")
        public void shouldSkipTestAndNotifyListener() {
            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);

            descriptor.skip();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionSkipped(descriptor, "Skipped");
            assertThat(descriptor.getTestDescriptorStatus()).satisfies(status -> {
                assertThat(status.isSkipped()).isTrue();
            });
        }

        @Test
        @DisplayName("Should skip children when parent is skipped")
        public void shouldSkipChildrenWhenParentIsSkipped() {
            final TestClassTestDescriptor parent = new TestClassTestDescriptor(
                    UniqueId.root("test", "parent-id"),
                    "Parent",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            final Argument<?> testArgument = Argument.of("test-arg", "test-payload");
            final TestArgumentTestDescriptor child = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "child-id"),
                    "Child",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            // Inject dependencies into child BEFORE adding to parent
            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, child);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), child);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), child);

            parent.addChild(child);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, parent);

            parent.skip();

            verify(mockListener).executionStarted(parent);
            verify(mockListener).executionSkipped(parent, "Skipped");
            assertThat(child.getTestDescriptorStatus().isSkipped()).isTrue();
        }
    }

    @Nested
    @DisplayName("Interceptor Integration Tests")
    public class InterceptorIntegrationTests {

        @Test
        @DisplayName("Should invoke preInstantiate interceptor")
        public void shouldInvokePreInstantiateInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptors.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(interceptor).preInstantiate(mockEngineContext, SimpleTestClass.class);
        }

        @Test
        @DisplayName("Should invoke postInstantiate interceptor")
        public void shouldInvokePostInstantiateInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptorsReversed.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(interceptor).postInstantiate(eq(mockEngineContext), eq(SimpleTestClass.class), any(), isNull());
        }

        @Test
        @DisplayName("Should invoke prePrepare interceptor")
        public void shouldInvokePrePrepareInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptors.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(interceptor).prePrepare(any(ClassContext.class));
        }

        @Test
        @DisplayName("Should invoke postPrepare interceptor")
        public void shouldInvokePostPrepareInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptorsReversed.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(interceptor).postPrepare(any(ClassContext.class), isNull());
        }

        @Test
        @DisplayName("Should invoke preConclude interceptor")
        public void shouldInvokePreConcludeInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptors.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(interceptor).preConclude(any(ClassContext.class));
        }

        @Test
        @DisplayName("Should invoke postConclude interceptor")
        public void shouldInvokePostConcludeInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptorsReversed.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(interceptor).postConclude(any(ClassContext.class), isNull());
        }

        @Test
        @DisplayName("Should invoke onDestroy interceptor")
        public void shouldInvokeOnDestroyInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptorsReversed.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(interceptor).onDestroy(any(ClassContext.class));
        }

        @Test
        @DisplayName("Should filter interceptors based on predicate")
        public void shouldFilterInterceptorsBasedOnPredicate() throws Throwable {
            final ClassInterceptor matchingInterceptor = mock(ClassInterceptor.class);
            final ClassInterceptor nonMatchingInterceptor = mock(ClassInterceptor.class);

            when(matchingInterceptor.predicate()).thenReturn(context -> true);
            when(nonMatchingInterceptor.predicate()).thenReturn(context -> false);

            classInterceptors.add(matchingInterceptor);
            classInterceptors.add(nonMatchingInterceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(matchingInterceptor).preInstantiate(any(), any());
            verify(nonMatchingInterceptor, never()).preInstantiate(any(), any());
        }

        @Test
        @DisplayName("Should include interceptor when predicate returns null")
        public void shouldIncludeInterceptorWhenPredicateReturnsNull() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            when(interceptor.predicate()).thenReturn(null);

            classInterceptors.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(interceptor).preInstantiate(any(), any());
        }

        @Test
        @DisplayName("Should handle exception in preInstantiate interceptor")
        public void shouldHandleExceptionInPreInstantiateInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            doThrow(new RuntimeException("preInstantiate failed"))
                    .when(interceptor)
                    .preInstantiate(any(), any());
            classInterceptors.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }

        @Test
        @DisplayName("Should handle exception in postInstantiate interceptor")
        public void shouldHandleExceptionInPostInstantiateInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            doThrow(new RuntimeException("postInstantiate failed"))
                    .when(interceptor)
                    .postInstantiate(any(), any(), any(), any());
            classInterceptorsReversed.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }

        @Test
        @DisplayName("Should handle exception in onDestroy interceptor")
        public void shouldHandleExceptionInOnDestroyInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            doThrow(new RuntimeException("onDestroy failed")).when(interceptor).onDestroy(any());
            classInterceptorsReversed.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("AutoCloseable Tests")
    public class AutoCloseableTests {

        @Test
        @DisplayName("Should close AutoCloseable test instance")
        public void shouldCloseAutoCloseableTestInstance() {
            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    AutoCloseableTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }

        @Test
        @DisplayName("Should handle exception during AutoCloseable close")
        public void shouldHandleExceptionDuringAutoCloseableClose() {
            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    FailingAutoCloseableTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }

        @Test
        @DisplayName("Should close AutoCloseable values in class context")
        public void shouldCloseAutoCloseableValuesInClassContext() {
            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }
    }

    @Nested
    @DisplayName("Parallel Execution Tests")
    public class ParallelExecutionTests {

        @Test
        @DisplayName("Should execute with single-threaded when parallelism is 1")
        public void shouldExecuteWithSingleThreadedWhenParallelismIsOne() {
            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }

        @Test
        @DisplayName("Should handle parallel execution when parallelism greater than 1")
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void shouldHandleParallelExecutionWhenParallelismGreaterThanOne() {
            final Future mockFuture = mock(Future.class);
            when(mockExecutorService.submit(any(Runnable.class))).thenReturn(mockFuture);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    4,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);
            Injector.inject(TestableTestDescriptor.TEST_ARGUMENT_EXECUTOR_SERVICE, mockExecutorService, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    public class ExceptionHandlingTests {

        @Test
        @DisplayName("Should handle exception in prepare method")
        public void shouldHandleExceptionInPrepareMethod() throws Exception {
            final Method prepareMethod = TestClassWithFailingPrepare.class.getDeclaredMethod("prepare");
            final List<Method> prepareMethods = Collections.singletonList(prepareMethod);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    TestClassWithFailingPrepare.class,
                    1,
                    prepareMethods,
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }

        @Test
        @DisplayName("Should handle exception in conclude method")
        public void shouldHandleExceptionInConcludeMethod() throws Exception {
            final Method concludeMethod = TestClassWithFailingConclude.class.getDeclaredMethod("conclude");
            final List<Method> concludeMethods = Collections.singletonList(concludeMethod);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    TestClassWithFailingConclude.class,
                    1,
                    Collections.emptyList(),
                    concludeMethods);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }

        @Test
        @DisplayName("Should skip test when ExecutionSkippedException thrown in prepare")
        public void shouldSkipTestWhenExecutionSkippedExceptionThrownInPrepare() throws Exception {
            final Method prepareMethod = TestClassWithSkippedPrepare.class.getDeclaredMethod("prepare");
            final List<Method> prepareMethods = Collections.singletonList(prepareMethod);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    TestClassWithSkippedPrepare.class,
                    1,
                    prepareMethods,
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            assertThat(descriptor.getTestDescriptorStatus().isSkipped()).isTrue();
        }

        @Test
        @DisplayName("Should handle ExecutionSkippedException in conclude as success")
        public void shouldHandleExecutionSkippedExceptionInConcludeAsSuccess() throws Exception {
            final Method concludeMethod = TestClassWithSkippedConclude.class.getDeclaredMethod("conclude");
            final List<Method> concludeMethods = Collections.singletonList(concludeMethod);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    TestClassWithSkippedConclude.class,
                    1,
                    Collections.emptyList(),
                    concludeMethods);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionStarted(descriptor);
            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }

        @Test
        @DisplayName("Should handle exception in prePrepare interceptor")
        public void shouldHandleExceptionInPrePrepareInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            doThrow(new RuntimeException("prePrepare failed")).when(interceptor).prePrepare(any());
            classInterceptors.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }

        @Test
        @DisplayName("Should handle exception in postPrepare interceptor")
        public void shouldHandleExceptionInPostPrepareInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            doThrow(new RuntimeException("postPrepare failed"))
                    .when(interceptor)
                    .postPrepare(any(), any());
            classInterceptorsReversed.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
        }

        @Test
        @DisplayName("Should handle exception in preConclude interceptor")
        public void shouldHandleExceptionInPreConcludeInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            doThrow(new RuntimeException("preConclude failed"))
                    .when(interceptor)
                    .preConclude(any());
            classInterceptors.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }

        @Test
        @DisplayName("Should handle exception in postConclude interceptor")
        public void shouldHandleExceptionInPostConcludeInterceptor() throws Throwable {
            final ClassInterceptor interceptor = mock(ClassInterceptor.class);
            doThrow(new RuntimeException("postConclude failed"))
                    .when(interceptor)
                    .postConclude(any(), any());
            classInterceptorsReversed.add(interceptor);

            final TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, descriptor);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, descriptor);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, descriptor);

            descriptor.test();

            verify(mockListener).executionFinished(eq(descriptor), any(TestExecutionResult.class));
            assertThat(descriptor.getTestDescriptorStatus().isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("Child Failure Tests")
    public class ChildFailureTests {

        @Test
        @DisplayName("Should collect failures from child test descriptors")
        public void shouldCollectFailuresFromChildTestDescriptors() {
            final TestClassTestDescriptor parent = new TestClassTestDescriptor(
                    UniqueId.root("test", "parent-id"),
                    "Parent",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            final Argument<?> testArgument = Argument.of("test-arg", "test-payload");
            final TestArgumentTestDescriptor child = new TestArgumentTestDescriptor(
                    UniqueId.root("test", "child-id"),
                    "Child",
                    0,
                    testArgument,
                    Collections.emptyList(),
                    Collections.emptyList());

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, child);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, Collections.emptyList(), child);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, Collections.emptyList(), child);

            parent.addChild(child);

            Injector.inject(TestableTestDescriptor.ENGINE_EXECUTION_LISTENER, mockListener, parent);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, parent);
            Injector.inject(TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, parent);
            Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, mockEngineContext, parent);

            // Simulate child failure
            child.setTestDescriptorStatus(TestDescriptorStatus.failed(new RuntimeException("Child failed")));

            parent.test();

            verify(mockListener).executionFinished(eq(parent), any(TestExecutionResult.class));
        }
    }

    // Test helper classes
    public static class SimpleTestClass {

        public SimpleTestClass() {
            // INTENTIONALLY EMPTY
        }
    }

    public static class TestClassWithMethods {

        public TestClassWithMethods() {
            // INTENTIONALLY EMPTY
        }

        public void prepare() {
            // INTENTIONALLY EMPTY
        }

        public void conclude() {
            // INTENTIONALLY EMPTY
        }
    }

    public static class TestClassWithFailingPrepare {

        public TestClassWithFailingPrepare() {
            // INTENTIONALLY EMPTY
        }

        public void prepare() {
            throw new RuntimeException("Prepare failed");
        }
    }

    public static class TestClassWithFailingConclude {

        public TestClassWithFailingConclude() {
            // INTENTIONALLY EMPTY
        }

        public void conclude() {
            throw new RuntimeException("Conclude failed");
        }
    }

    public static class TestClassWithSkippedPrepare {

        public TestClassWithSkippedPrepare() {
            // INTENTIONALLY EMPTY
        }

        public void prepare() {
            throw new Execution.ExecutionSkippedException("Skipped");
        }
    }

    public static class TestClassWithSkippedConclude {

        public TestClassWithSkippedConclude() {
            // INTENTIONALLY EMPTY
        }

        public void conclude() {
            throw new Execution.ExecutionSkippedException("Skipped");
        }
    }

    public static class AutoCloseableTestClass implements AutoCloseable {

        public boolean closed = false;

        @Override
        public void close() {
            closed = true;
        }
    }

    public static class FailingAutoCloseableTestClass implements AutoCloseable {

        @Override
        public void close() {
            throw new RuntimeException("Close failed");
        }
    }
}
