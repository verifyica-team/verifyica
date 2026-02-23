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

package org.verifyica.engine.resolver;

import java.lang.reflect.Method;
import java.util.Objects;
import org.verifyica.engine.api.MethodDefinition;

/**
 * ConcreteMethodDefinition provides a concrete implementation of MethodDefinition
 */
public class ConcreteMethodDefinition implements MethodDefinition {

    /** The method. */
    private final Method method;

    /** The display name for this method definition. */
    private String displayName;

    /**
     * Constructs a new ConcreteMethodDefinition with the specified method and display name.
     *
     * @param method the method
     * @param displayName the display name for this method definition
     */
    public ConcreteMethodDefinition(Method method, String displayName) {
        this.method = method;
        this.displayName = displayName;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void setDisplayName(String displayName) {
        if (displayName != null && !displayName.trim().isEmpty()) {
            this.displayName = displayName.trim();
        }
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(128);
        stringBuilder
                .append("ConcreteMethodDefinition{method=")
                .append(method)
                .append(", displayName='")
                .append(displayName)
                .append("'}");
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcreteMethodDefinition that = (ConcreteMethodDefinition) o;
        return Objects.equals(method, that.method) && Objects.equals(displayName, that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, displayName);
    }
}
