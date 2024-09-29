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

package org.verifyica.engine.context;

import java.util.concurrent.atomic.AtomicReference;
import org.verifyica.api.ClassContext;
import org.verifyica.api.EngineContext;
import org.verifyica.api.Store;

/** Class to implement ConcreteClassContext */
public class ConcreteClassContext implements ClassContext {

    private final EngineContext engineContext;
    private final Class<?> testClass;
    private final String testClassDisplayName;
    private final int testArgumentParallelism;
    private final AtomicReference<Object> testClassInstanceReference;
    private final Store store;

    /**
     * Constructor
     *
     * @param engineContext engineContext
     * @param testClass testClass
     * @param testClassDisplayName testClassDisplayName
     * @param testArgumentParallelism testArgumentParallelism
     * @param testClassInstanceReference testClassInstanceReference
     */
    public ConcreteClassContext(
            EngineContext engineContext,
            Class<?> testClass,
            String testClassDisplayName,
            int testArgumentParallelism,
            AtomicReference<Object> testClassInstanceReference) {
        this.engineContext = engineContext;
        this.testClass = testClass;
        this.testClassDisplayName = testClassDisplayName;
        this.testArgumentParallelism = testArgumentParallelism;
        this.testClassInstanceReference = testClassInstanceReference;
        this.store = new ConcreteStore();
    }

    @Override
    public EngineContext getEngineContext() {
        return engineContext;
    }

    @Override
    public Store getStore() {
        return store;
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
    public Object getTestInstance() {
        Object testInstance = testClassInstanceReference.get();
        if (testInstance == null) {
            throw new IllegalStateException("The class instance has not yet been instantiated");
        }
        return testInstance;
    }

    @Override
    public <V> V getTestInstance(Class<V> returnType) {
        Object testInstance = testClassInstanceReference.get();
        if (testInstance == null) {
            throw new IllegalStateException("The class instance has not yet been instantiated");
        }
        return returnType.cast(testInstance);
    }

    @Override
    public int getTestArgumentParallelism() {
        return testArgumentParallelism;
    }

    @Override
    public String toString() {
        return "ConcreteClassContext{" + "engineContext="
                + engineContext + ", testClass="
                + testClass + ", testClassDisplayName='"
                + testClassDisplayName + '\'' + ", testArgumentParallelism="
                + testArgumentParallelism + ", testClassInstanceReference="
                + testClassInstanceReference + ", store="
                + store + '}';
    }
}
