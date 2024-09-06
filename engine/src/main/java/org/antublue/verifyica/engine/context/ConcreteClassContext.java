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

package org.antublue.verifyica.engine.context;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;

/** Class to implement ConcreteClassContext */
public class ConcreteClassContext implements ClassContext {

    private final EngineContext engineContext;
    private final ClassTestDescriptor classTestDescriptor;
    private final AtomicReference<Object> testClassInstanceReference;
    private final Store store;

    /**
     * Constructor
     *
     * @param engineContext engineContext
     * @param classTestDescriptor classTestDescriptor
     */
    public ConcreteClassContext(
            EngineContext engineContext,
            ClassTestDescriptor classTestDescriptor,
            AtomicReference<Object> testClassInstanceReference) {
        this.engineContext = engineContext;
        this.classTestDescriptor = classTestDescriptor;
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
        return classTestDescriptor.getTestClass();
    }

    @Override
    public String getTestClassDisplayName() {
        return classTestDescriptor.getDisplayName();
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
        return classTestDescriptor.getTestArgumentParallelism();
    }

    @Override
    public String toString() {
        return "ConcreteClassContext{"
                + "engineContext="
                + engineContext
                + ", classTestDescriptor="
                + classTestDescriptor
                + ", store="
                + store
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcreteClassContext that = (ConcreteClassContext) o;
        return Objects.equals(engineContext, that.engineContext)
                && Objects.equals(classTestDescriptor, that.classTestDescriptor)
                && Objects.equals(store, that.store);
    }

    @Override
    public int hashCode() {
        return Objects.hash(engineContext, classTestDescriptor, store);
    }
}
