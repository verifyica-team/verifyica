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

package org.antublue.verifyica.engine.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.antublue.verifyica.api.Verifyica;
import org.junit.jupiter.api.Test;

public class OrderSupportTest {

    @Test
    public void testDefaultOrder() {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(TestClass2.class);
        classes.add(TestClass1.class);

        OrderSupport.orderClasses(classes);

        assertThat(classes).isNotNull();
        assertThat(classes).hasSize(2);
        assertThat(classes.get(0)).isEqualTo(TestClass1.class);
        assertThat(classes.get(1)).isEqualTo(TestClass2.class);
    }

    @Test
    public void testOrder() {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(TestClass2.class);
        classes.add(TestClass1.class);
        classes.add(TestClass3.class);

        OrderSupport.orderClasses(classes);

        assertThat(classes).isNotNull();
        assertThat(classes).hasSize(3);
        assertThat(classes.get(0)).isEqualTo(TestClass3.class);
        assertThat(classes.get(1)).isEqualTo(TestClass1.class);
        assertThat(classes.get(2)).isEqualTo(TestClass2.class);
    }

    @Test
    public void testDuplicateOrder() {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(TestClass2.class);
        classes.add(TestClass4.class);
        classes.add(TestClass1.class);
        classes.add(TestClass3.class);

        OrderSupport.orderClasses(classes);

        assertThat(classes).isNotNull();
        assertThat(classes).hasSize(4);
        assertThat(classes.get(0)).isEqualTo(TestClass3.class);
        assertThat(classes.get(1)).isEqualTo(TestClass4.class);
        assertThat(classes.get(2)).isEqualTo(TestClass1.class);
        assertThat(classes.get(3)).isEqualTo(TestClass2.class);
    }

    private static class TestClass1 {
        // INTENTIONALLY BLANK
    }

    private static class TestClass2 {
        // INTENTIONALLY BLANK
    }

    @Verifyica.Order(order = 0)
    private static class TestClass3 {
        // INTENTIONALLY BLANK
    }

    @Verifyica.Order(order = 0)
    private static class TestClass4 {
        // INTENTIONALLY BLANK
    }
}
