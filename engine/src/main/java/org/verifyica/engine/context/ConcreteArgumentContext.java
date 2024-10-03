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
        super(classContext.getConfiguration());

        this.classContext = classContext;
        this.argumentIndex = argumentIndex;
        this.argument = argument;
    }

    @Override
    public ClassContext getClassContext() {
        return classContext;
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

        return (Argument<V>) argument;
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
