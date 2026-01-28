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

package org.verifyica.test;

import static org.verifyica.test.support.AssertionSupport.assertArgumentContext;
import static org.verifyica.test.support.AssertionSupport.assertClassContext;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

@Verifyica.Testable
public class NestedTest {

    public static class NestedTest1 {

        @Verifyica.ArgumentSupplier
        public static Collection<Argument<String>> arguments() {
            Collection<Argument<String>> collection = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                collection.add(Argument.ofString("NestedTest1 String " + i));
            }

            return collection;
        }

        @Verifyica.Prepare
        public static void prepare(ClassContext classContext) {
            assertClassContext(classContext);

            System.out.println("prepare()");
            System.out.printf("test argument parallelism [%d]%n", classContext.getTestArgumentParallelism());
        }

        @Verifyica.BeforeAll
        public void beforeAll(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "beforeAll(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.BeforeEach
        public void beforeEach(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "beforeEach(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.Test
        public void test1(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "test1(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.Test
        public void test2(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "test2(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.Test
        public void test3(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "test3(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.AfterEach
        public void afterEach(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "afterEach(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.AfterAll
        public void afterAll(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "afterAll(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.Conclude
        public static void conclude(ClassContext classContext) {
            assertClassContext(classContext);

            System.out.println("conclude()");
        }
    }

    public static class NestedTest2 {

        @Verifyica.ArgumentSupplier
        public static Collection<Argument<String>> arguments() {
            Collection<Argument<String>> collection = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                collection.add(Argument.ofString("NestedTest2 String " + i));
            }

            return collection;
        }

        @Verifyica.Prepare
        public void prepare(ClassContext classContext) {
            assertClassContext(classContext);

            System.out.println("prepare()");
            System.out.printf("test argument parallelism [%d]%n", classContext.getTestArgumentParallelism());
        }

        @Verifyica.BeforeAll
        public void beforeAll(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "beforeAll(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.BeforeEach
        public void beforeEach(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "beforeEach(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.Test
        public void test1(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "test1(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.Test
        public void test2(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "test2(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.Test
        public void test3(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "test3(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.AfterEach
        public void afterEach(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "afterEach(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.AfterAll
        public void afterAll(ArgumentContext argumentContext) {
            assertArgumentContext(argumentContext);

            System.out.printf(
                    "afterAll(index=[%d], name=[%s])%n",
                    argumentContext.getTestArgumentIndex(),
                    argumentContext.getTestArgument().getName());
        }

        @Verifyica.Conclude
        public void conclude(ClassContext classContext) {
            assertClassContext(classContext);

            System.out.println("conclude()");
        }
    }

    @Verifyica.Testable
    public static class NestedTestClassContainer {

        public static class DeepNestedTestClass {

            @Verifyica.ArgumentSupplier
            public static Collection<Argument<String>> arguments() {
                Collection<Argument<String>> collection = new ArrayList<>();

                for (int i = 0; i < 10; i++) {
                    collection.add(Argument.ofString("DeepNestedTestClass String " + i));
                }

                return collection;
            }

            @Verifyica.Test
            public void test(ArgumentContext argumentContext) {
                assertArgumentContext(argumentContext);

                System.out.printf(
                        "test(index=[%d], name=[%s])%n",
                        argumentContext.getTestArgumentIndex(),
                        argumentContext.getTestArgument().getName());
            }
        }
    }
}
