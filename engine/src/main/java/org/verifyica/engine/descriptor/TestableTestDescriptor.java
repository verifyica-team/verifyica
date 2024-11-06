/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

import static java.lang.String.format;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.verifyica.api.Configuration;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.common.Throttle;
import org.verifyica.engine.exception.TestClassDefinitionException;
import org.verifyica.engine.inject.Inject;
import org.verifyica.engine.inject.Named;

/** Class to implement TestableTestDescriptor */
public abstract class TestableTestDescriptor extends AbstractTestDescriptor {

    /** Named annotation field constant */
    public static final String ENGINE_EXECUTION_LISTENER = "engineExecutionListener";

    /** Named annotation field constant */
    public static final String CLASS_INTERCEPTORS = "classInterceptors";

    /** Named annotation field constant */
    public static final String CLASS_INTERCEPTORS_REVERSED = "classInterceptorsReversed";

    /** Named annotation field constant */
    public static final String ARGUMENT_EXECUTOR_SERVICE = "argumentExecutorService";

    /** Named annotation field constant */
    public static final String ENGINE_CONTEXT = "engineContext";

    /** Named annotation field constant */
    public static final String CLASS_CONTEXT = "classContext";

    /** Named annotation field constant */
    public static final String ARGUMENT_CONTEXT = "argumentContext";

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

    @Inject
    @Named(ENGINE_EXECUTION_LISTENER)
    private EngineExecutionListener engineExecutionListener;

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
     * Method to skip the test descriptor
     */
    public abstract void skip();

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
     * Method to invoke a Method.
     *
     * <p>Loops through each argument. If an argument type matches, invokes the method with the argument and returns.
     *
     * @param method method
     * @param instance instant
     * @param arguments arguments
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     */
    protected static void invoke(Method method, Object instance, List<Object> arguments)
            throws InvocationTargetException, IllegalAccessException {
        invoke(method, instance, arguments, false);
    }

    /**
     * Method to invoke a Method.
     *
     * <p>Loops through each argument. If an argument type matches, invokes the method with the argument and returns.
     *
     * @param method method
     * @param instance instant
     * @param arguments arguments
     * @param noParameters noParameters
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     */
    protected static void invoke(Method method, Object instance, List<Object> arguments, boolean noParameters)
            throws InvocationTargetException, IllegalAccessException {
        if (method.getParameterCount() == 1) {
            Class<?> parameterType = method.getParameterTypes()[0];
            for (Object argument : arguments) {
                if (parameterType.isInstance(argument)) {
                    method.invoke(instance, argument);
                    return;
                }
            }
        }

        if (method.getParameterCount() == 0 && noParameters) {
            method.invoke(instance);
            return;
        }

        throw new TestClassDefinitionException(format(
                "Test class [%s] method [%s] invalid argument type",
                method.getDeclaringClass().getName(), method.getName()));
    }

    /**
     * Method to create a Throttle
     *
     * @param configuration configuration
     * @param name name
     * @return a Throttle
     */
    protected Throttle createThrottle(Configuration configuration, String name) {
        String value = configuration.getProperties().getProperty(name);

        if (value != null && !value.trim().isEmpty()) {
            try {
                String[] tokens = value.split("[\\s,]+");
                if (tokens.length == 1) {
                    return new Throttle(name, Long.parseLong(tokens[0]), Long.parseLong(tokens[0]));
                } else if (tokens.length == 2) {
                    return new Throttle(name, Long.parseLong(tokens[0]), Long.parseLong(tokens[1]));
                } else {
                    return new Throttle(name, 0, 0);
                }
            } catch (Throwable t) {
                return new Throttle(name, 0, 0);
            }
        } else {
            return new Throttle(name, 0, 0);
        }
    }

    /**
     * Method to print a stack trace in AnsiColor.TEXT_RED_BOLD
     *
     * @param throwable throwable
     */
    protected static void printStackTrace(Throwable throwable) {
        StackTracePrinter.printStackTrace(throwable, AnsiColor.TEXT_RED_BOLD, System.err);
    }
}
