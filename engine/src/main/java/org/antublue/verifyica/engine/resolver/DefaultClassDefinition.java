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

package org.antublue.verifyica.engine.resolver;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.interceptor.engine.ClassDefinition;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;

/** Class to implement DefaultClassDefinition */
public class DefaultClassDefinition implements ClassDefinition {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClassDefinition.class);

    private final Class<?> testClass;
    private final Set<Method> testMethods;
    private final List<Argument<?>> testArguments;

    private String testClassDisplayName;
    private int testArgumentParallelism;

    /**
     * Constructor
     *
     * @param testClass testClass
     * @param testClassDisplayName testClassDisplayName
     * @param testMethods testMethods
     * @param testArguments testArguments
     * @param testArgumentParallelism testArgumentParallelism
     */
    public DefaultClassDefinition(
            Class<?> testClass,
            String testClassDisplayName,
            List<Method> testMethods,
            List<Argument<?>> testArguments,
            int testArgumentParallelism) {
        this.testClass = testClass;
        this.testClassDisplayName = testClassDisplayName;
        this.testMethods = new LinkedHashSet<>(testMethods);
        this.testArguments = testArguments;
        this.testArgumentParallelism = testArgumentParallelism;
    }

    @Override
    public Class<?> getTestClass() {
        return testClass;
    }

    @Override
    public void setTestClassDisplayName(String displayName) {
        if (displayName != null && !displayName.trim().isEmpty()) {
            this.testClassDisplayName = displayName.trim();
        }
    }

    @Override
    public String getTestClassDisplayName() {
        return testClassDisplayName;
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
    public void setTestArgumentParallelism(int testArgumentParallelism) {
        if (testArgumentParallelism < 1) {
            LOGGER.warn(
                    "Test class [%s] test argument parallelism [%d] less than [1], defaulting to"
                            + " [1]",
                    testClass.getName(), testArgumentParallelism);
        }

        this.testArgumentParallelism = Math.max(testArgumentParallelism, 1);
    }

    @Override
    public String toString() {
        return "DefaultClassDefinition{"
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
        DefaultClassDefinition that = (DefaultClassDefinition) o;
        return Objects.equals(testClass, that.testClass)
                && Objects.equals(testMethods, that.testMethods)
                && Objects.equals(testArguments, that.testArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testClass, testMethods, testArguments);
    }
}
