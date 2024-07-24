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

import static java.lang.String.format;

import java.lang.reflect.Method;
import org.antublue.verifyica.api.extension.ArgumentExtensionContext;
import org.antublue.verifyica.api.extension.ClassExtension;
import org.antublue.verifyica.api.extension.ClassExtensionContext;
import org.antublue.verifyica.api.extension.engine.EngineExtensionContext;

/** Class to implement ExampleClassExtension */
public class ExampleClassExtension2 implements ClassExtension {

    /** Constructor */
    public ExampleClassExtension2() {
        System.out.println(format("%s constructor", getClass().getName()));
    }

    public void beforeInstantiate(EngineExtensionContext engineExtensionContext, Class<?> testClass)
            throws Throwable {
        System.out.println(format("%s beforeInstantiate()", getClass().getName()));
    }

    public void beforePrepare(ClassExtensionContext classExtensionContext) throws Throwable {
        System.out.println(
                format(
                        "%s beforePrepare(%s)",
                        getClass().getName(),
                        classExtensionContext.getClassContext().getTestClass().getName()));
    }

    public void beforeBeforeAll(ArgumentExtensionContext argumentExtensionContext)
            throws Throwable {
        System.out.println(
                format(
                        "%s beforeBeforeAll(%s %s)",
                        getClass().getName(),
                        argumentExtensionContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentExtensionContext.getArgumentContext().getTestArgument().getName()));
    }

    public void beforeBeforeEach(ArgumentExtensionContext argumentExtensionContext)
            throws Throwable {
        System.out.println(
                format(
                        "%s beforeBeforeEach(%s %s)",
                        getClass().getName(),
                        argumentExtensionContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentExtensionContext.getArgumentContext().getTestArgument().getName()));
    }

    public void beforeTest(ArgumentExtensionContext argumentExtensionContext, Method testMethod)
            throws Throwable {
        System.out.println(
                format(
                        "%s beforeTest(%s %s %s)",
                        getClass().getName(),
                        argumentExtensionContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentExtensionContext.getArgumentContext().getTestArgument().getName(),
                        testMethod.getName()));
    }

    public void afterTest(
            ArgumentExtensionContext argumentExtensionContext,
            Method testMethod,
            Throwable throwable)
            throws Throwable {
        System.out.println(
                format(
                        "%s afterTest(%s %s %s)",
                        getClass().getName(),
                        argumentExtensionContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentExtensionContext.getArgumentContext().getTestArgument().getName(),
                        testMethod.getName()));

        if (throwable != null) {
            throw throwable;
        }
    }

    public void beforeAfterEach(ArgumentExtensionContext argumentExtensionContext)
            throws Throwable {
        System.out.println(
                format(
                        "%s beforeAfterEach(%s %s)",
                        getClass().getName(),
                        argumentExtensionContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentExtensionContext.getArgumentContext().getTestArgument().getName()));
    }

    public void beforeAfterAll(ArgumentExtensionContext argumentExtensionContext) throws Throwable {
        System.out.println(
                format(
                        "%s beforeAfterAll(%s %s)",
                        getClass().getName(),
                        argumentExtensionContext
                                .getArgumentContext()
                                .getClassContext()
                                .getTestClass()
                                .getName(),
                        argumentExtensionContext.getArgumentContext().getTestArgument().getName()));
    }

    public void beforeConclude(ClassExtensionContext classExtensionContext) throws Throwable {
        System.out.println(
                format(
                        "%s beforeConclude(%s)",
                        getClass().getName(),
                        classExtensionContext.getClassContext().getTestClass().getName()));
    }

    public void beforeDestroy(ClassExtensionContext classExtensionContext) throws Throwable {
        System.out.println(format("%s beforeDestroy()", getClass().getName()));
    }
}
