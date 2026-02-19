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

package org.verifyica.engine.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AnsiColoredString Tests")
public class AnsiColoredStringTest {

    @Test
    @DisplayName("Should create empty builder")
    public void testEmptyBuilder() {
        AnsiColoredString ansiColoredString = new AnsiColoredString().append(AnsiColor.NONE);

        String string1 = ansiColoredString.toString();
        String string2 = ansiColoredString.build();

        assertThat(string1).isNotNull();
        assertThat(string2).isNotNull();
        assertThat(string1).isEqualTo(string2);
        assertThat(string1.length()).isEqualTo(0);
        assertThat(string1.toCharArray()).hasSize(0);
    }

    @Test
    @DisplayName("Should handle no color before append")
    public void testNoColorBeforeAppend() {
        String displayString = UUID.randomUUID().toString();

        AnsiColoredString ansiColoredString =
                new AnsiColoredString().append(AnsiColor.NONE).append(displayString);

        String string = ansiColoredString.build();

        assertThat(string).isNotNull();
        assertThat(string.length()).isEqualTo(displayString.length());
        assertThat(string.toCharArray()).hasSize(displayString.length());
    }

    @Test
    @DisplayName("Should handle no color after append")
    public void testNoColorAfterAppend() {
        String displayString = UUID.randomUUID().toString();

        AnsiColoredString ansiColoredString =
                new AnsiColoredString().append(displayString).append(AnsiColor.NONE);

        String string = ansiColoredString.build();
        int expectedLength = displayString.length() + AnsiColor.NONE.toString().length();

        assertThat(string).isNotNull();
        assertThat(string.length()).isEqualTo(expectedLength);
        assertThat(string.toCharArray()).hasSize(expectedLength);
    }

    @Test
    @DisplayName("Should reuse color")
    public void testReuseColor() {
        String displayString = UUID.randomUUID().toString();

        AnsiColoredString ansiColoredString =
                new AnsiColoredString(AnsiColor.TEXT_RED).append(displayString).append(AnsiColor.TEXT_RED);

        String string = ansiColoredString.build();
        int expectedLength = AnsiColor.TEXT_RED.toString().length() + displayString.length();

        assertThat(string).isNotNull();
        assertThat(string.length()).isEqualTo(expectedLength);
        assertThat(string.toCharArray()).hasSize(expectedLength);

        assertThat(string).isEqualTo(AnsiColor.TEXT_RED + displayString);
    }

    @Test
    @DisplayName("Should append color")
    public void testAppendColor() {
        String displayString = UUID.randomUUID().toString();

        AnsiColoredString ansiColoredString =
                new AnsiColoredString().append(displayString).append(AnsiColor.TEXT_RED);

        String string = ansiColoredString.build();

        assertThat(string).isNotNull();

        int expected = displayString.length() + AnsiColor.TEXT_RED.toString().length();

        assertThat(string.length()).isEqualTo(expected);
        assertThat(string.toCharArray()).hasSize(expected);
    }

    @Test
    @DisplayName("Should build and convert to string")
    public void testBuildAndToString() {
        String displayString = UUID.randomUUID().toString();

        AnsiColoredString ansiColorString1 = new AnsiColoredString()
                .append(AnsiColor.TEXT_RED)
                .append(displayString)
                .append(AnsiColor.NONE);

        String string1 = ansiColorString1.build();

        assertThat(string1).isNotNull();
        assertThat(string1.length()).isGreaterThan(0);
        assertThat(string1.toCharArray()).hasSizeGreaterThan(0);

        assertThat(string1).isEqualTo(AnsiColor.TEXT_RED + displayString + AnsiColor.NONE);

        AnsiColoredString ansiColorString2 =
                new AnsiColoredString(AnsiColor.TEXT_RED).append(displayString).append(AnsiColor.NONE);

        String string2 = ansiColorString2.toString();

        assertThat(string2).isNotNull();
        assertThat(string2.length()).isGreaterThan(0);
        assertThat(string2.toCharArray()).hasSizeGreaterThan(0);

        assertThat(string2).isEqualTo(string1);
    }

