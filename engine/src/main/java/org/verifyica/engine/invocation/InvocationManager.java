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

package org.verifyica.engine.invocation;

import java.lang.reflect.Method;
import java.util.List;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.interceptor.EngineInterceptorContext;
import org.verifyica.engine.interceptor.ClassInterceptorManager;
import org.verifyica.engine.interceptor.EngineInterceptorManager;

/** Class to implement InvocationManager */
public class InvocationManager {

    private final EngineInterceptorManager engineInterceptorManager;
    private final ClassInterceptorManager classInterceptorManager;

    /**
     * Constructor
     *
     * @param engineInterceptorManager engineInterceptorManager
     * @param classInterceptorManager classInterceptorManager
     */
    public InvocationManager(
            EngineInterceptorManager engineInterceptorManager,
            ClassInterceptorManager classInterceptorManager) {
        this.engineInterceptorManager = engineInterceptorManager;
        this.classInterceptorManager = classInterceptorManager;
    }

    public void invokeOnInitialize(EngineInterceptorContext engineInterceptorContext)
            throws Throwable {
        engineInterceptorManager.onInitialize(engineInterceptorContext);
    }

    /**
     * Method to invoke beforeEach methods
     *
     * @param beforeEachMethods beforeEachMethods
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    public void invokeBeforeEach(List<Method> beforeEachMethods, ArgumentContext argumentContext)
            throws Throwable {
        classInterceptorManager.beforeAll(beforeEachMethods, argumentContext);
    }

    /**
     * Method to invoke a test method
     *
     * @param testMethod testMethod
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    public void invokeTest(Method testMethod, ArgumentContext argumentContext) throws Throwable {
        classInterceptorManager.test(testMethod, argumentContext);
    }

    /**
     * Method to invoke afterEach methods
     *
     * @param afterEachMethods afterEachMethods
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    public void invokeAfterEach(List<Method> afterEachMethods, ArgumentContext argumentContext)
            throws Throwable {
        classInterceptorManager.afterEach(afterEachMethods, argumentContext);
    }
}
