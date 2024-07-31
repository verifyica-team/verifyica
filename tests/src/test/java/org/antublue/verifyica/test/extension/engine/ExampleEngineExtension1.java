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

package org.antublue.verifyica.test.extension.engine;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.antublue.verifyica.api.extension.TestClassDefinition;
import org.antublue.verifyica.api.extension.engine.EngineExtension;
import org.antublue.verifyica.api.extension.engine.EngineExtensionContext;

/** Class to implement ExampleEngineExtension1 */
public class ExampleEngineExtension1 implements EngineExtension {

    public static final String KEY = ExampleEngineExtension1.class.getName() + ".key";
    public static final String VALUE = UUID.randomUUID().toString();

    @Override
    public void onInitialize(EngineExtensionContext engineExtensionContext) {
        System.out.println(format("%s onInitialize()", getClass().getName()));

        // Add a global string to the EngineContext Store for EngineExtensionTest
        engineExtensionContext.getEngineContext().getStore().put(KEY, VALUE);
    }

    @Override
    public void onTestDiscovery(
            EngineExtensionContext engineExtensionContext,
            List<TestClassDefinition> testClassDefinitions) {
        System.out.println(format("%s onTestDiscovery()", getClass().getName()));

        for (TestClassDefinition testClassDefinition : testClassDefinitions) {
            if (testClassDefinition
                    .getTestClass()
                    .getName()
                    .equals("org.antublue.verifyica.test.extension.engine.EngineExtensionTest1")) {

                // Reverse test methods
                reverseMethods(testClassDefinition.getTestMethods());

                // Filter test method "test4"
                testClassDefinition
                        .getTestMethods()
                        .removeIf(method -> method.getName().equals("test4"));
            }
        }
    }

    @Override
    public void beforeExecute(EngineExtensionContext engineExtensionContext) {
        System.out.println(format("%s beforeExecute()", getClass().getName()));
    }

    @Override
    public void afterExecute(EngineExtensionContext engineExtensionContext) {
        System.out.println(format("%s afterExecute()", getClass().getName()));
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
