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

package org.antublue.verifyica.engine.descriptor.execution;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.TestDescriptor;

/** Class to implement AbstractTestDescriptorExecution */
@SuppressWarnings("PMD.EmptyCatchBlock")
public abstract class AbstractTestDescriptorExecutionContext {

    /** ArgumentTestDescriptor predicate */
    private static final Predicate<TestDescriptor> ARGUMENT_TEST_DESCRIPTOR =
            testDescriptor -> testDescriptor instanceof ArgumentTestDescriptor;

    /** ArgumentTestDescriptor mapping function */
    private static final Function<TestDescriptor, ArgumentTestDescriptor>
            ARGUMENT_TEST_DESCRIPTOR_MAPPER =
                    testDescriptor -> (ArgumentTestDescriptor) testDescriptor;

    /** TestMethodTestDescriptor predicate */
    private static final Predicate<TestDescriptor> TEST_METHOD_DESCRIPTOR =
            testDescriptor -> testDescriptor instanceof TestMethodTestDescriptor;

    /** TestMethodTestDescriptor mapping function */
    private static final Function<TestDescriptor, TestMethodTestDescriptor>
            TEST_METHOD_DESCRIPTOR_MAPPER =
                    testDescriptor -> (TestMethodTestDescriptor) testDescriptor;

    /** Constructor */
    public AbstractTestDescriptorExecutionContext() {
        // INTENTIONALLY BLANK
    }

    /** Method to test */
    public abstract void test();

    /** Method to skip */
    public abstract void skip();

    /**
     * Method to get a List of ArgumentTestDescriptors
     *
     * @param classTestDescriptor classTestDescriptor
     * @return a List of ArgumentTestDescriptors
     */
    protected static List<ArgumentTestDescriptor> getArgumentTestDescriptors(
            ClassTestDescriptor classTestDescriptor) {
        Precondition.notNull(classTestDescriptor, "classTestDescriptor is null");

        return classTestDescriptor.getChildren().stream()
                .filter(ARGUMENT_TEST_DESCRIPTOR)
                .map(ARGUMENT_TEST_DESCRIPTOR_MAPPER)
                .collect(Collectors.toList());
    }

    /**
     * Method to get a List of child TestMethodDescriptors
     *
     * @param argumentTestDescriptor argumentTestDescriptors
     * @return a List of TestMethodTestDescriptors
     */
    protected static List<TestMethodTestDescriptor> getTestMethodTestDescriptors(
            ArgumentTestDescriptor argumentTestDescriptor) {
        Precondition.notNull(argumentTestDescriptor, "argumentTestDescriptor is null");

        return argumentTestDescriptor.getChildren().stream()
                .filter(TEST_METHOD_DESCRIPTOR)
                .map(TEST_METHOD_DESCRIPTOR_MAPPER)
                .collect(Collectors.toList());
    }
}
