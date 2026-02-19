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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.verifyica.api.Execution.skipIfCondition;
import static org.verifyica.api.Execution.skipIfNotCondition;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Execution Tests")
public class ExecutionTest {

    @Test
    @DisplayName("Should skip execution based on condition")
    public void testSkipIfCondition() {
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class).isThrownBy(() -> skipIfCondition(true));
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfCondition(true, "skipped"));
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfCondition(() -> true));
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfCondition(() -> true, "skipped"));

        skipIfCondition(false);
        skipIfCondition(false, "skipped");
        skipIfCondition(() -> false);
        skipIfCondition(() -> false, "skipped");
    }

    @Test
    @DisplayName("Should skip execution based on negated condition")
    public void testSkipIfNotCondition() {
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfNotCondition(false));
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfNotCondition(false, "skipped"));
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfNotCondition(() -> false));
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfNotCondition(() -> false, "skipped"));

        skipIfNotCondition(true);
        skipIfNotCondition(true, "skipped");
        skipIfNotCondition(() -> true);
        skipIfNotCondition(() -> true, "skipped");
    }

    @Test
    @DisplayName("Should validate arguments and throw IllegalArgumentException")
    public void testIllegalArguments() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> skipIfCondition(null));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> skipIfCondition(null, "skipped"));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> skipIfNotCondition(null));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> skipIfNotCondition(null, "skipped"));
    }

    @Test
    @DisplayName("Should skip with custom message")
    public void testSkipIfConditionWithMessage() {
        String customMessage = "Custom skip message";

        assertThatThrownBy(() -> skipIfCondition(true, customMessage))
                .isInstanceOf(Execution.ExecutionSkippedException.class)
                .hasMessage(customMessage);
    }

    @Test
    @DisplayName("Should skip with negated condition and custom message")
    public void testSkipIfNotConditionWithMessage() {
        String customMessage = "Custom skip message";

        assertThatThrownBy(() -> skipIfNotCondition(false, customMessage))
                .isInstanceOf(Execution.ExecutionSkippedException.class)
                .hasMessage(customMessage);
    }

    @Test
    @DisplayName("Should handle empty message for skipIfCondition")
    public void testSkipIfConditionWithEmptyMessage() {
        // Empty message should result in exception with null message
        assertThatThrownBy(() -> skipIfCondition(true, ""))
                .isInstanceOf(Execution.ExecutionSkippedException.class)
                .hasMessage(null);
    }

    @Test
    @DisplayName("Should handle blank message for skipIfCondition")
    public void testSkipIfConditionWithBlankMessage() {
        // Blank message should result in exception with null message
        assertThatThrownBy(() -> skipIfCondition(true, "   "))
                .isInstanceOf(Execution.ExecutionSkippedException.class)
                .hasMessage(null);
    }

    @Test
    @DisplayName("Should handle empty message for skipIfNotCondition")
    public void testSkipIfNotConditionWithEmptyMessage() {
        assertThatThrownBy(() -> skipIfNotCondition(false, ""))
                .isInstanceOf(Execution.ExecutionSkippedException.class)
                .hasMessage(null);
    }

    @Test
    @DisplayName("Should handle blank message for skipIfNotCondition")
    public void testSkipIfNotConditionWithBlankMessage() {
        assertThatThrownBy(() -> skipIfNotCondition(false, "   "))
                .isInstanceOf(Execution.ExecutionSkippedException.class)
                .hasMessage(null);
    }

    @Test
    @DisplayName("Should create ExecutionSkippedException with various constructors")
    public void testExecutionSkippedExceptionConstructors() {
        // Default constructor
        Execution.ExecutionSkippedException exception1 = new Execution.ExecutionSkippedException();
        assertThat(exception1.getMessage()).isNull();

        // Message constructor
        String message = "Test message";
        Execution.ExecutionSkippedException exception2 = new Execution.ExecutionSkippedException(message);
        assertThat(exception2.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should throw exception when supplier is null for skipIfCondition")
    public void testSkipIfConditionSupplierNullThrows() {
        assertThatThrownBy(() -> skipIfCondition(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("supplier is null");
    }

    @Test
    @DisplayName("Should throw exception when supplier is null for skipIfNotCondition")
    public void testSkipIfNotConditionSupplierNullThrows() {
        assertThatThrownBy(() -> skipIfNotCondition(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("supplier is null");
    }

    @Test
    @DisplayName("Should throw exception when supplier with message is null for skipIfCondition")
    public void testSkipIfConditionSupplierWithMessageNullThrows() {
        assertThatThrownBy(() -> skipIfCondition(null, "message"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("supplier is null");
    }

    @Test
    @DisplayName("Should throw exception when supplier with message is null for skipIfNotCondition")
    public void testSkipIfNotConditionSupplierWithMessageNullThrows() {
        assertThatThrownBy(() -> skipIfNotCondition(null, "message"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("supplier is null");
    }

    @Test
    @DisplayName("Should evaluate skipIfCondition supplier lazily")
    public void testSkipIfConditionSupplierEvaluatesLazily() {
        AtomicBoolean supplierCalled = new AtomicBoolean(false);
        BooleanSupplier supplier = () -> {
            supplierCalled.set(true);
            return false; // Don't skip
        };

        // When condition is false, supplier should be called
        skipIfCondition(supplier);
        assertThat(supplierCalled).isTrue();

        // Reset
        supplierCalled.set(false);

        // When condition is true, supplier should be called
        BooleanSupplier skippingSupplier = () -> {
            supplierCalled.set(true);
            return true; // Will skip
        };

        assertThatThrownBy(() -> skipIfCondition(skippingSupplier))
                .isInstanceOf(Execution.ExecutionSkippedException.class);
        assertThat(supplierCalled).isTrue();
    }

    @Test
    @DisplayName("Should evaluate skipIfNotCondition supplier lazily")
    public void testSkipIfNotConditionSupplierEvaluatesLazily() {
        AtomicBoolean supplierCalled = new AtomicBoolean(false);
        BooleanSupplier supplier = () -> {
            supplierCalled.set(true);
            return true; // Don't skip
        };

        // When condition is true, supplier should be called
        skipIfNotCondition(supplier);
        assertThat(supplierCalled).isTrue();

        // Reset
        supplierCalled.set(false);

        // When condition is false, supplier should be called
        BooleanSupplier skippingSupplier = () -> {
            supplierCalled.set(true);
            return false; // Will skip
        };

        assertThatThrownBy(() -> skipIfNotCondition(skippingSupplier))
                .isInstanceOf(Execution.ExecutionSkippedException.class);
        assertThat(supplierCalled).isTrue();
    }
}
