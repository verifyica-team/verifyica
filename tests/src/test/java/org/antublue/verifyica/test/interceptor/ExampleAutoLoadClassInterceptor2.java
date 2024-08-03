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

import static java.lang.String.format;

import java.lang.reflect.Method;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.ArgumentInterceptorContext;
import org.antublue.verifyica.api.interceptor.ClassInterceptor;
import org.antublue.verifyica.api.interceptor.ClassInterceptorContext;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;

/** Class to implement ExampleAutoLoadClassInterceptor2 */
@Verifyica.AutowiredInterceptor
public class ExampleAutoLoadClassInterceptor2 implements ClassInterceptor {

    /** Constructor */
    public ExampleAutoLoadClassInterceptor2() {
        System.out.println(format("%s constructor", getClass().getName()));
    }

    @Override
    public void preInstantiate(
            EngineInterceptorContext engineInterceptorContext, Class<?> testClass)
            throws Throwable {
        System.out.println(format("%s preInstantiate()", getClass().getName()));
    }

    @Override
    public void postInstantiate(
            EngineInterceptorContext engineInterceptorContext,
            Class<?> testClass,
            Object testInstance,
            Throwable throwable)
            throws Throwable {
        System.out.println(format("%s postInstantiate()", getClass().getName()));
        if (throwable != null) {
            throw throwable;
        }
    }

    @Override
    public void prePrepare(ClassInterceptorContext classInterceptorContext) throws Throwable {
        System.out.println(
                format(
                        "%s prePrepare(%s)",
                        getClass().getName(),
                        classInterceptorContext.getClassContext().getTestClass().getName()));
    }

    @Override
    public void postPrepare(ClassInterceptorContext classInterceptorContext, Throwable throwable)
            throws Throwable {
        System.out.println(
                format(
                        "%s postPrepare(%s)",
                        getClass().getName(),
                        classInterceptorContext.getClassContext().getTestClass().getName()));
        if (throwable != null) {
            throw throwable;
        }
    }

    @Override
    public void preBeforeAll(ArgumentInterceptorContext argumentInterceptorContext)
            throws Throwable {
        System.out.println(
                format(
                        "%s preBeforeAll(%s %s)",
                        getClass().getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getTestArgument()
                                .getName()));
    }

    @Override
    public void postBeforeAll(
            ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        System.out.println(
                format(
                        "%s postBeforeAll(%s %s)",
                        getClass().getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getTestArgument()
                                .getName()));
        if (throwable != null) {
            throw throwable;
        }
    }

    @Override
    public void preBeforeEach(ArgumentInterceptorContext argumentInterceptorContext)
            throws Throwable {
        System.out.println(
                format(
                        "%s preBeforeEach(%s %s)",
                        getClass().getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getTestArgument()
                                .getName()));
    }

    @Override
    public void postBeforeEach(
            ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        System.out.println(
                format(
                        "%s postBeforeEach(%s %s)",
                        getClass().getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getTestArgument()
                                .getName()));
        if (throwable != null) {
            throw throwable;
        }
    }

    @Override
    public void preTest(ArgumentInterceptorContext argumentInterceptorContext, Method testMethod)
            throws Throwable {
        System.out.println(
                format(
                        "%s preTest(%s %s %s)",
                        getClass().getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentInterceptorContext.getArgumentContext().getTestArgument().getName(),
                        testMethod.getName()));
    }

    @Override
    public void postTest(
            ArgumentInterceptorContext argumentInterceptorContext,
            Method testMethod,
            Throwable throwable)
            throws Throwable {
        System.out.println(
                format(
                        "%s postTest(%s %s %s)",
                        getClass().getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentInterceptorContext.getArgumentContext().getTestArgument().getName(),
                        testMethod.getName()));

        if (throwable != null) {
            throw throwable;
        }
    }

    @Override
    public void preAfterEach(ArgumentInterceptorContext argumentInterceptorContext)
            throws Throwable {
        System.out.println(
                format(
                        "%s preAfterEach(%s %s)",
                        getClass().getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getTestArgument()
                                .getName()));
    }

    @Override
    public void postAfterEach(
            ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        System.out.println(
                format(
                        "%s postAfterEach(%s %s)",
                        getClass().getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getTestArgument()
                                .getName()));
        if (throwable != null) {
            throw throwable;
        }
    }

    @Override
    public void preAfterAll(ArgumentInterceptorContext argumentInterceptorContext)
            throws Throwable {
        System.out.println(
                format(
                        "%s preAfterAll(%s %s)",
                        getClass().getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getTestArgument()
                                .getName()));
    }

    @Override
    public void postAfterAll(
            ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        System.out.println(
                format(
                        "%s postAfterAll(%s %s)",
                        getClass().getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentInterceptorContext
                                .getArgumentContext()
                                .getTestArgument()
                                .getName()));
        if (throwable != null) {
            throw throwable;
        }
    }

    @Override
    public void preConclude(ClassInterceptorContext classInterceptorContext) throws Throwable {
        System.out.println(
                format(
                        "%s preConclude(%s)",
                        getClass().getName(),
                        classInterceptorContext.getClassContext().getTestClass().getName()));
    }

    @Override
    public void postConclude(ClassInterceptorContext classInterceptorContext, Throwable throwable)
            throws Throwable {
        System.out.println(
                format(
                        "%s postConclude(%s)",
                        getClass().getName(),
                        classInterceptorContext.getClassContext().getTestClass().getName()));
        if (throwable != null) {
            throw throwable;
        }
    }

    @Override
    public void onDestroy(ClassInterceptorContext classInterceptorContext) throws Throwable {
        System.out.println(
                format(
                        "%s onDestroy(%s)",
                        getClass().getName(),
                        classInterceptorContext.getClassContext().getTestClass().getName()));
    }
}
