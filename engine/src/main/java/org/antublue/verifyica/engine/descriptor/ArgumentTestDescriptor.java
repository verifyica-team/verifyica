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

package org.antublue.verifyica.engine.descriptor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.engine.common.Precondition;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement ArgumentTestDescriptor */
public class ArgumentTestDescriptor extends AbstractTestDescriptor {

    private final Class<?> testClass;
    private final int testArgumentIndex;
    private final Argument<?> testArgument;
    private final List<Method> beforeAllMethods;
    private final List<Method> afterAllMethods;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param testArgumentIndex testArgumentIndex
     * @param testArgument testArgument
     * @param beforeAllMethods beforeAllMethods
     * @param afterAllMethods afterAllMethods
     */
    public ArgumentTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            int testArgumentIndex,
            Argument<?> testArgument,
            List<Method> beforeAllMethods,
            List<Method> afterAllMethods) {
        super(uniqueId, displayName);

        Precondition.notNull(testClass, "testClass is null");
        Precondition.isTrue(testArgumentIndex >= 0, "testArgumentIndex is less than 0");
        Precondition.notNull(testArgument, "testArgument is null");
        Precondition.notNull(beforeAllMethods, "beforeAllMethods is null");
        Precondition.notNull(afterAllMethods, "afterAllMethods is null");

        this.testClass = testClass;
        this.testArgumentIndex = testArgumentIndex;
        this.testArgument = testArgument;
        this.beforeAllMethods = beforeAllMethods;
        this.afterAllMethods = afterAllMethods;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    /**
     * Method to get the test Argument index
     *
     * @return the test Argument index
     */
    public int getTestArgumentIndex() {
        return testArgumentIndex;
    }

    /**
     * Method to get the test Argument
     *
     * @return the test Argument
     */
    public Argument<?> getTestArgument() {
        return testArgument;
    }

    /**
     * Method to get a List of beforeAll Methods
     *
     * @return a List of beforeAll Methods
     */
    public List<Method> getBeforeAllMethods() {
        return beforeAllMethods;
    }

    /**
     * Method to get a List of afterAll Methods
     *
     * @return a List of afterAll Methods
     */
    public List<Method> getAfterAllMethods() {
        return afterAllMethods;
    }
}
