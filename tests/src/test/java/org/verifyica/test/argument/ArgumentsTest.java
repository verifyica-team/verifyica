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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;

public class ArgumentsTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        Collection<Object> collection = new ArrayList<>();

        collection.add(Argument.ofBigDecimal(new BigDecimal("1.0")));
        collection.add(Argument.ofBigDecimal("1.0"));
        collection.add(Argument.ofBigInteger(new BigInteger("1")));
        collection.add(Argument.ofBigInteger("1"));
        collection.add(Argument.ofBoolean(true));
        collection.add(Argument.ofByte((byte) 1));
        collection.add(Argument.ofChar('a'));
        collection.add(Argument.ofDouble(1));
        collection.add(Argument.ofFloat((float) 1.0));
        collection.add(Argument.ofInt(1));
        collection.add(Argument.ofLong(1));
        collection.add(Argument.ofShort((short) 1));
        collection.add(Argument.ofString("a"));

        return collection.stream();
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) {
        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getArgument()).isNotNull();

        System.out.printf(
                "test(name[%s], payload[%s])%n",
                argumentContext.getArgument(), argumentContext.getArgument().getPayload());
    }
}
