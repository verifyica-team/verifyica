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

package org.verifyica.engine.descriptor;

import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.verifyica.api.Configuration;
import org.verifyica.api.EngineContext;
import org.verifyica.api.interceptor.EngineInterceptorContext;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.injection.Inject;
import org.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.verifyica.engine.interceptor.EngineInterceptorRegistry;

/** Class to implement TestableTestDescriptor */
public abstract class TestableTestDescriptor extends AbstractTestDescriptor {

    /**
     * Predicate to filter TestableTestDescriptors
     */
    public static final Predicate<TestDescriptor> TESTABLE_TEST_DESCRIPTOR_FILTER =
            testDescriptor -> testDescriptor instanceof TestableTestDescriptor;

    /**
     * Function to map to TestableTestDescriptor
     */
    public static final Function<TestDescriptor, TestableTestDescriptor> TESTABLE_TEST_DESCRIPTOR_MAPPER =
            testDescriptor -> (TestableTestDescriptor) testDescriptor;

    /**
     * Configuration
     */
    @Inject
    protected Configuration configuration;

    /**
     * EngineExecutionListener
     */
    @Inject
    protected EngineExecutionListener engineExecutionListener;

    /**
     * ClassInterceptorRegistry
     */
    @Inject
    protected ClassInterceptorRegistry classInterceptorRegistry;

    /**
     * EngineInterceptorRegistry
     */
    @Inject
    protected EngineInterceptorRegistry engineInterceptorRegistry;

    /**
     * EngineContext
     */
    @Inject
    protected EngineContext engineContext;

    /**
     * EngineInterceptorContext
     */
    @Inject
    protected EngineInterceptorContext engineInterceptorContext;

    private TestDescriptorStatus testDescriptorStatus;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     */
    protected TestableTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    /**
     * Method to test the test descriptor
     *
     * @return this
     */
    public abstract TestableTestDescriptor test();

    /**
     * Method to skip the test descriptor and all children
     */
    public final void skip() {
        engineExecutionListener.executionStarted(this);

        getChildren().stream().map(TESTABLE_TEST_DESCRIPTOR_MAPPER).forEach(TestableTestDescriptor::skip);

        engineExecutionListener.executionSkipped(this, "Skipped");

        setTestDescriptorStatus(TestDescriptorStatus.skipped());
    }

    /**
     * Method to get the test descriptor status
     *
     * @return the test descriptor status
     */
    public TestDescriptorStatus getTestDescriptorStatus() {
        return testDescriptorStatus;
    }

    /**
     * Method to set the test descriptor status
     *
     * @param testDescriptorStatus testDescriptorStatus
     */
    protected void setTestDescriptorStatus(TestDescriptorStatus testDescriptorStatus) {
        this.testDescriptorStatus = testDescriptorStatus;
    }

    /**
     * Method to print a stack trace in AnsiColor.TEXT_RED_BOLD
     *
     * @param throwable throwable
     */
    protected static void printStackTrace(Throwable throwable) {
        StackTracePrinter.printStackTrace(throwable, AnsiColor.TEXT_RED_BOLD, System.err);
    }

    /**
     * Method to check an Object has been injected
     *
     * @param object object
     * @param message message
     */
    protected static void checkInjected(Object object, String message) {
        if (object == null) {
            throw new IllegalStateException(message);
        }
    }
}
