/*
 * Copyright (C) Verifyica project authors and contributors
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
 * Class to implement ConcreteClassDefinition
 */
public class ConcreteClassDefinition implements ClassDefinition {

    private final Class<?> testClass;
    private final Set<MethodDefinition> testMethodDefinitions;
    private final List<Argument<?>> arguments;
    private final Set<String> tags;
    private final int argumentParallelism;

    private String displayName;

    /**
     * Constructor
     *
     * @param testClass testClass
     * @param displayName displayName
     * @param tags tags
     * @param testMethodDefinitions testMethodDefinitions
     * @param arguments arguments
     * @param argumentParallelism testArgumentParallelism
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
        return "ConcreteClassDefinition{"
                + "testClass="
                + testClass
                + ", displayName="
                + displayName
                + ", argumentParallelism="
                + argumentParallelism
                + ", testMethodDefinitions="
                + testMethodDefinitions
                + ", testArguments="
                + arguments
                + '}';
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
        return Objects.hash(testClass, testMethodDefinitions, arguments);
    }
}
