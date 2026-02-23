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

package org.verifyica.engine.resolver;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.verifyica.api.Argument;
import org.verifyica.engine.api.ClassDefinition;
import org.verifyica.engine.api.MethodDefinition;

/**
 * ConcreteClassDefinition provides a concrete implementation of ClassDefinition
 */
public class ConcreteClassDefinition implements ClassDefinition {

    /**
     * The test class represented by this ClassDefinition.
     */
    private final Class<?> testClass;

    /**
     * The set of test method definitions associated with this class definition.
     */
    private final Set<MethodDefinition> testMethodDefinitions;

    /**
     * The list of test arguments associated with this class definition.
     */
    private final List<Argument<?>> arguments;

    /**
     * The set of tags associated with this class definition, which can be used
     * for filtering and categorization purposes.
     */
    private final Set<String> tags;

    /**
     * The parallelism factor for test arguments, which indicates how many
     * instances of the test class should be created to run tests with different
     * arguments in parallel.
     */
    private final int argumentParallelism;

    /**
     * The display name for this ClassDefinition, which can be used for reporting and logging purposes.
     */
    private String displayName;

    /**
     * Constructs a new ConcreteClassDefinition with the specified parameters.
     *
     * @param testClass the test class
     * @param displayName the display name for this class definition
     * @param tags the set of tags associated with this class definition
     * @param testMethodDefinitions the list of test method definitions
     * @param arguments the list of test arguments
     * @param argumentParallelism the parallelism factor for test arguments
     */
    public ConcreteClassDefinition(
            Class<?> testClass,
            String displayName,
            Set<String> tags,
            List<MethodDefinition> testMethodDefinitions,
            List<Argument<?>> arguments,
            int argumentParallelism) {
        this.testClass = testClass;
        this.displayName = displayName;
        this.tags = tags;
        this.testMethodDefinitions = new LinkedHashSet<>(testMethodDefinitions);
        this.arguments = arguments;
        this.argumentParallelism = argumentParallelism;
    }

    @Override
    public Class<?> getTestClass() {
        return testClass;
    }

    @Override
    public void setDisplayName(String displayName) {
        if (displayName != null && !displayName.trim().isEmpty()) {
            this.displayName = displayName.trim();
        }
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }

    @Override
    public Set<MethodDefinition> getTestMethodDefinitions() {
        return testMethodDefinitions;
    }

    @Override
    public List<Argument<?>> getArguments() {
        return arguments;
    }

    @Override
    public int getArgumentParallelism() {
        return argumentParallelism;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(256);
        stringBuilder
                .append("ConcreteClassDefinition{testClass=")
                .append(testClass)
                .append(", displayName=")
                .append(displayName)
                .append(", argumentParallelism=")
                .append(argumentParallelism)
                .append(", testMethodDefinitions=")
                .append(testMethodDefinitions)
                .append(", testArguments=")
                .append(arguments)
                .append('}');
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcreteClassDefinition that = (ConcreteClassDefinition) o;
        return Objects.equals(testClass, that.testClass)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(testMethodDefinitions, that.testMethodDefinitions)
                && Objects.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testClass, displayName, testMethodDefinitions, arguments);
    }
}