    @Test
    @DisplayName("Should use builder constructor with color")
    public void testBuilderConstructorWithColor() {
        String displayString = UUID.randomUUID().toString();

        AnsiColoredString ansiColorString1 = new AnsiColoredString()
                .append(AnsiColor.TEXT_RED)
                .append(displayString)
                .append(AnsiColor.NONE);

        String string1 = ansiColorString1.build();

        assertThat(string1).isNotNull();
        assertThat(string1.length()).isGreaterThan(0);
        assertThat(string1.toCharArray()).hasSizeGreaterThan(0);

        assertThat(string1).isEqualTo(AnsiColor.TEXT_RED + displayString + AnsiColor.NONE);

        AnsiColoredString ansiColorString2 =
                new AnsiColoredString(AnsiColor.TEXT_RED).append(displayString).append(AnsiColor.NONE);

        String string2 = ansiColorString2.build();

        assertThat(string2).isNotNull();
        assertThat(string2.length()).isGreaterThan(0);
        assertThat(string2.toCharArray()).hasSizeGreaterThan(0);

        assertThat(string2).isEqualTo(string1);
    }

    @Test
    @DisplayName("Should handle duplicate last color")
    public void testDuplicateLastColor() {
        String displayString = UUID.randomUUID().toString();

        AnsiColoredString ansiColorString1 = new AnsiColoredString()
                .append(AnsiColor.TEXT_RED)
                .append(displayString)
                .append(AnsiColor.NONE);

        String string1 = ansiColorString1.build();

        assertThat(string1).isNotNull();
        assertThat(string1.length()).isGreaterThan(0);
        assertThat(string1.toCharArray()).hasSizeGreaterThan(0);

        assertThat(string1).isEqualTo(AnsiColor.TEXT_RED + displayString + AnsiColor.NONE);

        AnsiColoredString ansiColorString2 = new AnsiColoredString()
                .append(AnsiColor.TEXT_RED)
                .append(displayString)
                .append(AnsiColor.NONE)
                .append(AnsiColor.NONE);

        String string2 = ansiColorString2.build();

        assertThat(string2).isNotNull();
        assertThat(string2.length()).isGreaterThan(0);
        assertThat(string2.toCharArray()).hasSizeGreaterThan(0);

        assertThat(string2).isEqualTo(string1);
    }

    /*
    @Test
    public  void debug() {
        boolean resetAnsiColor = false;

        try {
            if (!AnsiColor.isSupported()) {
                AnsiColor.setSupported(true);
                resetAnsiColor = true;
            }

            AnsiColoredString baseString =
                    new AnsiColoredString()
                            .append(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append("White String")
                            .append(AnsiColor.NONE);

            System.out.println(baseString);

            AnsiColoredString string1 =
                    new AnsiColoredString()
                            .append(AnsiColor.TEXT_YELLOW_BOLD_BRIGHT)
                            .append("Yellow String")
                            .append(AnsiColor.NONE);

            System.out.println(string1);

            AnsiColoredString string2 =
                    new AnsiColoredString()
                            .append(AnsiColor.TEXT_RED_BOLD_BRIGHT)
                            .append("Red String")
                            .append(AnsiColor.NONE);

            System.out.println(string2);

            AnsiColoredString string3 =
                    new AnsiColoredString()
                            .append(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append("Another White String")
                            .append(AnsiColor.NONE);

            System.out.println(string3);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append(baseString)
                    .append(" ")
                    .append(string1)
                    .append(" ")
                    .append(string2)
                    .append(" ")
                    .append(string3);

            System.out.println(stringBuilder);

            AnsiColoredString ansiColoredString = new AnsiColoredString();
            ansiColoredString
                    .append(baseString)
                    .append(" ")
                    .append(string1)
                    .append(" ")
                    .append(string2)
                    .append(" ")
                    .append(string3);

            System.out.println(ansiColoredString);
        } finally {
            if (resetAnsiColor) {
                AnsiColor.setSupported(false);
            }
        }
    }
    */
}
