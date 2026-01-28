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

package org.verifyica.test.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.verifyica.test.support.AssertionSupport.assertArgumentContext;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;

@Verifyica.Tag("Tag1")
public class TagTest1 {

    @Verifyica.ArgumentSupplier
    public static Collection<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();

        for (int i = 0; i < 1; i++) {
            collection.add(Argument.ofString("String " + i));
        }

        return collection;
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) {
        assertArgumentContext(argumentContext);
        assertThat(argumentContext.getClassContext().getTestClassTags()).isNotEmpty();
        assertThat(argumentContext.getClassContext().getTestClassTags()).contains("Tag1");

        System.out.printf("test1(%s)%n", argumentContext.getTestArgument());
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) {
        assertArgumentContext(argumentContext);

        System.out.printf("test2(%s)%n", argumentContext.getTestArgument());
    }

    @Verifyica.Test
    public void test3(ArgumentContext argumentContext) {
        assertArgumentContext(argumentContext);

        System.out.printf("test3(%s)%n", argumentContext.getTestArgument());
    }
}
