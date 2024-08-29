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

package org.antublue.verifyica.test.interceptor;

import java.lang.reflect.Method;
import org.antublue.verifyica.api.interceptor.ArgumentInterceptorContext;
import org.antublue.verifyica.api.interceptor.ClassInterceptor;
import org.antublue.verifyica.api.interceptor.ClassInterceptorContext;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;

public class ExampleClassInterceptor2 implements ClassInterceptor {

    /** Constructor */
    public ExampleClassInterceptor2() {
        System.out.printf("%s constructor%n", getClass().getName());
    }

    @Override
    public void preInstantiate(
            EngineInterceptorContext engineInterceptorContext, Class<?> testClass)
            throws Throwable {
        System.out.printf("%s preInstantiate()%n", getClass().getName());
    }

    @Override
    public void postInstantiate(
            EngineInterceptorContext engineInterceptorContext,
            Class<?> testClass,
            Object testInstance,
            Throwable throwable)
            throws Throwable {
        System.out.printf("%s postInstantiate()%n", getClass().getName());
        rethrow(throwable);
    }

    @Override
    public void prePrepare(ClassInterceptorContext classInterceptorContext) throws Throwable {
        System.out.printf(
                "%s prePrepare(%s)%n",
                getClass().getName(),
                classInterceptorContext.getClassContext().getTestClass().getName());
    }

    @Override
    public void postPrepare(ClassInterceptorContext classInterceptorContext, Throwable throwable)
            throws Throwable {
        System.out.printf(
                "%s postPrepare(%s)%n",
                getClass().getName(),
                classInterceptorContext.getClassContext().getTestClass().getName());
        rethrow(throwable);
    }

    @Override
    public void preBeforeAll(ArgumentInterceptorContext argumentInterceptorContext)
            throws Throwable {
        System.out.printf(
                "%s preBeforeAll(%s %s)%n",
                getClass().getName(),
                argumentInterceptorContext
                        .getArgumentContext()
                        .getClassContext()
                        .getTestClass()
                        .getName(),
                argumentInterceptorContext.getArgumentContext().getTestArgument().getName());
    }

    @Override
    public void postBeforeAll(
            ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        System.out.printf(
                "%s postBeforeAll(%s %s)%n",
                getClass().getName(),
                argumentInterceptorContext
                        .getArgumentContext()
                        .getClassContext()
                        .getTestClass()
                        .getName(),
                argumentInterceptorContext.getArgumentContext().getTestArgument().getName());
        rethrow(throwable);
    }

    @Override
    public void preBeforeEach(ArgumentInterceptorContext argumentInterceptorContext)
            throws Throwable {
        System.out.printf(
                "%s preBeforeEach(%s %s)%n",
                getClass().getName(),
                argumentInterceptorContext
                        .getArgumentContext()
                        .getClassContext()
                        .getTestClass()
                        .getName(),
                argumentInterceptorContext.getArgumentContext().getTestArgument().getName());
    }

    @Override
    public void postBeforeEach(
            ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        System.out.printf(
                "%s postBeforeEach(%s %s)%n",
                getClass().getName(),
                argumentInterceptorContext
                        .getArgumentContext()
                        .getClassContext()
                        .getTestClass()
                        .getName(),
                argumentInterceptorContext.getArgumentContext().getTestArgument().getName());
        rethrow(throwable);
    }

    @Override
    public void preTest(ArgumentInterceptorContext argumentInterceptorContext, Method testMethod)
            throws Throwable {
        System.out.printf(
                "%s preTest(%s %s %s)%n",
                getClass().getName(),
                argumentInterceptorContext
                        .getArgumentContext()
                        .getClassContext()
                        .getTestClass()
                        .getName(),
                argumentInterceptorContext.getArgumentContext().getTestArgument().getName(),
                testMethod.getName());
    }

    @Override
    public void postTest(
            ArgumentInterceptorContext argumentInterceptorContext,
            Method testMethod,
            Throwable throwable)
            throws Throwable {
        System.out.printf(
                "%s postTest(%s %s %s)%n",
                getClass().getName(),
                argumentInterceptorContext
                        .getArgumentContext()
                        .getClassContext()
                        .getTestClass()
                        .getName(),
                argumentInterceptorContext.getArgumentContext().getTestArgument().getName(),
                testMethod.getName());
        rethrow(throwable);
    }

    @Override
    public void preAfterEach(ArgumentInterceptorContext argumentInterceptorContext)
            throws Throwable {
        System.out.printf(
                "%s preAfterEach(%s %s)%n",
                getClass().getName(),
                argumentInterceptorContext
                        .getArgumentContext()
                        .getClassContext()
                        .getTestClass()
                        .getName(),
                argumentInterceptorContext.getArgumentContext().getTestArgument().getName());
    }

    @Override
    public void postAfterEach(
            ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        System.out.printf(
                "%s postAfterEach(%s %s)%n",
                getClass().getName(),
                argumentInterceptorContext
                        .getArgumentContext()
                        .getClassContext()
                        .getTestClass()
                        .getName(),
                argumentInterceptorContext.getArgumentContext().getTestArgument().getName());
        rethrow(throwable);
    }

    @Override
    public void preAfterAll(ArgumentInterceptorContext argumentInterceptorContext)
            throws Throwable {
        System.out.printf(
                "%s preAfterAll(%s %s)%n",
                getClass().getName(),
                argumentInterceptorContext
                        .getArgumentContext()
                        .getClassContext()
                        .getTestClass()
                        .getName(),
                argumentInterceptorContext.getArgumentContext().getTestArgument().getName());
    }

    @Override
    public void postAfterAll(
            ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        System.out.printf(
                "%s postAfterAll(%s %s)%n",
                getClass().getName(),
                argumentInterceptorContext
                        .getArgumentContext()
                        .getClassContext()
                        .getTestClass()
                        .getName(),
                argumentInterceptorContext.getArgumentContext().getTestArgument().getName());
        rethrow(throwable);
    }

    @Override
    public void preConclude(ClassInterceptorContext classInterceptorContext) throws Throwable {
        System.out.printf(
                "%s preConclude(%s)%n",
                getClass().getName(),
                classInterceptorContext.getClassContext().getTestClass().getName());
    }

    @Override
    public void postConclude(ClassInterceptorContext classInterceptorContext, Throwable throwable)
            throws Throwable {
        System.out.printf(
                "%s postConclude(%s)%n",
                getClass().getName(),
                classInterceptorContext.getClassContext().getTestClass().getName());
        rethrow(throwable);
    }

    @Override
    public void onDestroy(ClassInterceptorContext classInterceptorContext) throws Throwable {
        System.out.printf("%s onDestroy()%n", getClass().getName());
    }
}
