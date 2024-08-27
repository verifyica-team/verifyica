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

import java.util.UUID;
import org.junit.jupiter.api.Test;

public class AnsiColorStringTest {

    @Test
    public void testEmptyBuilder() {
        AnsiColor.StringBuilder ansiColorStringBuilder =
                new AnsiColor.StringBuilder().append(AnsiColor.NONE);

        String string1 = ansiColorStringBuilder.toString();
        String string2 = ansiColorStringBuilder.build();

        assertThat(string1).isNotNull();
        assertThat(string2).isNotNull();
        assertThat(string1).isEqualTo(string2);
        assertThat(string1.length()).isEqualTo(0);
        assertThat(string1.toCharArray()).hasSize(0);
    }

    @Test
    public void testNoColorBeforeAppend() {
        String displayString = UUID.randomUUID().toString();

        AnsiColor.StringBuilder ansiColorStringBuilder =
                new AnsiColor.StringBuilder().append(AnsiColor.NONE).append(displayString);

        String string = ansiColorStringBuilder.build();

        assertThat(string).isNotNull();
        assertThat(string.length()).isEqualTo(displayString.length());
        assertThat(string.toCharArray()).hasSize(displayString.length());
    }

    @Test
    public void testNoColorAfterAppend() {
        String displayString = UUID.randomUUID().toString();

        AnsiColor.StringBuilder ansiColorStringBuilder =
                new AnsiColor.StringBuilder().append(displayString).append(AnsiColor.NONE);

        String string = ansiColorStringBuilder.build();

        assertThat(string).isNotNull();
        assertThat(string.length()).isEqualTo(displayString.length());
        assertThat(string.toCharArray()).hasSize(displayString.length());
    }

    @Test
    public void testReuseColor() {
        String displayString = UUID.randomUUID().toString();

        AnsiColor.StringBuilder ansiColorStringBuilder =
                new AnsiColor.StringBuilder(AnsiColor.TEXT_RED)
                        .append(displayString)
                        .append(AnsiColor.TEXT_RED);

        String string = ansiColorStringBuilder.build();

        assertThat(string).isNotNull();
        assertThat(string.length()).isEqualTo(displayString.length());
        assertThat(string.toCharArray()).hasSize(displayString.length());

        assertThat(string).isEqualTo(AnsiColor.TEXT_RED + displayString);
    }

    @Test
    public void testAppendColor() {
        String displayString = UUID.randomUUID().toString();

        AnsiColor.StringBuilder ansiColorStringBuilder =
                new AnsiColor.StringBuilder().append(displayString).append(AnsiColor.TEXT_RED);

        String string = ansiColorStringBuilder.build();

        assertThat(string).isNotNull();

        int expected = displayString.length() + AnsiColor.TEXT_RED.toString().length();

        assertThat(string.length()).isEqualTo(expected);
        assertThat(string.toCharArray()).hasSize(expected);
    }

    @Test
    public void testBuildAndToString() {
        String displayString = UUID.randomUUID().toString();

        AnsiColor.StringBuilder ansiColorStringBuilder1 =
                new AnsiColor.StringBuilder()
                        .append(AnsiColor.TEXT_RED)
                        .append(displayString)
                        .append(AnsiColor.NONE);

        String string1 = ansiColorStringBuilder1.build();

        assertThat(string1).isNotNull();
        assertThat(string1.length()).isGreaterThan(0);
        assertThat(string1.toCharArray()).hasSizeGreaterThan(0);

        assertThat(string1).isEqualTo(AnsiColor.TEXT_RED + displayString + AnsiColor.NONE);

        AnsiColor.StringBuilder ansiColorStringBuilder2 =
                new AnsiColor.StringBuilder(AnsiColor.TEXT_RED)
                        .append(displayString)
                        .append(AnsiColor.NONE);

        String string2 = ansiColorStringBuilder2.toString();

        assertThat(string2).isNotNull();
        assertThat(string2.length()).isGreaterThan(0);
        assertThat(string2.toCharArray()).hasSizeGreaterThan(0);

        assertThat(string2).isEqualTo(string1);
    }

    @Test
    public void testBuilderConstructorWithColor() {
        String displayString = UUID.randomUUID().toString();

        AnsiColor.StringBuilder ansiColorStringBuilder1 =
                new AnsiColor.StringBuilder()
                        .append(AnsiColor.TEXT_RED)
                        .append(displayString)
                        .append(AnsiColor.NONE);

        String string1 = ansiColorStringBuilder1.build();

        assertThat(string1).isNotNull();
        assertThat(string1.length()).isGreaterThan(0);
        assertThat(string1.toCharArray()).hasSizeGreaterThan(0);

        assertThat(string1).isEqualTo(AnsiColor.TEXT_RED + displayString + AnsiColor.NONE);

        AnsiColor.StringBuilder ansiColorStringBuilder2 =
                new AnsiColor.StringBuilder(AnsiColor.TEXT_RED)
                        .append(displayString)
                        .append(AnsiColor.NONE);

        String string2 = ansiColorStringBuilder2.build();

        assertThat(string2).isNotNull();
        assertThat(string2.length()).isGreaterThan(0);
        assertThat(string2.toCharArray()).hasSizeGreaterThan(0);

        assertThat(string2).isEqualTo(string1);
    }

    @Test
    public void testDuplicateLastColor() {
        String displayString = UUID.randomUUID().toString();

        AnsiColor.StringBuilder ansiColorStringBuilder1 =
                new AnsiColor.StringBuilder()
                        .append(AnsiColor.TEXT_RED)
                        .append(displayString)
                        .append(AnsiColor.NONE);

        String string1 = ansiColorStringBuilder1.build();

        assertThat(string1).isNotNull();
        assertThat(string1.length()).isGreaterThan(0);
        assertThat(string1.toCharArray()).hasSizeGreaterThan(0);

        assertThat(string1).isEqualTo(AnsiColor.TEXT_RED + displayString + AnsiColor.NONE);

        AnsiColor.StringBuilder ansiColorStringBuilder2 =
                new AnsiColor.StringBuilder()
                        .append(AnsiColor.TEXT_RED)
                        .append(displayString)
                        .append(AnsiColor.NONE)
                        .append(AnsiColor.NONE);

        String string2 = ansiColorStringBuilder2.build();

        assertThat(string2).isNotNull();
        assertThat(string2.length()).isGreaterThan(0);
        assertThat(string2.toCharArray()).hasSizeGreaterThan(0);

        assertThat(string2).isEqualTo(string1);
    }
}
