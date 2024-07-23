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

package org.antublue.verifyica.test.engine;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.antublue.verifyica.api.engine.EngineExtension;
import org.antublue.verifyica.api.engine.EngineExtensionContext;
import org.antublue.verifyica.api.engine.ExtensionResult;

/** Class to implement ExampleEngineExtension */
public class ExampleEngineExtension implements EngineExtension {

    public static final String KEY = ExampleEngineExtension.class.getName() + ".key";
    public static final String VALUE = UUID.randomUUID().toString();

    @Override
    public ExtensionResult onInitialize(EngineExtensionContext engineExtensionContext) {
        System.out.println(getClass().getName() + " onInitialize()");

        // Add a global string to the EngineContext Store for EngineExtensionTest
        engineExtensionContext.getEngineContext().getStore().put(KEY, VALUE);

        return ExtensionResult.PROCEED;
    }

    @Override
    public ExtensionResult onTestClassDiscovery(
            EngineExtensionContext engineExtensionContext, List<Class<?>> testClasses) {
        System.out.println(getClass().getName() + " onTestClassDiscovery()");

        // Print all test classes that were discovered
        testClasses.forEach(
                testClass -> System.out.println(format("  test class [%s]", testClass.getName())));

        return ExtensionResult.PROCEED;
    }

    @Override
    public ExtensionResult onTestClassTestMethodDiscovery(
            EngineExtensionContext engineExtensionContext,
            Class<?> testClass,
            List<Method> testMethods)
            throws Throwable {
        System.out.println(getClass().getName() + " onTestClassTestMethodDiscovery()");

        System.out.println(format("  test class [%s]", testClass.getName()));

        if (testClass.getName().equals("org.antublue.verifyica.test.engine.EngineExtensionTest")) {
            System.out.println("  reversing test methods ...");

            // Reverse the test methods
            Collections.reverse(testMethods);
        }

        // Print all test class test methods that were discovered
        testMethods.forEach(
                method -> {
                    System.out.println(format("    test method [%s]", method.getName()));
                });

        return ExtensionResult.PROCEED;
    }

    @Override
    public ExtensionResult onExecute(EngineExtensionContext engineExtensionContext) {
        System.out.println(getClass().getName() + " onExecute()");

        return ExtensionResult.PROCEED;
    }

    @Override
    public void onDestroy(EngineExtensionContext engineExtensionContext) {
        System.out.println(getClass().getName() + " onDestroy()");
    }
}
