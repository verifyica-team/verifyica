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

package org.antublue.verifyica.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.antublue.verifyica.api.concurrency.ConcurrencySupport;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
public class ConcurrencySupportTest {

    @Test
    public void testLockReference() {
        ConcurrencySupport.LockReference lockReference1 =
                ConcurrencySupport.getLockReference("FOO");
        ConcurrencySupport.LockReference lockReference2 =
                ConcurrencySupport.getLockReference("FOO");

        assertThat(lockReference1).isNotNull();
        assertThat(lockReference2).isNotNull();

        assertThat(lockReference1.key()).isNotNull();
        assertThat(lockReference2.key()).isNotNull();

        assertThat(lockReference2.key()).isEqualTo(lockReference2.key());
        assertThat(lockReference2).isEqualTo(lockReference1);

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> assertThat(lockReference2).isSameAs(lockReference1));
    }
}
