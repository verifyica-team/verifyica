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
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.extension.ArgumentExtensionContext;

/** Class to implement DefaultArgumentExtensionContext */
public class DefaultArgumentExtensionContext implements ArgumentExtensionContext {

    private final ArgumentContext argumentContext;

    /**
     * Constructor
     *
     * @param argumentContext argumentContext
     */
    public DefaultArgumentExtensionContext(ArgumentContext argumentContext) {
        this.argumentContext = argumentContext;
    }

    @Override
    public ArgumentContext getArgumentContext() {
        return argumentContext;
    }

    @Override
    public String toString() {
        return "DefaultArgumentExtensionContext{" + "argumentContext=" + argumentContext + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultArgumentExtensionContext that = (DefaultArgumentExtensionContext) o;
        return Objects.equals(argumentContext, that.argumentContext);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(argumentContext);
    }
}
