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
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;

/** Class to implement DefaultClassContext */
@SuppressWarnings("unchecked")
public class DefaultClassContext implements ClassContext {

    private final EngineContext engineContext;
    private final ClassTestDescriptor classTestDescriptor;
    private final Store store;

    /**
     * Constructor
     *
     * @param engineContext engineContext
     */
    public DefaultClassContext(
            EngineContext engineContext, ClassTestDescriptor classTestDescriptor) {
        this.engineContext = engineContext;
        this.classTestDescriptor = classTestDescriptor;
        this.store = new DefaultStore();
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
    public <T> Class<T> getTestClass(Class<T> type) {
        return (Class<T>) getTestClass().asSubclass(type);
    }

    @Override
    public String getTestClassDisplayName() {
        return classTestDescriptor.getDisplayName();
    }

    @Override
    public Object getTestInstance() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public <T> T getTestInstance(Class<T> type) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public int getTestArgumentParallelism() {
        return classTestDescriptor.getTestArgumentParallelism();
    }

    @Override
    public String toString() {
        return "DefaultClassContext{"
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
        DefaultClassContext that = (DefaultClassContext) o;
        return Objects.equals(engineContext, that.engineContext)
                && Objects.equals(classTestDescriptor, that.classTestDescriptor)
                && Objects.equals(store, that.store);
    }

    @Override
    public int hashCode() {
        return Objects.hash(engineContext, classTestDescriptor, store);
    }
}
