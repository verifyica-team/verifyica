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

package org.verifyica.examples.skip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.verifyica.api.Execution.skipIfCondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;
import org.verifyica.examples.support.Logger;

@SuppressWarnings("unchecked")
public class SkipTestMethodTest {

    private static final Logger LOGGER = Logger.createLogger(SkipTestMethodTest.class);

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Object arguments() {
        Collection<Argument<Integer>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(Argument.ofInt(i));
        }
        return collection;
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) {
        skipIfCondition(argumentContext.testArgument().payload(Integer.class) % 2 == 0);

        assertThat(argumentContext.getTestArgument().getPayload(Integer.class) % 2)
                .isOdd();

        LOGGER.info(
                "test1(name[%s], payload[%s])",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        storeState(argumentContext, argumentContext.getTestArgument().getPayload() + ".test1");
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) {
        LOGGER.info(
                "test2(name[%s], payload[%s])",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        storeState(argumentContext, argumentContext.getTestArgument().getPayload() + ".test2");
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        List<String> list = classContext.getMap().getAs("state");

        assertThat(list).isNotNull();
        assertThat(list).hasSize(15);

        for (int i = 0; i < 10; i++) {
            for (int j = 1; j <= 2; j++) {
                if (i % 2 == 1) {
                    assertThat(list).contains(i + ".test" + j);
                } else if (j == 2) {
                    assertThat(list).contains(i + ".test" + j);
                }
            }
        }
    }

    private static void storeState(ArgumentContext argumentContext, String state) {
        List<String> list = (List<String>) argumentContext
                .getClassContext()
                .getMap()
                .computeIfAbsent("state", object -> Collections.synchronizedList(new ArrayList<>()));
        list.add(state);
    }
}
