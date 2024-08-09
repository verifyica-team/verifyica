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

package org.antublue.verifyica.engine.execution;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.TestDescriptor;

/** Class to implement AbstractRunnableTestDescriptor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public abstract class AbstractRunnableTestDescriptor implements Runnable {

    /** Constructor */
    public AbstractRunnableTestDescriptor() {
        // INTENTIONALLY BLANK
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    /** Method to execute */
    public abstract void execute();

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
        return classTestDescriptor.getChildren().stream()
                .filter(
                        (Predicate<TestDescriptor>)
                                testDescriptor -> testDescriptor instanceof ArgumentTestDescriptor)
                .map(
                        (Function<TestDescriptor, ArgumentTestDescriptor>)
                                testDescriptor -> (ArgumentTestDescriptor) testDescriptor)
                .collect(Collectors.toList());
    }

    /**
     * Method to get a List of child TestMethodDescriptors
     *
     * @param argumentTestDescriptor argumentTestDescriptors
     * @return a List of child TestMethodTestDescriptor
     */
    protected static List<TestMethodTestDescriptor> getTestMethodTestDescriptors(
            ArgumentTestDescriptor argumentTestDescriptor) {
        return argumentTestDescriptor.getChildren().stream()
                .filter(
                        (Predicate<TestDescriptor>)
                                testDescriptor ->
                                        testDescriptor instanceof TestMethodTestDescriptor)
                .map(
                        (Function<TestDescriptor, TestMethodTestDescriptor>)
                                testDescriptor -> (TestMethodTestDescriptor) testDescriptor)
                .collect(Collectors.toList());
    }
}
