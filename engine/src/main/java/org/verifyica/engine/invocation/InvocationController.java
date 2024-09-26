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
import java.util.concurrent.atomic.AtomicReference;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.interceptor.EngineInterceptorContext;
import org.verifyica.engine.interceptor.ClassInterceptorManager;
import org.verifyica.engine.interceptor.EngineInterceptorManager;

/** Class to implement InvocationController */
public class InvocationController {

    private final EngineInterceptorManager engineInterceptorManager;
    private final ClassInterceptorManager classInterceptorManager;

    /**
     * Constructor
     *
     * @param engineInterceptorManager engineInterceptorManager
     * @param classInterceptorManager classInterceptorManager
     */
    public InvocationController(
            EngineInterceptorManager engineInterceptorManager, ClassInterceptorManager classInterceptorManager) {
        this.engineInterceptorManager = engineInterceptorManager;
        this.classInterceptorManager = classInterceptorManager;
    }

    /**
     * Method to invoke onInitialize
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @throws Throwable Throwable
     */
    public void invokeOnInitialize(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        engineInterceptorManager.onInitialize(engineInterceptorContext);
    }

    /**
     * Method to invoke instantiate
     *
     * @param testClass testClass
     * @param testInstanceReference testInstanceReference
     * @return an InvocationResult
     */
    public InvocationResult invokeInstantiate(Class<?> testClass, AtomicReference<Object> testInstanceReference) {
        try {
            classInterceptorManager.instantiate(testClass, testInstanceReference);
            return InvocationResult.success();
        } catch (Throwable t) {
            return InvocationResult.exception(t);
        }
    }

    /**
     * Method to invoke prepare methods
     *
     * @param prepareMethods prepareMethods
     * @param classContext classContext
     * @return an InvocationResult
     */
    public InvocationResult invokePrepareMethods(List<Method> prepareMethods, ClassContext classContext) {
        try {
            classInterceptorManager.prepare(prepareMethods, classContext);
            return InvocationResult.success();
        } catch (Throwable t) {
            return InvocationResult.exception(t);
        }
    }

    /**
     * Method to invoke beforeAll methods
     *
     * @param beforeAllMethods beforeAllMethods
     * @param argumentContext argumentContext
     * @return an InvocationResult
     */
    public InvocationResult invokeBeforeAllMethods(List<Method> beforeAllMethods, ArgumentContext argumentContext) {
        try {
            classInterceptorManager.beforeAll(beforeAllMethods, argumentContext);
            return InvocationResult.success();
        } catch (Throwable t) {
            return InvocationResult.exception(t);
        }
    }

    /**
     * Method to invoke beforeEach methods
     *
     * @param beforeEachMethods beforeEachMethods
     * @param argumentContext argumentContext
     * @return an InvocationResult
     */
    public InvocationResult invokeBeforeEachMethods(List<Method> beforeEachMethods, ArgumentContext argumentContext) {
        try {
            classInterceptorManager.beforeEach(beforeEachMethods, argumentContext);
            return InvocationResult.success();
        } catch (Throwable t) {
            return InvocationResult.exception(t);
        }
    }

    /**
     * Method to invoke a test method
     *
     * @param testMethod testMethod
     * @param argumentContext argumentContext
     * @return an InvocationResult
     */
    public InvocationResult invokeTestMethod(Method testMethod, ArgumentContext argumentContext) {
        try {
            classInterceptorManager.test(testMethod, argumentContext);
            return InvocationResult.success();
        } catch (Throwable t) {
            return InvocationResult.exception(t);
        }
    }

    /**
     * Method to invoke afterEach methods
     *
     * @param afterEachMethods afterEachMethods
     * @param argumentContext argumentContext
     * @return an InvocationResult
     */
    public InvocationResult invokeAfterEachMethods(List<Method> afterEachMethods, ArgumentContext argumentContext) {
        try {
            classInterceptorManager.afterEach(afterEachMethods, argumentContext);
            return InvocationResult.success();
        } catch (Throwable t) {
            return InvocationResult.exception(t);
        }
    }

    /**
     * Method to invoke afterAll methods
     *
     * @param afterAllMethods afterAllMethods
     * @param argumentContext argumentContext
     * @return an InvocationResult
     */
    public InvocationResult invokeAfterAllMethods(List<Method> afterAllMethods, ArgumentContext argumentContext) {
        try {
            classInterceptorManager.afterAll(afterAllMethods, argumentContext);
            return InvocationResult.success();
        } catch (Throwable t) {
            return InvocationResult.exception(t);
        }
    }

    /**
     * Method to invoke conclude methods
     *
     * @param concludeMethods concludeMethods
     * @param classContext classContext
     * @return an InvocationResult
     */
    public InvocationResult invokeConcludeMethods(List<Method> concludeMethods, ClassContext classContext) {
        try {
            classInterceptorManager.conclude(concludeMethods, classContext);
            return InvocationResult.success();
        } catch (Throwable t) {
            return InvocationResult.exception(t);
        }
    }

    /**
     * Method to invoke destroy
     *
     * @param classContext class
     * @return an InvocationResult
     */
    public InvocationResult invokeOnDestroy(ClassContext classContext) {
        try {
            classInterceptorManager.onDestroy(classContext);
            return InvocationResult.success();
        } catch (Throwable t) {
            return InvocationResult.exception(t);
        }
    }
}
