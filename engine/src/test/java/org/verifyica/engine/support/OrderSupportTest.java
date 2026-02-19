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

package org.verifyica.engine.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.verifyica.api.Verifyica;

@DisplayName("OrderSupport Tests")
public class OrderSupportTest {

    @Test
    @DisplayName("Should order classes with default order")
    public void testDefaultOrder() {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(TestClass1.class);
        classes.add(TestClass2.class);

        for (int i = 0; i < 4; i++) {
            Collections.shuffle(classes);
        }

        OrderSupport.orderClasses(classes);

        assertThat(classes).isNotNull();
        assertThat(classes).hasSize(2);
        assertThat(classes.get(0)).isEqualTo(TestClass1.class);
        assertThat(classes.get(1)).isEqualTo(TestClass2.class);
    }

    @Test
    @DisplayName("Should order classes with explicit order")
    public void testOrder() {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(TestClass1.class);
        classes.add(TestClass2.class);
        classes.add(TestClass3.class);

        for (int i = 0; i < 4; i++) {
            Collections.shuffle(classes);
        }

        OrderSupport.orderClasses(classes);

        assertThat(classes).isNotNull();
        assertThat(classes).hasSize(3);
        assertThat(classes.get(0)).isEqualTo(TestClass3.class);
        assertThat(classes.get(1)).isEqualTo(TestClass1.class);
        assertThat(classes.get(2)).isEqualTo(TestClass2.class);
    }

    @Test
    @DisplayName("Should order classes with duplicate order values")
    public void testDuplicateOrder() {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(TestClass1.class);
        classes.add(TestClass2.class);
        classes.add(TestClass3.class);
        classes.add(TestClass4.class);

        for (int i = 0; i < 4; i++) {
            Collections.shuffle(classes);
        }

        OrderSupport.orderClasses(classes);

        assertThat(classes).isNotNull();
        assertThat(classes).hasSize(4);
        assertThat(classes.get(0)).isEqualTo(TestClass3.class);
        assertThat(classes.get(1)).isEqualTo(TestClass4.class);
        assertThat(classes.get(2)).isEqualTo(TestClass1.class);
        assertThat(classes.get(3)).isEqualTo(TestClass2.class);
    }

    private static class TestClass1 {
        // INTENTIONALLY EMPTY
    }

    private static class TestClass2 {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Order(1)
    private static class TestClass3 {
        // INTENTIONALLY EMPTY
    }

    @Verifyica.Order(1)
    private static class TestClass4 {
        // INTENTIONALLY EMPTY
    }
}
