/*
 * Copyright (C) Verifyica project authors and contributors
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

package org.verifyica.test.interceptor;

import java.util.UUID;
import org.verifyica.api.EngineContext;
import org.verifyica.api.EngineInterceptor;
import org.verifyica.api.Verifyica;

@Verifyica.Autowired
public class ExampleAutowiredEngineInterceptor1 implements EngineInterceptor {

    public static final String KEY = ExampleAutowiredEngineInterceptor1.class.getName() + ".key";
    public static final String VALUE = UUID.randomUUID().toString();

    @Override
    public void initialize(EngineContext engineContext) {
        System.out.printf("%s onInitialize()%n", getClass().getName());

        // Add a global string to the EngineContext Store for EngineInterceptorTest
        engineContext.getMap().put(KEY, VALUE);
    }

    @Override
    public void destroy(EngineContext engineContext) {
        System.out.printf("%s onDestroy()%n", getClass().getName());
    }
}
