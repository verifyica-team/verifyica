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

package org.antublue.verifyica.test.argument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.util.Arrays;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;

public class EnumArgumentTest {

    private int index = 0;

    private enum Arguments {
        ZERO,
        ONE,
        TWO
    }

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return Arrays.asList(Arguments.ZERO, Arguments.ONE, Arguments.TWO);
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();

        System.out.printf(
                "test(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());

        switch (index) {
            case 0:
                {
                    assertThat(argumentContext.getTestArgument().getPayload())
                            .isEqualTo(Arguments.ZERO);
                    break;
                }
            case 1:
                {
                    assertThat(argumentContext.getTestArgument().getPayload())
                            .isEqualTo(Arguments.ONE);
                    break;
                }
            case 2:
                {
                    assertThat(argumentContext.getTestArgument().getPayload())
                            .isEqualTo(Arguments.TWO);
                    break;
                }
            default:
                {
                    fail("Should not happen");
                }
        }

        index++;
    }
}
