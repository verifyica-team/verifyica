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

package org.antublue.verifyica.test.extension;

import java.util.UUID;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.EngineExtension;

/** Class to implement ExampleEngineExtension1 */
public class ExampleEngineExtension implements EngineExtension {

    public static final String KEY = ExampleEngineExtension.class.getName() + ".key";
    public static final String VALUE = UUID.randomUUID().toString();

    @Override
    public void initialize(EngineContext engineContext) {
        System.out.println(getClass().getName() + " initialize()");

        // Add a global string to the EngineContext Store
        engineContext.getStore().put(KEY, VALUE);
    }

    @Override
    public void destroy(EngineContext engineContext) {
        System.out.println(getClass().getName() + " destroy()");
    }
}
