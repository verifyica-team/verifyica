/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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
import org.verifyica.api.ClassContext;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.EngineContext;
import org.verifyica.api.Verifyica;
import org.verifyica.examples.support.Logger;

@Verifyica.Autowired
public class ElapsedTimeClassInterceptor implements ClassInterceptor {

    private static final Logger LOGGER = Logger.createLogger(ElapsedTimeClassInterceptor.class);

    private static final String TIMESTAMP = "timestamp";

    @Override
    public void initialize(EngineContext engineContext) {
        LOGGER.info("initialize()");
    }

    @Override
    public void prePrepare(ClassContext classContext) {
        classContext.map().put(TIMESTAMP, System.nanoTime());
    }

    @Override
    public void preTest(ArgumentContext argumentContext, Method testMethod) {
        argumentContext.map().put(TIMESTAMP, System.nanoTime());
    }

    @Override
    public void postTest(ArgumentContext argumentContext, Method testMethod, Throwable throwable) throws Throwable {
        long elapsedTime = System.nanoTime() - argumentContext.map().removeAs(TIMESTAMP, Long.class);
        LOGGER.info(
                "test class [%s] test argument [%s] test method [%s] elapsed time [%f] ms",
                argumentContext.classContext().testClass().getName(),
                argumentContext.testArgument().name(),
                testMethod.getName(),
                elapsedTime / 1_000_000.0);

        rethrow(throwable);
    }

    @Override
    public void postConclude(ClassContext classContext, Throwable throwable) throws Throwable {
        long elapsedTime = System.nanoTime() - classContext.map().removeAs(TIMESTAMP, Long.class);
        LOGGER.info(
                "test class [%s] elapsed time [%f] ms", classContext.testClass().getName(), elapsedTime / 1_000_000.0);

        rethrow(throwable);
    }

    @Override
    public void destroy(EngineContext engineContext) throws Throwable {
        LOGGER.info("destroy()", ElapsedTimeClassInterceptor.class.getName());
    }
}
