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

package org.verifyica.engine.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.verifyica.engine.descriptor.TestDescriptorStatus.Type;

@DisplayName("TestDescriptorStatus Tests")
public class TestDescriptorStatusTest {

    @Test
    @DisplayName("Should create passed status")
    public void shouldCreatePassedStatus() {
        TestDescriptorStatus status = TestDescriptorStatus.passed();

        assertThat(status.getType()).isEqualTo(Type.PASSED);
        assertThat(status.isSuccess()).isTrue();
        assertThat(status.isFailure()).isFalse();
        assertThat(status.isSkipped()).isFalse();
    }

    @Test
    @DisplayName("Should create skipped status")
    public void shouldCreateSkippedStatus() {
        TestDescriptorStatus status = TestDescriptorStatus.skipped();

        assertThat(status.getType()).isEqualTo(Type.SKIPPED);
        assertThat(status.isSuccess()).isFalse();
        assertThat(status.isFailure()).isFalse();
        assertThat(status.isSkipped()).isTrue();
    }

    @Test
    @DisplayName("Should create skipped status with throwable")
    public void shouldCreateSkippedStatusWithThrowable() {
        Throwable throwable = new RuntimeException("Skip reason");
        TestDescriptorStatus status = TestDescriptorStatus.skipped(throwable);

        assertThat(status.getType()).isEqualTo(Type.SKIPPED);
        assertThat(status.getThrowable()).isEqualTo(throwable);
        assertThat(status.isSkipped()).isTrue();
        assertThat(status.isSkippedWithThrowable()).isTrue();
    }

    @Test
    @DisplayName("Should create failed status")
    public void shouldCreateFailedStatus() {
        Throwable throwable = new RuntimeException("Test failed");
        TestDescriptorStatus status = TestDescriptorStatus.failed(throwable);

        assertThat(status.getType()).isEqualTo(Type.FAILED);
        assertThat(status.getThrowable()).isEqualTo(throwable);
        assertThat(status.isSuccess()).isFalse();
        assertThat(status.isFailure()).isTrue();
        assertThat(status.isSkipped()).isFalse();
    }

    @Test
    @DisplayName("Should return null throwable for passed status")
    public void shouldReturnNullThrowableForPassedStatus() {
        TestDescriptorStatus status = TestDescriptorStatus.passed();

        assertThat(status.getThrowable()).isNull();
    }

    @Test
    @DisplayName("Should return null throwable for skipped status without throwable")
    public void shouldReturnNullThrowableForSkippedStatusWithoutThrowable() {
        TestDescriptorStatus status = TestDescriptorStatus.skipped();

        assertThat(status.getThrowable()).isNull();
        assertThat(status.isSkippedWithThrowable()).isFalse();
    }

    @Test
    @DisplayName("Should implement toString")
    public void shouldImplementToString() {
        TestDescriptorStatus status = TestDescriptorStatus.passed();

        String result = status.toString();

        assertThat(result).contains("InvocationResult");
        assertThat(result).contains("PASSED");
    }

    @Test
    @DisplayName("Should implement equals for same object")
    public void shouldImplementEqualsForSameObject() {
        TestDescriptorStatus status = TestDescriptorStatus.passed();

        assertThat(status).isEqualTo(status);
    }

    @Test
    @DisplayName("Should implement equals for equal passed statuses")
    public void shouldImplementEqualsForEqualPassedStatuses() {
        TestDescriptorStatus status1 = TestDescriptorStatus.passed();
        TestDescriptorStatus status2 = TestDescriptorStatus.passed();

        assertThat(status1).isEqualTo(status2);
    }

    @Test
    @DisplayName("Should implement equals for equal skipped statuses")
    public void shouldImplementEqualsForEqualSkippedStatuses() {
        TestDescriptorStatus status1 = TestDescriptorStatus.skipped();
        TestDescriptorStatus status2 = TestDescriptorStatus.skipped();

        assertThat(status1).isEqualTo(status2);
    }

    @Test
    @DisplayName("Should not equal passed and failed statuses")
    public void shouldNotEqualPassedAndFailedStatuses() {
        TestDescriptorStatus passed = TestDescriptorStatus.passed();
        TestDescriptorStatus failed = TestDescriptorStatus.failed(new RuntimeException());

        assertThat(passed).isNotEqualTo(failed);
    }

    @Test
    @DisplayName("Should not equal passed and skipped statuses")
    public void shouldNotEqualPassedAndSkippedStatuses() {
        TestDescriptorStatus passed = TestDescriptorStatus.passed();
        TestDescriptorStatus skipped = TestDescriptorStatus.skipped();

        assertThat(passed).isNotEqualTo(skipped);
    }

    @Test
    @DisplayName("Should not equal null")
    public void shouldNotEqualNull() {
        TestDescriptorStatus status = TestDescriptorStatus.passed();

        assertThat(status).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should not equal different class")
    public void shouldNotEqualDifferentClass() {
        TestDescriptorStatus status = TestDescriptorStatus.passed();

        assertThat(status).isNotEqualTo("not a status");
    }

    @Test
    @DisplayName("Should implement hashCode consistently")
    public void shouldImplementHashCodeConsistently() {
        TestDescriptorStatus status1 = TestDescriptorStatus.passed();
        TestDescriptorStatus status2 = TestDescriptorStatus.passed();

        assertThat(status1.hashCode()).isEqualTo(status2.hashCode());
    }

    @Test
    @DisplayName("Should have different hashCodes for different types")
    public void shouldHaveDifferentHashCodesForDifferentTypes() {
        TestDescriptorStatus passed = TestDescriptorStatus.passed();
        TestDescriptorStatus skipped = TestDescriptorStatus.skipped();

        assertThat(passed.hashCode()).isNotEqualTo(skipped.hashCode());
    }

    @Test
    @DisplayName("Should have enum Type values")
    public void shouldHaveEnumTypeValues() {
        assertThat(Type.PASSED).isNotNull();
        assertThat(Type.FAILED).isNotNull();
        assertThat(Type.SKIPPED).isNotNull();

        Type[] values = Type.values();
        assertThat(values).hasSize(3);
    }
}
