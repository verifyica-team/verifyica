/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

public class TestMethodElapsedTimeClassInterceptor implements ClassInterceptor {

    private static final String CLASS_NAME = TestMethodElapsedTimeClassInterceptor.class.getSimpleName();

    private static final String TIMESTAMP = "timestamp";

    @Override
    public void initialize(EngineContext engineContext) throws Throwable {
        System.out.printf("%s::initialize()%n", CLASS_NAME);
    }

    @Override
    public void preTest(ArgumentContext argumentContext, Method testMethod) throws Throwable {
        argumentContext.map().put(TIMESTAMP, System.nanoTime());

        System.out.printf(
                "%s::preTest() test class [%s] test method [%s]%n",
                CLASS_NAME, argumentContext.getClassContext().getTestClass().getSimpleName(), testMethod.getName());
    }

    @Override
    public void postTest(ArgumentContext argumentContext, Method testMethod, Throwable throwable) throws Throwable {
        System.out.printf(
                "%s::postTest() test class [%s] test method [%s]%n",
                CLASS_NAME, argumentContext.getClassContext().getTestClass().getSimpleName(), testMethod.getName());

        long elapsedTime = System.nanoTime() - argumentContext.map().removeAs(TIMESTAMP, Long.class);
        System.out.printf(
                "test class [%s] test method [%s] test argument [%s] elapsed time [%d] ns%n",
                argumentContext.classContext().testClass().getName(),
                testMethod.getName(),
                argumentContext.testArgument().name(),
                elapsedTime);

        rethrow(throwable);
    }

    @Override
    public void destroy(EngineContext engineContext) throws Throwable {
        System.out.printf("%s::destroy()%n", TestMethodElapsedTimeClassInterceptor.class.getName());
    }
}
