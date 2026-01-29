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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.RandomSupport;
import org.verifyica.api.Verifyica;

public class ArgumentExecutorServiceSupplierTest {

    @Verifyica.ArgumentExecutorServiceSupplier
    public static ExecutorService executorService() {
        return Executors.newFixedThreadPool(2);
    }

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            collection.add("test-" + i);
        }

        return collection.stream();
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws InterruptedException {
        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();

        System.out.printf(
                "test(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        Thread.sleep(RandomSupport.nextLong(0, 1_000));
    }
}
