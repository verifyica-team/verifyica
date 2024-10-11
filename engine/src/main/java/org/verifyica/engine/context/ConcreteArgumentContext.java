/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

import static java.lang.String.format;

import java.util.Objects;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Configuration;
import org.verifyica.engine.common.Precondition;

/** Class to implement ConcreteArgumentContext */
@SuppressWarnings("unchecked")
public class ConcreteArgumentContext extends AbstractContext implements ArgumentContext {

    private final ClassContext classContext;
    private final int argumentIndex;
    private final Argument<?> argument;

    /**
     * Constructor
     *
     * @param classContext classContext
     * @param argumentIndex argumentIndex
     * @param argument argument
     */
    public ConcreteArgumentContext(ClassContext classContext, int argumentIndex, Argument<?> argument) {
        this.classContext = classContext;
        this.argumentIndex = argumentIndex;
        this.argument = argument;
    }

    @Override
    public ClassContext getClassContext() {
        return classContext;
    }

    @Override
    public Configuration getConfiguration() {
        return classContext.getConfiguration();
    }

    @Override
    public int getTestArgumentIndex() {
        return argumentIndex;
    }

    @Override
    public Argument<?> getTestArgument() {
        return argument;
    }

    @Override
    public <V> Argument<V> getTestArgument(Class<V> type) {
        Precondition.notNull(type, "type is null");

        if (argument.getPayload() == null) {
            return (Argument<V>) argument;
        } else if (type.isAssignableFrom(argument.getPayload().getClass())) {
            return (Argument<V>) argument;
        } else {
            throw new ClassCastException(format(
                    "Cannot cast Argument<%s> to Argument<%s>",
                    argument.getPayload().getClass().getName(), type.getName()));
        }
    }

    @Override
    public <V> V getTestArgumentPayload() {
        return argument != null ? (V) argument.getPayload() : null;
    }

    @Override
    public <V> V getTestArgumentPayload(Class<V> type) {
        Precondition.notNull(type, "type is null");

        return argument != null ? argument.getPayload(type) : null;
    }

    @Override
    public String toString() {
        return "ConcreteArgumentContext{" + "classContext="
                + classContext + ", argumentIndex="
                + argumentIndex + ", argument="
                + argument + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ConcreteArgumentContext that = (ConcreteArgumentContext) object;
        return argumentIndex == that.argumentIndex
                && Objects.equals(classContext, that.classContext)
                && Objects.equals(argument, that.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classContext, argumentIndex, argument);
    }
}
