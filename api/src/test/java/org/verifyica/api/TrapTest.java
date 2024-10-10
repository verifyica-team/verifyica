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

package org.verifyica.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

public class TrapTest {

    @Test
    public void testIllegalArgument() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Trap(null).assertEmpty());
    }

    @Test
    public void testTrap() throws Throwable {
        Trap trap = new Trap(() -> System.out.println("successful Trap.Runnable"));

        assertThat(trap.isEmpty()).isTrue();
        trap.assertEmpty();
    }

    @Test
    public void testTrappedThrowable() throws Throwable {
        Trap trap = new Trap(() -> {
            throw new RuntimeException("FORCED");
        });

        assertThat(trap).isNotNull();
        assertThat(trap.isEmpty()).isFalse();
        assertThat(trap.throwable()).isNotNull();
        assertThat(trap.throwable()).isPresent();

        trap.throwable().ifPresent(throwable -> assertThat(throwable).isInstanceOf(RuntimeException.class));
    }
}
