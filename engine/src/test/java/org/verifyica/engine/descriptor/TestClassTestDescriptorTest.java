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
import org.verifyica.engine.inject.Injector;

@DisplayName("TestClassTestDescriptor Tests")
class TestClassTestDescriptorTest {

    private EngineExecutionListener mockListener;
    private EngineContext mockEngineContext;
    private Configuration mockConfiguration;
    private ExecutorService mockExecutorService;
    private List<ClassInterceptor> classInterceptors;
    private List<ClassInterceptor> classInterceptorsReversed;
    private Properties properties;

    @BeforeEach
    void setUp() {
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
    class ConstructorTests {

        @Test
        @DisplayName("Should create descriptor with all parameters")
        void shouldCreateDescriptorWithAllParameters() {
            UniqueId uniqueId = UniqueId.root("test", "class-id");
            Set<String> tags = new HashSet<>(Arrays.asList("tag1", "tag2"));
            List<Method> prepareMethods = new ArrayList<>();
            List<Method> concludeMethods = new ArrayList<>();

            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    uniqueId, "Test Class Display", tags, SimpleTestClass.class, 4, prepareMethods, concludeMethods);

            assertThat(descriptor).satisfies(d -> {
                assertThat(d.getUniqueId()).isEqualTo(uniqueId);
                assertThat(d.getDisplayName()).isEqualTo("Test Class Display");
                assertThat(d.getTestClass()).isEqualTo(SimpleTestClass.class);
            });
        }

        @Test
        @DisplayName("Should enforce minimum parallelism of 1")
        void shouldEnforceMinimumParallelismOfOne() {
            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
        void shouldHandleNegativeParallelism() {
            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
    class SourceTests {

        @Test
        @DisplayName("Should return ClassSource")
        void shouldReturnClassSource() {
            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
    class TestClassTests {

        @Test
        @DisplayName("Should return test class")
        void shouldReturnTestClass() {
            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
    class TestExecutionTests {

        @Test
        @DisplayName("Should execute successful test lifecycle")
        void shouldExecuteSuccessfulTestLifecycle() {
            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
        void shouldHandlePrepareMethodInvocation() throws Exception {
            Method prepareMethod = TestClassWithMethods.class.getDeclaredMethod("prepare");
            List<Method> prepareMethods = Collections.singletonList(prepareMethod);

            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
        void shouldHandleConcludeMethodInvocation() throws Exception {
            Method concludeMethod = TestClassWithMethods.class.getDeclaredMethod("conclude");
            List<Method> concludeMethods = Collections.singletonList(concludeMethod);

            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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

        // @Test
        // @DisplayName("Should handle exception in test execution")
        // TODO fix test
        void shouldHandleExceptionInTestExecution() {
            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
                    UniqueId.root("test", "id"),
                    "Test",
                    Collections.emptySet(),
                    TestClassThatThrows.class,
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
                assertThat(status.isFailure()).isTrue();
                assertThat(status.getThrowable()).isNotNull();
            });
        }
    }

    @Nested
    @DisplayName("Skip Tests")
    class SkipTests {

        @Test
        @DisplayName("Should skip test and notify listener")
        void shouldSkipTestAndNotifyListener() {
            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
        void shouldSkipChildrenWhenParentIsSkipped() {
            TestClassTestDescriptor parent = new TestClassTestDescriptor(
                    UniqueId.root("test", "parent-id"),
                    "Parent",
                    Collections.emptySet(),
                    SimpleTestClass.class,
                    1,
                    Collections.emptyList(),
                    Collections.emptyList());

            Argument<?> testArgument = Argument.of("test-arg", "test-payload");
            TestArgumentTestDescriptor child = new TestArgumentTestDescriptor(
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
    class InterceptorIntegrationTests {

        @Test
        @DisplayName("Should invoke preInstantiate interceptor")
        void shouldInvokePreInstantiateInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptors.add(interceptor);

            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
        void shouldInvokePostInstantiateInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptorsReversed.add(interceptor);

            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
        void shouldInvokePrePrepareInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptors.add(interceptor);

            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
        void shouldInvokePostPrepareInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptorsReversed.add(interceptor);

            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
        void shouldInvokePreConcludeInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptors.add(interceptor);

            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
        void shouldInvokePostConcludeInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptorsReversed.add(interceptor);

            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
        void shouldInvokeOnDestroyInterceptor() throws Throwable {
            ClassInterceptor interceptor = mock(ClassInterceptor.class);
            classInterceptorsReversed.add(interceptor);

            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
    }

    @Nested
    @DisplayName("AutoCloseable Tests")
    class AutoCloseableTests {

        @Test
        @DisplayName("Should close AutoCloseable test instance")
        void shouldCloseAutoCloseableTestInstance() {
            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
    }

    @Nested
    @DisplayName("Parallel Execution Tests")
    class ParallelExecutionTests {

        @Test
        @DisplayName("Should execute with single-threaded when parallelism is 1")
        void shouldExecuteWithSingleThreadedWhenParallelismIsOne() {
            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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
        @SuppressWarnings("unchecked")
        void shouldHandleParallelExecutionWhenParallelismGreaterThanOne() {
            Future mockFuture = mock(Future.class);
            when(mockExecutorService.submit(any(Runnable.class))).thenReturn(mockFuture);

            TestClassTestDescriptor descriptor = new TestClassTestDescriptor(
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

    // Test helper classes
    public static class SimpleTestClass {
        public SimpleTestClass() {}
    }

    public static class TestClassWithMethods {
        public TestClassWithMethods() {}

        public void prepare() {}

        public void conclude() {}
    }

    public static class TestClassThatThrows {
        public TestClassThatThrows() {
            throw new RuntimeException("Constructor failed");
        }
    }

    public static class AutoCloseableTestClass implements AutoCloseable {
        public boolean closed = false;

        @Override
        public void close() {
            closed = true;
        }
    }
}
