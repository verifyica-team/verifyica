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

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.Verifyica;

public class ClassInterceptorTest {

    @Verifyica.ClassInterceptorSupplier
    public static Collection<ClassInterceptor> classInterceptors() {
        Collection<ClassInterceptor> collections = new ArrayList<>();
        collections.add(new CustomClassInterceptor());
        collections.add(new CustomClassInterceptor2());
        return collections;
    }

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add("string-" + i);
        }

        return collection;
    }

    @Verifyica.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @Verifyica.BeforeAll
    public void beforeAll(String argument) {
        System.out.printf("beforeAll() argument [%s]%n", argument);
    }

    @Verifyica.BeforeEach
    public void beforeEach(String argument) {
        System.out.printf("beforeEach() argument [%s]%n", argument);
    }

    @Verifyica.Test
    public void test1(String argument) {
        System.out.printf("test1() argument [%s]%n", argument);
    }

    @Verifyica.Test
    public void test2(String argument) {
        System.out.printf("test2() argument [%s]%n", argument);
    }

    @Verifyica.Test
    public void test3(String argument) {
        System.out.printf("test3() argument [%s]%n", argument);
    }

    @Verifyica.AfterEach
    public void afterEach(String argument) {
        System.out.printf("afterEach() argument [%s]%n", argument);
    }

    @Verifyica.AfterAll
    public void afterAll(String argument) {
        System.out.printf("afterAll() argument [%s]%n", argument);
    }

    @Verifyica.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }
}
