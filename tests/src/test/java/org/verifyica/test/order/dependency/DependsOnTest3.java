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

package org.verifyica.test.order.dependency;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

@SuppressWarnings("unchecked")
public class DependsOnTest3 {

    private static final String LIST = "list";

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return "test";
    }

    @Verifyica.Prepare
    public void prepare(ClassContext classContext) {
        classContext.map().put(LIST, new ArrayList<String>());
    }

    @Verifyica.Test
    @Verifyica.Tag("alpha")
    public void alpha(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "alpha(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("alpha");
    }

    @Verifyica.Test
    @Verifyica.Tag("beta")
    @Verifyica.DependsOn("alpha")
    public void beta(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "beta(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("beta");
    }

    @Verifyica.Test
    @Verifyica.Tag("gamma")
    @Verifyica.DependsOn("beta")
    public void gamma(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "gamma(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("gamma");
    }

    @Verifyica.Test
    @Verifyica.Tag("delta")
    @Verifyica.DependsOn("gamma")
    public void delta(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "delta(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("delta");
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        List<String> expected = new ArrayList<>();
        expected.add("alpha");
        expected.add("beta");
        expected.add("gamma");
        expected.add("delta");

        List<String> actual = classContext.map().getAs(LIST);

        assertThat(actual).isEqualTo(expected);
    }
}
