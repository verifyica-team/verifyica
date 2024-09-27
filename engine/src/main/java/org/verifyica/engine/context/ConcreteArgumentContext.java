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

import java.util.Objects;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Store;
import org.verifyica.engine.common.Precondition;
import org.verifyica.engine.descriptor.ArgumentTestDescriptor;

/** Class to implement ConcreteArgumentContext */
@SuppressWarnings("unchecked")
public class ConcreteArgumentContext implements ArgumentContext {

    private final ClassContext classContext;
    private final ArgumentTestDescriptor argumentTestDescriptor;
    private final Store store;

    /**
     * Constructor
     *
     * @param classContext classContext
     * @param argumentTestDescriptor argumentTestDescriptor
     */
    public ConcreteArgumentContext(ClassContext classContext, ArgumentTestDescriptor argumentTestDescriptor) {
        this.classContext = classContext;
        this.argumentTestDescriptor = argumentTestDescriptor;
        this.store = new ConcreteStore();
    }

    @Override
    public ClassContext getClassContext() {
        return classContext;
    }

    @Override
    public int getTestArgumentIndex() {
        return argumentTestDescriptor.getTestArgumentIndex();
    }

    @Override
    public Argument<?> getTestArgument() {
        return getTestArgument(Object.class);
    }

    @Override
    public <V> Argument<V> getTestArgument(Class<V> type) {
        Precondition.notNull(type, "type is null");

        return (Argument<V>) argumentTestDescriptor.getTestArgument();
    }

    @Override
    public Store getStore() {
        return store;
    }

    @Override
    public String toString() {
        return "ConcreteArgumentContext{"
                + "classContext="
                + classContext
                + ", argumentTestDescriptor="
                + argumentTestDescriptor
                + ", store="
                + store
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcreteArgumentContext that = (ConcreteArgumentContext) o;
        return Objects.equals(classContext, that.classContext)
                && Objects.equals(argumentTestDescriptor, that.argumentTestDescriptor)
                && Objects.equals(store, that.store);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classContext, argumentTestDescriptor, store);
    }
}
