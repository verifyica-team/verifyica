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

package org.antublue.verifyica.test;

import java.util.ArrayList;
import java.util.Collection;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;

@Verifyica.Disabled
@Verifyica.Stepwise
public class StepwiseTest {

    private static final String FAIL_ON_STEP_1 = "failOnStep1";
    private static final String FAIL_ON_STEP_2 = "failOnStep2";

    @Verifyica.ArgumentSupplier
    public static Collection<Argument<?>> arguments() {
        Collection<Argument<?>> collection = new ArrayList<>();

        collection.add(Argument.ofString(FAIL_ON_STEP_1));
        collection.add(Argument.ofString(FAIL_ON_STEP_2));

        return collection;
    }

    @Verifyica.Test
    public void step1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step1(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());

        if (argumentContext.getTestArgument().getPayload(String.class).equals(FAIL_ON_STEP_1)) {
            throw new java.lang.AssertionError("Forced");
        }
    }

    @Verifyica.Test
    public void step2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step2(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());

        if (argumentContext.getTestArgument().getPayload(String.class).equals(FAIL_ON_STEP_2)) {
            throw new java.lang.AssertionError("Forced");
        }
    }

    @Verifyica.Test
    public void step3(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step3(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());
    }
}