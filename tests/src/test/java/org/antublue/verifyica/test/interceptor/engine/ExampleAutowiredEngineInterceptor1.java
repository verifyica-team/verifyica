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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.engine.ClassDefinition;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptor;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;
import org.antublue.verifyica.api.interceptor.engine.MethodDefinition;

@Verifyica.Autowired
public class ExampleAutowiredEngineInterceptor1 implements EngineInterceptor {

    public static final String KEY = ExampleAutowiredEngineInterceptor1.class.getName() + ".key";
    public static final String VALUE = UUID.randomUUID().toString();

    @Override
    public void onInitialize(EngineInterceptorContext engineInterceptorContext) {
        System.out.printf("%s onInitialize()%n", getClass().getName());

        // Add a global string to the EngineContext Store for EngineInterceptorTest
        engineInterceptorContext.getEngineContext().getStore().put(KEY, VALUE);
    }

    @Override
    public void onTestDiscovery(
            EngineInterceptorContext engineInterceptorContext,
            List<ClassDefinition> classDefinitions) {
        System.out.printf("%s onTestDiscovery()%n", getClass().getName());

        for (ClassDefinition classDefinition : classDefinitions) {
            if (classDefinition.getTestClass() == EngineInterceptorTest1.class) {

                // Reverse test methods
                reverseMethodsDefinitions(classDefinition.getTestMethodDefinitions());

                // Filter test method "test4"
                classDefinition
                        .getTestMethodDefinitions()
                        .removeIf(
                                methodDefinition ->
                                        methodDefinition.getMethod().getName().equals("test4"));
            }
        }
    }

    @Override
    public void preExecute(EngineInterceptorContext engineInterceptorContext) {
        System.out.printf("%s preExecute()%n", getClass().getName());
    }

    @Override
    public void postExecute(EngineInterceptorContext engineInterceptorContext) {
        System.out.printf("%s postExecute()%n", getClass().getName());
    }

    @Override
    public void onDestroy(EngineInterceptorContext engineInterceptorContext) {
        System.out.printf("%s onDestroy()%n", getClass().getName());
    }

    /**
     * Method to reverse a Set of Methods
     *
     * @param methodDefinitions methodDefinitions
     */
    private static void reverseMethodsDefinitions(Set<MethodDefinition> methodDefinitions) {
        List<MethodDefinition> list = new ArrayList<>(methodDefinitions);
        Collections.reverse(list);
        methodDefinitions.clear();
        methodDefinitions.addAll(list);
    }
}
