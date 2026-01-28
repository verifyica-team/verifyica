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

package org.verifyica.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public class TrapTest {

    @Test
    public void testIllegalArgument() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Trap.assertEmpty((Trap) null));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Trap(null).assertEmpty());
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Trap.assertEmpty((Trap[]) null));

        Trap[] array = new Trap[2];
        array[0] = new Trap(() -> {});
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Trap.assertEmpty(array));

        Collection<Trap> collection = new ArrayList<>();
        collection.add(new Trap(() -> {}));
        collection.add(null);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Trap.assertEmpty(collection));
    }

    @Test
    public void testTrap() throws Throwable {
        Trap trap = new Trap(() -> {});

        assertThat(trap.isEmpty()).isTrue();
        trap.assertEmpty();
        Trap.assertEmpty(trap);
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
