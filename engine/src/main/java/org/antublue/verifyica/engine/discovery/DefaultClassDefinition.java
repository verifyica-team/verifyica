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

package org.antublue.verifyica.engine.discovery;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.extension.TestClassDefinition;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;

/** Class to implement DefaultTestClassDefinition */
public class DefaultTestClassDefinition implements TestClassDefinition {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTestClassDefinition.class);

    private final Class<?> testClass;
    private final Set<Method> testMethods;
    private final List<Argument<?>> testArguments;
    private int testArgumentParallelism;

    /**
     * Constructor
     *
     * @param testClass testClass
     * @param testMethods testMethods
     * @param testArguments testArguments
     * @param testArgumentParallelism testArgumentParallelism
     */
    public DefaultTestClassDefinition(
            Class<?> testClass,
            List<Method> testMethods,
            List<Argument<?>> testArguments,
            int testArgumentParallelism) {
        this.testClass = testClass;
        this.testMethods = new LinkedHashSet<>(testMethods);
        this.testArguments = testArguments;
        this.testArgumentParallelism = testArgumentParallelism;
    }

    @Override
    public Class<?> getTestClass() {
        return testClass;
    }

    @Override
    public Set<Method> getTestMethods() {
        return testMethods;
    }

    @Override
    public List<Argument<?>> getTestArguments() {
        return testArguments;
    }

    @Override
    public int getTestArgumentParallelism() {
        return testArgumentParallelism;
    }

    @Override
    public void setTestArgumentParallelism(int parallelism) {
        if (parallelism < 1) {
            LOGGER.warn(
                    "Invalid test class [%s] test argument parallelism [%d], defaulting to [1]",
                    testClass.getName(), parallelism);
        }

        this.testArgumentParallelism = Math.max(this.testArgumentParallelism, 1);
    }

    @Override
    public String toString() {
        return "DefaultTestClassDefinition{"
                + "testClass="
                + testClass
                + ", testMethods="
                + testMethods
                + ", testArguments="
                + testArguments
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultTestClassDefinition that = (DefaultTestClassDefinition) o;
        return Objects.equals(testClass, that.testClass)
                && Objects.equals(testMethods, that.testMethods)
                && Objects.equals(testArguments, that.testArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testClass, testMethods, testArguments);
    }
}
