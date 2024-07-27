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
import org.antublue.verifyica.api.extension.ClassExtensionContext;

/** Class to implement DefaultClassExtensionContext */
public class DefaultClassExtensionContext implements ClassExtensionContext {

    private final ClassContext classContext;

    /**
     * Constructor
     *
     * @param classContext classContext
     */
    public DefaultClassExtensionContext(ClassContext classContext) {
        this.classContext = classContext;
    }

    @Override
    public ClassContext getClassContext() {
        return classContext;
    }

    @Override
    public String toString() {
        return "DefaultClassExtensionContext{" + "classContext=" + classContext + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultClassExtensionContext that = (DefaultClassExtensionContext) o;
        return Objects.equals(classContext, that.classContext);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(classContext);
    }
}
