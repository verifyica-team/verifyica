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
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Store;

/** Class to implement DefaultArgumentContext */
public class DefaultArgumentContext implements ArgumentContext {

    private final DefaultClassContext defaultClassContext;
    private final Store store;

    private Object testInstance;
    private Argument<?> argument;

    /**
     * Constructor
     *
     * @param defaultClassContext classContextImpl
     */
    public DefaultArgumentContext(DefaultClassContext defaultClassContext) {
        this.defaultClassContext = defaultClassContext;
        this.store = new DefaultStore();
    }

    @Override
    public ClassContext getClassContext() {
        return defaultClassContext;
    }

    @Override
    public Argument<?> getTestArgument() {
        return getTestArgument(Object.class);
    }

    @Override
    public <T> Argument<T> getTestArgument(Class<T> type) {
        notNull(type, "type is null");
        return Argument.of(argument.getName(), type.cast(argument.getPayload()));
    }

    @Override
    public Store getStore() {
        return store;
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
    public Object getTestInstance() {
        return testInstance;
    }

    /**
     * Method to set the argument
     *
     * @param argument argument
     */
    public void setTestArgument(Argument<?> argument) {
        this.argument = argument;
    }

    @Override
    public String toString() {
        return "DefaultArgumentContext{"
                + "defaultClassContext="
                + defaultClassContext
                + ", store="
                + store
                + ", testInstance="
                + testInstance
                + ", argument="
                + argument
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultArgumentContext that = (DefaultArgumentContext) o;
        return Objects.equals(defaultClassContext, that.defaultClassContext)
                && Objects.equals(testInstance, that.testInstance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultClassContext, testInstance);
    }

    /**
     * Checks if an Object is not null, throwing an IllegalArgumentException if the Object is null.
     *
     * @param object object
     * @param message message
     */
    private static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
