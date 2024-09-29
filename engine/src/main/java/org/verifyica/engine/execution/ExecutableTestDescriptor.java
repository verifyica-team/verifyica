/*
 * Copyright (C) 2024 The Verifyica project authors
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

package org.verifyica.engine.execution;

import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.verifyica.api.Configuration;
import org.verifyica.api.EngineContext;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.injection.Inject;
import org.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.verifyica.engine.interceptor.EngineInterceptorRegistry;

public abstract class ExecutableTestDescriptor extends AbstractTestDescriptor {

    public static final Predicate<TestDescriptor> EXECUTABLE_TEST_DESCRIPTOR_FILTER = new Predicate<TestDescriptor>() {
        @Override
        public boolean test(TestDescriptor testDescriptor) {
            return testDescriptor instanceof ExecutableTestDescriptor;
        }
    };

    public static final Function<TestDescriptor, ExecutableTestDescriptor> EXECUTABLE_TEST_DESCRIPTOR_MAPPER =
            testDescriptor -> (ExecutableTestDescriptor) testDescriptor;

    @Inject
    protected Configuration configuration;

    @Inject
    protected EngineExecutionListener engineExecutionListener;

    @Inject
    protected ClassInterceptorRegistry classInterceptorRegistry;

    @Inject
    protected EngineInterceptorRegistry engineInterceptorRegistry;

    @Inject
    protected EngineContext engineContext;

    private ExecutionResult executionResult;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     */
    protected ExecutableTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    /**
     * Method to test the test descriptor
     */
    public abstract ExecutableTestDescriptor test();

    /**
     * Method to skip the test descriptor and all children
     */
    public final void skip() {
        engineExecutionListener.executionStarted(this);

        getChildren().stream().map(EXECUTABLE_TEST_DESCRIPTOR_MAPPER).forEach(ExecutableTestDescriptor::skip);

        engineExecutionListener.executionSkipped(this, "Skipped");

        setExecutionResult(ExecutionResult.skipped());
    }

    protected void setExecutionResult(ExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    public ExecutionResult getExecutionResult() {
        return executionResult;
    }

    protected static void printStackTrace(Throwable throwable) {
        StackTracePrinter.printStackTrace(throwable, AnsiColor.TEXT_RED_BOLD, System.err);
    }

    protected static void checkInjected(Object object, String message) {
        if (object == null) {
            throw new IllegalStateException(message);
        }
    }
}
