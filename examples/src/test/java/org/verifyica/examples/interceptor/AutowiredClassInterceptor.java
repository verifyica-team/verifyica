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

package org.verifyica.examples.interceptor;

import java.lang.reflect.Method;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.EngineContext;
import org.verifyica.api.Verifyica;

@Verifyica.Autowired
public class AutowiredClassInterceptor implements ClassInterceptor {

    private static final String CLASS_NAME = AutowiredClassInterceptor.class.getSimpleName();

    public void initialize(EngineContext engineContext) throws Throwable {
        System.out.printf("%s::initialize()%n", CLASS_NAME);
    }

    public void preTest(ArgumentContext argumentContext, Method testMethod) throws Throwable {
        System.out.printf(
                "%s::preTest() test class [%s] test method [%s]%n",
                CLASS_NAME, argumentContext.getClassContext().getTestClass().getSimpleName(), testMethod.getName());
    }

    public void postTest(ArgumentContext argumentContext, Method testMethod, Throwable throwable) throws Throwable {
        System.out.printf(
                "%s::postTest() test class [%s] test method [%s]%n",
                CLASS_NAME, argumentContext.getClassContext().getTestClass().getSimpleName(), testMethod.getName());

        rethrow(throwable);
    }

    public void destroy(EngineContext engineContext) throws Throwable {
        System.out.printf("%s::destroy()%n", AutowiredClassInterceptor.class.getName());
    }
}
