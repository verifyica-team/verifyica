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

import java.lang.reflect.Method;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.EngineContext;

public class ExampleClassInterceptor1 implements ClassInterceptor {

    /**
     * Constructor
     */
    public ExampleClassInterceptor1() {
        System.out.printf("%s constructor%n", getClass().getName());
    }

    @Override
    public void preInstantiate(EngineContext engineContext, Class<?> testClass) {
        System.out.printf("%s preInstantiate()%n", getClass().getName());
    }

    @Override
    public void postInstantiate(
            EngineContext engineContext, Class<?> testClass, Object testInstance, Throwable throwable)
            throws Throwable {
        System.out.printf("%s postInstantiate()%n", getClass().getName());
        rethrow(throwable);
    }

    @Override
    public void prePrepare(ClassContext classContext) {
        System.out.printf(
                "%s prePrepare(%s)%n",
                getClass().getName(), classContext.getTestClass().getName());
    }

    @Override
    public void postPrepare(ClassContext classContext, Throwable throwable) throws Throwable {
        System.out.printf(
                "%s postPrepare(%s)%n",
                getClass().getName(), classContext.getTestClass().getName());
        rethrow(throwable);
    }

    @Override
    public void preBeforeAll(ArgumentContext argumentContext) {
        System.out.printf(
                "%s preBeforeAll(%s %s)%n",
                getClass().getName(),
                argumentContext.getClassContext().getTestClass().getName(),
                argumentContext.getTestArgument().getName());
    }

    @Override
    public void postBeforeAll(ArgumentContext argumentContext, Throwable throwable) throws Throwable {
        System.out.printf(
                "%s postBeforeAll(%s %s)%n",
                getClass().getName(),
                argumentContext.getClassContext().getTestClass().getName(),
                argumentContext.getTestArgument().getName());
        rethrow(throwable);
    }

    @Override
    public void preBeforeEach(ArgumentContext argumentContext) {
        System.out.printf(
                "%s preBeforeEach(%s %s)%n",
                getClass().getName(),
                argumentContext.getClassContext().getTestClass().getName(),
                argumentContext.getTestArgument().getName());
    }

    @Override
    public void postBeforeEach(ArgumentContext argumentContext, Throwable throwable) throws Throwable {
        System.out.printf(
                "%s postBeforeEach(%s %s)%n",
                getClass().getName(),
                argumentContext.getClassContext().getTestClass().getName(),
                argumentContext.getTestArgument().getName());
        rethrow(throwable);
    }

    @Override
    public void preTest(ArgumentContext argumentContext, Method testMethod) {
        System.out.printf(
                "%s preTest(%s %s %s)%n",
                getClass().getName(),
                argumentContext.getClassContext().getTestClass().getName(),
                argumentContext.getTestArgument().getName(),
                testMethod.getName());
    }

    @Override
    public void postTest(ArgumentContext argumentContext, Method testMethod, Throwable throwable) throws Throwable {
        System.out.printf(
                "%s postTest(%s %s %s)%n",
                getClass().getName(),
                argumentContext.getClassContext().getTestClass().getName(),
                argumentContext.getTestArgument().getName(),
                testMethod.getName());
        rethrow(throwable);
    }

    @Override
    public void preAfterEach(ArgumentContext argumentContext) {
        System.out.printf(
                "%s preAfterEach(%s %s)%n",
                getClass().getName(),
                argumentContext.getClassContext().getTestClass().getName(),
                argumentContext.getTestArgument().getName());
    }

    @Override
    public void postAfterEach(ArgumentContext argumentContext, Throwable throwable) throws Throwable {
        System.out.printf(
                "%s postAfterEach(%s %s)%n",
                getClass().getName(),
                argumentContext.getClassContext().getTestClass().getName(),
                argumentContext.getTestArgument().getName());
        rethrow(throwable);
    }

    @Override
    public void preAfterAll(ArgumentContext argumentContext) {
        System.out.printf(
                "%s preAfterAll(%s %s)%n",
                getClass().getName(),
                argumentContext.getClassContext().getTestClass().getName(),
                argumentContext.getTestArgument().getName());
    }

    @Override
    public void postAfterAll(ArgumentContext argumentContext, Throwable throwable) throws Throwable {
        System.out.printf(
                "%s postAfterAll(%s %s)%n",
                getClass().getName(),
                argumentContext.getClassContext().getTestClass().getName(),
                argumentContext.getTestArgument().getName());
        rethrow(throwable);
    }

    @Override
    public void preConclude(ClassContext classContext) {
        System.out.printf(
                "%s preConclude(%s)%n",
                getClass().getName(), classContext.getTestClass().getName());
    }

    @Override
    public void postConclude(ClassContext classContext, Throwable throwable) throws Throwable {
        System.out.printf(
                "%s postConclude(%s)%n",
                getClass().getName(), classContext.getTestClass().getName());
        rethrow(throwable);
    }

    @Override
    public void onDestroy(ClassContext classContext) {
        System.out.printf("%s onDestroy()%n", getClass().getName());
    }
}
