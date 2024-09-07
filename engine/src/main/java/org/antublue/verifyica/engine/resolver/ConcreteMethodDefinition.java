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

package org.antublue.verifyica.engine.resolver;

import java.lang.reflect.Method;
import org.antublue.verifyica.api.interceptor.engine.MethodDefinition;

/** Class to implement ConcreteMethodDefinition */
public class ConcreteMethodDefinition implements MethodDefinition {

    private final Method method;
    private String displayName;

    /**
     * Constructor
     *
     * @param method method
     * @param displayName displayName
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
}
