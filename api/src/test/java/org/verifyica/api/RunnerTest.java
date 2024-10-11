/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class RunnerTest {

    @Test
    public void testIllegalArguments() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new Runner().perform((Runner.Task) null));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new Runner().perform((Runner.Task[]) null));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new Runner().perform((Collection<Runner.Task>) null));

        Runner.Task[] array = new Runner.Task[2];
        array[0] = new TestTask();
        array[1] = null;

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Runner().perform(array));

        Collection<Runner.Task> collection = new ArrayList<>();
        collection.add(new TestTask());
        collection.add(null);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Runner().perform(collection));
    }

    @Test
    public void testSingleThrowable() {
        Runner runner = new Runner().perform(new ExceptionTestTask());

        assertThat(runner).isNotNull();
        assertThat(runner.isNotEmpty()).isTrue();
        assertThat(runner.isEmpty()).isFalse();
        assertThat(runner.throwables()).isNotNull();
        assertThat(runner.throwables()).hasSize(1);
        assertThat(runner.firstThrowable()).isNotNull();

        Optional<Throwable> optional = runner.firstThrowable();

        assertThat(optional).isPresent();
        assertThat(optional.get()).isNotNull();
        assertThat(optional.get()).isInstanceOf(RuntimeException.class);

        assertThat(optional.get().getMessage()).isEqualTo("FORCED");

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(runner::assertSuccessful);
    }

    @Test
    public void testMixedThrowable() {
        Runner runner = new Runner().perform(new TestTask(), new ExceptionTestTask());

        assertThat(runner).isNotNull();
        assertThat(runner.isNotEmpty()).isTrue();
        assertThat(runner.isEmpty()).isFalse();
        assertThat(runner.throwables()).isNotNull();
        assertThat(runner.throwables()).hasSize(1);
        assertThat(runner.firstThrowable()).isNotNull();

        Optional<Throwable> optional = runner.firstThrowable();

        assertThat(optional).isPresent();
        assertThat(optional.get()).isNotNull();
        assertThat(optional.get()).isInstanceOf(RuntimeException.class);

        assertThat(optional.get().getMessage()).isEqualTo("FORCED");

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(runner::assertSuccessful);
    }

    private static class TestTask implements Runner.Task {

        @Override
        public void perform() {
            // INTENTIONALLY BLANK
        }
    }

    private static class ExceptionTestTask implements Runner.Task {

        @Override
        @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
        public void perform() {
            throw new RuntimeException("FORCED");
        }
    }
}
