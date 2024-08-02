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

package org.antublue.verifyica.test.interceptor.engine;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.antublue.verifyica.api.interceptor.engine.AutoDiscoverableEngineInterceptor;
import org.antublue.verifyica.api.interceptor.engine.ClassDefinition;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptor;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;

/** Class to implement ExampleEngineInterceptor1 */
public class ExampleEngineInterceptor1 implements AutoDiscoverableEngineInterceptor {

    public static final String KEY = ExampleEngineInterceptor1.class.getName() + ".key";
    public static final String VALUE = UUID.randomUUID().toString();

    @Override
    public void onInitialize(EngineInterceptorContext engineInterceptorContext) {
        System.out.println(format("%s onInitialize()", getClass().getName()));

        // Add a global string to the EngineContext Store for EngineInterceptorTest
        engineInterceptorContext.getEngineContext().getStore().put(KEY, VALUE);
    }

    @Override
    public void onTestDiscovery(
            EngineInterceptorContext engineInterceptorContext,
            List<ClassDefinition> classDefinitions) {
        System.out.println(format("%s onTestDiscovery()", getClass().getName()));

        for (ClassDefinition classDefinition : classDefinitions) {
            if (classDefinition
                    .getTestClass()
                    .getName()
                    .equals(
                            "org.antublue.verifyica.test.interceptor.engine.EngineInterceptorTest1")) {

                // Reverse test methods
                reverseMethods(classDefinition.getTestMethods());

                // Filter test method "test4"
                classDefinition
                        .getTestMethods()
                        .removeIf(method -> method.getName().equals("test4"));
            }
        }
    }

    @Override
    public void preExecute(EngineInterceptorContext engineInterceptorContext) {
        System.out.println(format("%s preExecute()", getClass().getName()));
    }

    @Override
    public void postExecute(EngineInterceptorContext engineInterceptorContext) {
        System.out.println(format("%s postExecute()", getClass().getName()));
    }

    /**
     * Method to reverse a Set of Methods
     *
     * @param methods methods
     */
    private static void reverseMethods(Set<Method> methods) {
        List<Method> list = new ArrayList<>(methods);
        Collections.reverse(list);
        methods.clear();
        methods.addAll(list);
    }
}
