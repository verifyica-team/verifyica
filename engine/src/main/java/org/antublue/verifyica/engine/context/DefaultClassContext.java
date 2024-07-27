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

/** Class to implement DefaultClassContext */
@SuppressWarnings("unchecked")
public class DefaultClassContext implements ClassContext {

    private final EngineContext engineContext;
    private final Store store;

    private Class<?> testClass;
    private Object testInstance;

    /**
     * Constructor
     *
     * @param engineContext engineContext
     */
    public DefaultClassContext(EngineContext engineContext) {
        this.engineContext = engineContext;
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

    /**
     * Method to test the test class
     *
     * @param testClass testClass
     */
    public void setTestClass(Class<?> testClass) {
        this.testClass = testClass;
    }

    @Override
    public Class<?> getTestClass() {
        return testClass;
    }

    @Override
    public <T> Class<T> getTestClass(Class<T> type) {
        return (Class<T>) getTestClass().asSubclass(type);
    }

    /**
     * Method to set the test instance
     *
     * @param testInstance testInstance
     */
    public void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }

    /**
     * Method to get the test instance
     *
     * @return the test instance
     */
    @Override
    public Object getTestInstance() {
        return testInstance;
    }

    @Override
    public <T> T getTestInstance(Class<T> type) {
        return type.cast(getTestInstance());
    }

    @Override
    public String toString() {
        return "DefaultClassContext{"
                + "engineContext="
                + engineContext
                + ", store="
                + store
                + ", testClass="
                + testClass
                + ", testInstance="
                + testInstance
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultClassContext that = (DefaultClassContext) o;
        return Objects.equals(engineContext, that.engineContext)
                && Objects.equals(testClass, that.testClass)
                && Objects.equals(testInstance, that.testInstance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(engineContext, testClass, testInstance);
    }
}
