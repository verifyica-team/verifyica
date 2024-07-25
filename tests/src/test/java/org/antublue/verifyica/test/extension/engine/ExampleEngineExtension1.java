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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
    public Map<Class<?>, Set<Method>> onTestDiscovery(
            EngineExtensionContext engineExtensionContext,
            Map<Class<?>, Set<Method>> testClassMethodMap) {
        System.out.println(format("%s onTestDiscovery()", getClass().getName()));

        Map<Class<?>, Set<Method>> workingTestClassMethodMap =
                new LinkedHashMap<>(testClassMethodMap);

        for (Map.Entry<Class<?>, Set<Method>> mapEntry : testClassMethodMap.entrySet()) {
            Class<?> testClazz = mapEntry.getKey();
            if (testClazz
                    .getName()
                    .equals("org.antublue.verifyica.test.extension.engine.EngineExtensionTest")) {
                List<Method> testMethods = new ArrayList<>(mapEntry.getValue());
                Collections.reverse(testMethods);
                workingTestClassMethodMap.put(testClazz, new LinkedHashSet<>(testMethods));
            }
        }

        return workingTestClassMethodMap;
    }

    @Override
    public void beforeExecute(EngineExtensionContext engineExtensionContext) {
        System.out.println(format("%s beforeExecute()", getClass().getName()));
    }

    @Override
    public void afterExecute(EngineExtensionContext engineExtensionContext) {
        System.out.println(format("%s afterExecute()", getClass().getName()));
    }
}
