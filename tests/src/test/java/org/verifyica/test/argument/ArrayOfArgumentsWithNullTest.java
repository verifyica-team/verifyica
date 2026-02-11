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

import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;

public class ArrayOfArgumentsWithNullTest {

    @Verifyica.ArgumentSupplier
    public static Argument<?>[] arguments() {
        Argument<?>[] arguments = new Argument<?>[3];

        for (int i = 0; i < arguments.length; i++) {
            if (i % 2 == 0) {
                arguments[i] = null;
            } else {
                arguments[i] = Argument.of("test" + i, "test" + i);
            }
        }

        return arguments;
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) {
        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getArgument()).isNotNull();

        if (!argumentContext.getArgumentAs(String.class).hasPayload()) {
            return;
        }

        assertThat(argumentContext.getArgumentAs(String.class).getPayload()).startsWith("test");

        System.out.printf(
                "test(name[%s], payload[%s])%n",
                argumentContext.getArgument(), argumentContext.getArgument().getPayload());
    }
}
