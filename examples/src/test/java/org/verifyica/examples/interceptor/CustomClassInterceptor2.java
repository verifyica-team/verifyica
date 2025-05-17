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

package org.verifyica.examples.interceptor;

import java.lang.reflect.Method;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.EngineContext;
import org.verifyica.examples.support.Logger;

public class CustomClassInterceptor2 implements ClassInterceptor {

    private static final Logger LOGGER = Logger.createLogger(CustomClassInterceptor2.class);

    @Override
    public void initialize(EngineContext engineContext) throws Throwable {
        LOGGER.info("initialize()");
    }

    @Override
    public void preTest(ArgumentContext argumentContext, Method testMethod) throws Throwable {
        LOGGER.info(
                "preTest() test class [%s] test method [%s]",
                argumentContext.getClassContext().getTestClass().getSimpleName(), testMethod.getName());
    }

    @Override
    public void postTest(ArgumentContext argumentContext, Method testMethod, Throwable throwable) throws Throwable {
        LOGGER.info(
                "postTest() test class [%s] test method [%s]",
                argumentContext.getClassContext().getTestClass().getSimpleName(), testMethod.getName());

        rethrow(throwable);
    }

    @Override
    public void destroy(EngineContext engineContext) throws Throwable {
        LOGGER.info("destroy()");
    }
}
