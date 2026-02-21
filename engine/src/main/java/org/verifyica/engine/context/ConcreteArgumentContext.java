/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

/**
 * Concrete implementation of {@link ArgumentContext}.
 */
@SuppressWarnings("unchecked")
public class ConcreteArgumentContext extends AbstractContext implements ArgumentContext {

    private final ClassContext classContext;
    private final int argumentIndex;
    private final Argument<?> argument;

    /**
     * Creates a new ConcreteArgumentContext.
     *
     * @param classContext the class context
     * @param argumentIndex the argument index
     * @param argument the argument
     */
    public ConcreteArgumentContext(
            final ClassContext classContext, final int argumentIndex, final Argument<?> argument) {
        Precondition.notNull(classContext, "classContext is null");
        Precondition.notNull(argument, "argument is null");

        this.argumentIndex = argumentIndex;
        this.classContext = classContext;
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
    public int getArgumentIndex() {
        return argumentIndex;
    }

    @Override
    public Argument<?> getArgument() {
        return argument;
    }

    @Override
    public <V> Argument<V> getArgumentAs(final Class<V> type) {
        Precondition.notNull(type, "type is null");

        if (argument.getPayload() == null) {
            return (Argument<V>) argument;
        } else if (type.isAssignableFrom(argument.getPayload().getClass())) {
            return (Argument<V>) argument;
        } else {
            throw new IllegalArgumentException(format(
                    "Cannot cast Argument<%s> to Argument<%s>",
                    argument.getPayload().getClass().getName(), type.getName()));
        }
    }

    @Override
    public String toString() {
        return "ConcreteArgumentContext{" + "classContext="
                + classContext + ", argumentIndex="
                + argumentIndex + ", argument="
                + argument + '}';
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        final ConcreteArgumentContext that = (ConcreteArgumentContext) object;
        return argumentIndex == that.argumentIndex
                && Objects.equals(classContext, that.classContext)
                && Objects.equals(argument, that.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classContext, argumentIndex, argument);
    }
}
