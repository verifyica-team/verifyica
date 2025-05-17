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

package org.verifyica.engine.context;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Configuration;
import org.verifyica.api.EngineContext;

/** Class to implement ConcreteClassContext */
public class ConcreteClassContext extends AbstractContext implements ClassContext {

    private final EngineContext engineContext;
    private final Class<?> testClass;
    private final String testClassDisplayName;
    private final Set<String> testClassTags;
    private final int testArgumentParallelism;
    private final AtomicReference<Object> testClassInstanceReference;

    /**
     * Constructor
     *
     * @param engineContext engineContext
     * @param testClass testClass
     * @param testClassDisplayName testClassDisplayName
     * @param testClassTags testClassTags
     * @param testArgumentParallelism testArgumentParallelism
     * @param testClassInstanceReference testClassInstanceReference
     */
    public ConcreteClassContext(
            EngineContext engineContext,
            Class<?> testClass,
            String testClassDisplayName,
            Set<String> testClassTags,
            int testArgumentParallelism,
            AtomicReference<Object> testClassInstanceReference) {
        this.engineContext = engineContext;
        this.testClass = testClass;
        this.testClassDisplayName = testClassDisplayName;
        this.testClassTags = testClassTags;
        this.testArgumentParallelism = testArgumentParallelism;
        this.testClassInstanceReference = testClassInstanceReference;
    }

    @Override
    public Configuration getConfiguration() {
        return engineContext.getConfiguration();
    }

    @Override
    public EngineContext getEngineContext() {
        return engineContext;
    }

    @Override
    public Class<?> getTestClass() {
        return testClass;
    }

    @Override
    public String getTestClassDisplayName() {
        return testClassDisplayName;
    }

    @Override
    public Set<String> getTestClassTags() {
        return testClassTags;
    }

    @Override
    public int getTestArgumentParallelism() {
        return testArgumentParallelism;
    }

    @Override
    public Object getTestInstance() {
        Object testInstance = testClassInstanceReference.get();
        if (testInstance == null) {
            throw new IllegalStateException("The class instance has not yet been instantiated");
        }
        return testInstance;
    }

    @Override
    public <V> V getTestInstance(Class<V> type) {
        Object testInstance = testClassInstanceReference.get();
        if (testInstance == null) {
            throw new IllegalStateException("The class instance has not yet been instantiated");
        }
        return type.cast(testInstance);
    }

    @Override
    public String toString() {
        return "ConcreteClassContext{" + "engineContext="
                + engineContext + ", testClass="
                + testClass + ", testClassDisplayName='"
                + testClassDisplayName + '\'' + ", testArgumentParallelism="
                + testArgumentParallelism + ", testClassInstanceReference="
                + testClassInstanceReference + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        ConcreteClassContext that = (ConcreteClassContext) object;
        return testArgumentParallelism == that.testArgumentParallelism
                && Objects.equals(engineContext, that.engineContext)
                && Objects.equals(testClass, that.testClass)
                && Objects.equals(testClassDisplayName, that.testClassDisplayName)
                && Objects.equals(testClassInstanceReference.get(), that.testClassInstanceReference.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                engineContext,
                testClass,
                testClassDisplayName,
                testArgumentParallelism,
                testClassInstanceReference);
    }
}
