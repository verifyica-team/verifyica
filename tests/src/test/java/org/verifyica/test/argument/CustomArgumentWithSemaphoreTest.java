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

package org.verifyica.test.argument;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.RandomSupport;
import org.verifyica.api.Verifyica;

public class CustomArgumentWithSemaphoreTest {

    @Verifyica.ArgumentSupplier
    public static Collection<CustomArgument> arguments() {
        Semaphore semaphore = new Semaphore(3);

        Collection<CustomArgument> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(new CustomArgument(semaphore, "String " + i));
        }

        return collection;
    }

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) {
        System.out.println("prepare()");

        assertThat(classContext).isNotNull();
        assertThat(classContext.getMap()).isNotNull();
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws InterruptedException {
        argumentContext.getTestArgument(CustomArgument.class).getPayload().acquire();

        System.out.printf("beforeAll(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) {
        System.out.printf("beforeEach(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) throws InterruptedException {
        System.out.printf("test1(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();

        Thread.sleep(RandomSupport.nextLong(0, 1_000));
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) throws InterruptedException {
        System.out.printf("test2(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();

        Thread.sleep(RandomSupport.nextLong(0, 1_000));
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) {
        System.out.printf("afterEach(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        System.out.printf("afterAll(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();

        argumentContext.getTestArgument(CustomArgument.class).getPayload().release();
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        System.out.println("conclude()");

        assertThat(classContext).isNotNull();
        assertThat(classContext.getMap()).isNotNull();
    }

    public static class CustomArgument implements Argument<CustomArgument> {

        private final Semaphore semaphore;
        private final String value;

        public CustomArgument(Semaphore semaphore, String value) {
            this.semaphore = semaphore;
            this.value = value;
        }

        @Override
        public String getName() {
            return value;
        }

        @Override
        public CustomArgument getPayload() {
            return this;
        }

        public void acquire() throws InterruptedException {
            semaphore.acquire();
        }

        public void release() {
            semaphore.release();
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
