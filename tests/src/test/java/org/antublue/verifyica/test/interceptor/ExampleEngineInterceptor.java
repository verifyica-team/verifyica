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

package org.antublue.verifyica.test.interceptor;

import java.util.UUID;
import org.antublue.verifyica.api.interceptor.EngineDiscoveryInterceptorContext;
import org.antublue.verifyica.api.interceptor.EngineInterceptor;
import org.antublue.verifyica.api.interceptor.EngineInterceptorContext;
import org.antublue.verifyica.api.interceptor.InterceptorResult;

/** Class to implement ExampleEngineInterceptor */
public class ExampleEngineInterceptor implements EngineInterceptor {

    public static final String KEY = ExampleEngineInterceptor.class.getName() + ".key";
    public static final String VALUE = UUID.randomUUID().toString();

    @Override
    public InterceptorResult interceptDiscovery(
            EngineDiscoveryInterceptorContext engineDiscoveryInterceptorContext) {
        System.out.println(getClass().getName() + " interceptDiscovery()");

        // Print all test classes that were discovered
        engineDiscoveryInterceptorContext
                .getTestClasses()
                .forEach(
                        testClass ->
                                System.out.println("test class [" + testClass.getName() + "]"));

        return InterceptorResult.PROCEED;
    }

    @Override
    public InterceptorResult interceptInitialize(
            EngineInterceptorContext engineInterceptorContext) {
        System.out.println(getClass().getName() + " interceptInitialize()");

        // Add a global string to the EngineContext Store for EngineInterceptorTest
        engineInterceptorContext.getEngineContext().getStore().put(KEY, VALUE);

        return InterceptorResult.PROCEED;
    }

    @Override
    public void interceptDestroy(EngineInterceptorContext engineInterceptorContext) {
        System.out.println(getClass().getName() + " interceptDestroy()");
    }
}
