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

/** Class to implement ImmutableClassContext */
public class ImmutableClassContext implements ClassContext {

    private final ClassContext classContext;

    /**
     * Constructor
     *
     * @param classContext classContext
     */
    private ImmutableClassContext(ClassContext classContext) {
        this.classContext = classContext;
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
    public <T> Class<T> getTestClass(Class<T> type) {
        return classContext.getTestClass(type);
    }

    @Override
    public Object getTestInstance() {
        return classContext.getTestInstance();
    }

    @Override
    public <T> T getTestInstance(Class<T> type) {
        return classContext.getTestInstance(type);
    }

    @Override
    public String toString() {
        return "ImmutableClassContext{" + "classContext=" + classContext + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableClassContext that = (ImmutableClassContext) o;
        return Objects.equals(classContext, that.classContext);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(classContext);
    }

    /**
     * Method to wrap a ClassContext
     *
     * @param classContext classContext
     * @return an ImmutableClassContext
     */
    public static ClassContext wrap(ClassContext classContext) {
        return new ImmutableClassContext(classContext);
    }
}
