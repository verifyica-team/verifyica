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
import org.antublue.verifyica.engine.support.ArgumentSupport;

/** Class to implement ImmutableArgumentContext */
public class ImmutableArgumentContext implements ArgumentContext {

    private final ArgumentContext argumentContext;
    private final ClassContext classContext;

    /**
     * Constructor
     *
     * @param argumentContext argumentContext
     */
    private ImmutableArgumentContext(ArgumentContext argumentContext) {
        this.argumentContext = argumentContext;
        this.classContext = ImmutableClassContext.wrap(argumentContext.getClassContext());
    }

    @Override
    public ClassContext getClassContext() {
        return classContext;
    }

    @Override
    public Argument<?> getTestArgument() {
        return getTestArgument(Object.class);
    }

    @Override
    public <T> Argument<T> getTestArgument(Class<T> type) {
        ArgumentSupport.notNull(type, "type is null");

        return argumentContext.getTestArgument(type);
    }

    @Override
    public Store getStore() {
        return argumentContext.getStore();
    }

    @Override
    public String toString() {
        return "ImmutableArgumentContext{"
                + "argumentContext="
                + argumentContext
                + ", classContext="
                + classContext
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableArgumentContext that = (ImmutableArgumentContext) o;
        return Objects.equals(argumentContext, that.argumentContext)
                && Objects.equals(classContext, that.classContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(argumentContext, classContext);
    }

    /**
     * Method to wrap a ClassContext
     *
     * @param argumentContext argumentContext
     * @return an ImmutableClassContext
     */
    public static ArgumentContext wrap(ArgumentContext argumentContext) {
        return new ImmutableArgumentContext(argumentContext);
    }
}
