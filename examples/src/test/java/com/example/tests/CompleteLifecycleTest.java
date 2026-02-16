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

package com.example.tests;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

public class CompleteLifecycleTest {

    @Verifyica.ArgumentSupplier(parallelism = 2)
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add("string-" + i);
        }
        return collection;
    }

    @Verifyica.Prepare
    public void prepare(ClassContext classContext) {
        System.out.println("Prepare: Called once before all arguments");
    }

    @Verifyica.BeforeAll
    public void beforeAll(String argument) {
        System.out.println("BeforeAll: " + argument);
    }

    @Verifyica.BeforeEach
    public void beforeEach(String argument) {
        System.out.println("BeforeEach: " + argument);
    }

    @Verifyica.Test
    public void test1(String argument) {
        System.out.println("Test1: " + argument);
    }

    @Verifyica.Test
    public void test2(String argument) {
        System.out.println("Test2: " + argument);
    }

    @Verifyica.AfterEach
    public void afterEach(String argument) {
        System.out.println("AfterEach: " + argument);
    }

    @Verifyica.AfterAll
    public void afterAll(String argument) {
        System.out.println("AfterAll: " + argument);
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        System.out.println("Conclude: Called once after all arguments");
    }
}
