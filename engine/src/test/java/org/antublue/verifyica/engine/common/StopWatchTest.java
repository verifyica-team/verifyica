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

package org.antublue.verifyica.engine.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

public class StopWatchTest {

    @Test
    public void test() {
        StopWatch stopWatch = new StopWatch();

        assertThat(stopWatch.elapsedTime()).isNotNull();

        long start = System.currentTimeMillis();
        sleep();
        long stop = System.currentTimeMillis();

        assertThat(stopWatch.elapsedTime().toMillis()).isBetween(0L, (stop - start) + 50);

        sleep();

        stop = System.currentTimeMillis();

        assertThat(stopWatch.elapsedTime().toMillis()).isBetween(0L, (stop - start) + 50);

        stopWatch.stop();

        Duration duration = stopWatch.elapsedTime();
        assertThat(duration).isNotNull();

        sleep();

        Duration duration2 = stopWatch.elapsedTime();
        assertThat(duration2).isEqualTo(duration);

        start = System.currentTimeMillis();

        stopWatch.reset();
        assertThat(stopWatch.elapsedTime()).isNotNull();

        sleep();

        stop = System.currentTimeMillis();

        assertThat(stopWatch.elapsedTime().toMillis()).isBetween(0L, (stop - start) + 50);
    }

    private static void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
