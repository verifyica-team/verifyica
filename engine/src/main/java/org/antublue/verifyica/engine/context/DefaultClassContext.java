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

import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.Store;

/** Class to implement DefaultClassContext */
@SuppressWarnings("unchecked")
public class DefaultClassContext implements ClassContext {

    private final EngineContext engineContext;
    private final Store<Object, Object> store;
    private final ImmutableClassContext immutableClassContext;

    private Class<?> testClass;
    private Object testInstance;

    /**
     * Constructor
     *
     * @param engineContext engineContext
     */
    public DefaultClassContext(EngineContext engineContext) {
        this.engineContext = engineContext;
        this.store = new Store<>();
        this.immutableClassContext = new ImmutableClassContext(this);
    }

    @Override
    public EngineContext getEngineContext() {
        return engineContext;
    }

    @Override
    public Store getObjectStore() {
        return store;
    }

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

    /**
     * Method to get an immutable version
     *
     * @return an immutable version
     */
    public ClassContext asImmutable() {
        return immutableClassContext;
    }
}
