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

/** Class to implement ConcreteClassInstanceContext */
public class ConcreteClassInstanceContext implements ClassContext {

    private final ClassContext classContext;
    private final Object testInstance;

    /**
     * Constructor
     *
     * @param classContext classContext
     * @param testInstance testInstance
     */
    public ConcreteClassInstanceContext(ClassContext classContext, Object testInstance) {
        this.classContext = classContext;
        this.testInstance = testInstance;
    }

    @Override
    public EngineContext getEngineContext() {
        return classContext.getEngineContext();
    }

    @Override
    public Store getStore() {
        return classContext.getStore();
    }

    @Override
    public Class<?> getTestClass() {
        return classContext.getTestClass();
    }

    @Override
    public String getTestClassDisplayName() {
        return classContext.getTestClassDisplayName();
    }

    @Override
    public Object getTestInstance() {
        return testInstance;
    }

    @Override
    public <V> V getTestInstance(Class<V> returnType) {
        return returnType.cast(getTestInstance());
    }

    @Override
    public int getTestArgumentParallelism() {
        return classContext.getTestArgumentParallelism();
    }

    @Override
    public String toString() {
        return "ConcreteClassInstanceContext{"
                + "classContext="
                + classContext
                + ", testInstance="
                + testInstance
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcreteClassInstanceContext that = (ConcreteClassInstanceContext) o;
        return Objects.equals(classContext, that.classContext)
                && Objects.equals(testInstance, that.testInstance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classContext, testInstance);
    }
}
