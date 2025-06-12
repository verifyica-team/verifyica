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

package org.verifyica.test.argument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.util.Arrays;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;

public class EnumArgumentsTest {

    private int index = 0;

    public enum EnumArgument {
        ZERO,
        ONE,
        TWO
    }

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return Arrays.asList(EnumArgument.ZERO, EnumArgument.ONE, EnumArgument.TWO);
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void testDirectArgument(EnumArgument enumArgument) {
        System.out.printf("test(name[%s], payload[%s])%n", enumArgument, enumArgument);

        assertThat(enumArgument).isNotNull();
    }

    @Verifyica.Test
    @Verifyica.Order(2)
    public void testArgumentContext(ArgumentContext argumentContext) {
        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();

        System.out.printf(
                "test(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        switch (index) {
            case 0: {
                assertThat(argumentContext.getTestArgument().getPayload()).isEqualTo(EnumArgument.ZERO);
                break;
            }
            case 1: {
                assertThat(argumentContext.getTestArgument().getPayload()).isEqualTo(EnumArgument.ONE);
                break;
            }
            case 2: {
                assertThat(argumentContext.getTestArgument().getPayload()).isEqualTo(EnumArgument.TWO);
                break;
            }
            default: {
                fail("Should not happen");
            }
        }

        index++;
    }
}
