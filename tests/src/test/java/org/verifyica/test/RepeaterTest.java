/*
 * Copyright (C) Verifyica project authors and contributors
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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;
import org.verifyica.test.support.Repeater;

@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public class RepeaterTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return "test";
    }

    public void beforeEach(ArgumentContext argumentContext) {
        System.out.println("beforeEach()");

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getTestArgument().getPayload()).isEqualTo("test");
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) throws Throwable {
        new Repeater(10)
                .before(() -> beforeEach(argumentContext))
                .execute(() -> {
                    assertThat(argumentContext).isNotNull();
                    assertThat(argumentContext.getMap()).isNotNull();
                    assertThat(argumentContext.getTestArgument()).isNotNull();
                    assertThat(argumentContext.getTestArgument().getPayload()).isEqualTo("test");

                    System.out.printf(
                            "test1(name[%s], payload[%s])%n",
                            argumentContext.getTestArgument(),
                            argumentContext.getTestArgument().getPayload());
                })
                .after(() -> afterEach(argumentContext))
                .throttle(new Repeater.FixedThrottle(100))
                .execute();
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) throws Throwable {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> new Repeater(10)
                .before(() -> beforeEach(argumentContext))
                .execute(() -> {
                    assertThat(argumentContext).isNotNull();
                    assertThat(argumentContext.getMap()).isNotNull();
                    assertThat(argumentContext.getTestArgument()).isNotNull();
                    assertThat(argumentContext.getTestArgument().getPayload()).isEqualTo("test");

                    System.out.printf(
                            "test2(name[%s], payload[%s])%n",
                            argumentContext.getTestArgument(),
                            argumentContext.getTestArgument().getPayload());

                    throw new RuntimeException("Forced");
                })
                .after(() -> afterEach(argumentContext))
                .accept((counter, throwable) -> {
                    if (counter >= 10) {
                        Repeater.rethrow(throwable);
                    }
                })
                .throttle(new Repeater.ExponentialBackoffThrottle(10000))
                .execute());
    }

    public void afterEach(ArgumentContext argumentContext) {
        System.out.println("afterEach()");

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getTestArgument().getPayload()).isEqualTo("test");
    }
}
