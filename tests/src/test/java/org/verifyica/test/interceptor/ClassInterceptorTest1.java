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

package org.verifyica.test.interceptor;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.Verifyica;

public class ClassInterceptorTest1 {

    @Verifyica.ClassInterceptorSupplier
    public static Collection<ClassInterceptor> classInterceptors() {
        Collection<ClassInterceptor> collection = new ArrayList<>();

        collection.add(new ExampleClassInterceptor1());
        collection.add(new ExampleClassInterceptor2());

        return collection;
    }

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "test";
    }

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) {
        System.out.printf("  %s prepare()%n", classContext.getTestClass().getName());
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        System.out.printf("  %s beforeAll()%n", argumentContext.getArgument().getPayload());
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) {
        System.out.printf("  %s beforeEach()%n", argumentContext.getArgument().getPayload());
    }

    @Verifyica.Test
    @Verifyica.Order(0)
    public void test1(ArgumentContext argumentContext) {
        System.out.printf("  %s test1()%n", argumentContext.getArgument().getPayload());
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void test2(ArgumentContext argumentContext) {
        System.out.printf("  %s test2()%n", argumentContext.getArgument().getPayload());
    }

    @Verifyica.Test
    @Verifyica.Order(2)
    public void test3(ArgumentContext argumentContext) {
        System.out.printf("  %s test3()%n", argumentContext.getArgument().getPayload());
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) {
        System.out.printf("  %s afterEach()%n", argumentContext.getArgument().getPayload());
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        System.out.printf("  %s afterAll()%n", argumentContext.getArgument().getPayload());
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        System.out.printf("  %s conclude()%n", classContext.getTestClass().getName());
    }
}
