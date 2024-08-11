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

import java.util.ArrayList;
import java.util.Collection;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.ClassInterceptor;

/** Example test */
public class ClassInterceptorTest4 {

    @Verifyica.ClassInterceptorSupplier
    public static ClassInterceptor interceptors1() {
        System.out.printf("%s interceptors1()%n", ClassInterceptorTest4.class.getName());

        return new ExampleClassInterceptor1();
    }

    @Verifyica.ClassInterceptorSupplier
    public static Collection<ClassInterceptor> interceptors2() {
        System.out.printf("%s interceptors2()%n", ClassInterceptorTest4.class.getName());

        Collection<ClassInterceptor> collection = new ArrayList<>();

        collection.add(new ExampleClassInterceptor1());
        collection.add(new ExampleClassInterceptor2());

        return collection;
    }

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "dummy";
    }

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) throws Throwable {
        System.out.printf("  %s prepare()%n", classContext.getTestClass().getName());
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s beforeAll()%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s beforeEach()%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test
    @Verifyica.Order(order = 0)
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s test1()%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test
    @Verifyica.Order(order = 1)
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s test2()%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test
    @Verifyica.Order(order = 2)
    public void test3(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s test3()%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s afterEach()%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s afterAll()%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) throws Throwable {
        System.out.printf("  %s conclude()%n", classContext.getTestClass().getName());
    }
}
