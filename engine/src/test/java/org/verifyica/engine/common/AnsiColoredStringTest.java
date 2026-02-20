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

    @Test
    @DisplayName("Should append boolean value")
    public void shouldAppendBooleanValue() {
        AnsiColoredString ansiColoredString =
                new AnsiColoredString().append(true).append(false);

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo("truefalse");
    }

    @Test
    @DisplayName("Should append short value")
    public void shouldAppendShortValue() {
        AnsiColoredString ansiColoredString = new AnsiColoredString().append((short) 42);

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo("42");
    }

    @Test
    @DisplayName("Should append character")
    public void shouldAppendCharacter() {
        AnsiColoredString ansiColoredString =
                new AnsiColoredString().append('A').append('B');

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo("AB");
    }

    @Test
    @DisplayName("Should append character array")
    public void shouldAppendCharacterArray() {
        char[] chars = {'H', 'e', 'l', 'l', 'o'};

        AnsiColoredString ansiColoredString = new AnsiColoredString().append(chars);

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo("Hello");
    }

    @Test
    @DisplayName("Should append character array with offset and length")
    public void shouldAppendCharacterArrayWithOffsetAndLength() {
        char[] chars = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};

        AnsiColoredString ansiColoredString = new AnsiColoredString().append(chars, 6, 5);

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo("World");
    }

    @Test
    @DisplayName("Should append integer value")
    public void shouldAppendIntegerValue() {
        AnsiColoredString ansiColoredString =
                new AnsiColoredString().append(123).append(-456);

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo("123-456");
    }

    @Test
    @DisplayName("Should append long value")
    public void shouldAppendLongValue() {
        AnsiColoredString ansiColoredString = new AnsiColoredString().append(123456789L);

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo("123456789");
    }

    @Test
    @DisplayName("Should append float value")
    public void shouldAppendFloatValue() {
        AnsiColoredString ansiColoredString = new AnsiColoredString().append(3.14f);

        String result = ansiColoredString.build();

        assertThat(result).startsWith("3.14");
    }

    @Test
    @DisplayName("Should append double value")
    public void shouldAppendDoubleValue() {
        AnsiColoredString ansiColoredString = new AnsiColoredString().append(2.71828);

        String result = ansiColoredString.build();

        assertThat(result).startsWith("2.71828");
    }

    @Test
    @DisplayName("Should append object")
    public void shouldAppendObject() {
        Object obj = new Object() {
            @Override
            public String toString() {
                return "customObject";
            }
        };

        AnsiColoredString ansiColoredString = new AnsiColoredString().append(obj);

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo("customObject");
    }

    @Test
    @DisplayName("Should append null object")
    public void shouldAppendNullObject() {
        AnsiColoredString ansiColoredString = new AnsiColoredString().append((Object) null);

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo("null");
    }

    @Test
    @DisplayName("Should append StringBuffer")
    public void shouldAppendStringBuffer() {
        StringBuffer sb = new StringBuffer("bufferContent");

        AnsiColoredString ansiColoredString = new AnsiColoredString().append(sb);

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo("bufferContent");
    }

    @Test
    @DisplayName("Should append CharSequence")
    public void shouldAppendCharSequence() {
        CharSequence cs = "charSequence";

        AnsiColoredString ansiColoredString = new AnsiColoredString().append(cs);

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo("charSequence");
    }

    @Test
    @DisplayName("Should create with string constructor")
    public void shouldCreateWithStringConstructor() {
        String initial = "initialString";

        AnsiColoredString ansiColoredString = new AnsiColoredString(initial);

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo("initialString");
    }

    @Test
    @DisplayName("Should create with null color constructor")
    public void shouldCreateWithNullColorConstructor() {
        AnsiColoredString ansiColoredString = new AnsiColoredString((AnsiColor) null);

        String result = ansiColoredString.build();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return correct length")
    public void shouldReturnCorrectLength() {
        AnsiColoredString ansiColoredString =
                new AnsiColoredString().append("Hello").append("World");

        assertThat(ansiColoredString.length()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return zero length for empty builder")
    public void shouldReturnZeroLengthForEmptyBuilder() {
        AnsiColoredString ansiColoredString = new AnsiColoredString();

        assertThat(ansiColoredString.length()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should check ANSI color support")
    public void shouldCheckAnsiColorSupport() {
        AnsiColoredString ansiColoredString = new AnsiColoredString();

        boolean supported = ansiColoredString.isAnsiColorSupported();

        assertThat(supported).isEqualTo(AnsiColor.isSupported());
    }

    @Test
    @DisplayName("Should skip appending NONE color when builder is empty")
    public void shouldSkipAppendingNoneColorWhenBuilderIsEmpty() {
        AnsiColoredString ansiColoredString = new AnsiColoredString().append(AnsiColor.NONE);

        String result = ansiColoredString.build();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should skip appending null color")
    public void shouldSkipAppendingNullColor() {
        AnsiColoredString ansiColoredString = new AnsiColoredString().append((AnsiColor) null);

        String result = ansiColoredString.build();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should append NONE color when builder is not empty")
    public void shouldAppendNoneColorWhenBuilderIsNotEmpty() {
        AnsiColoredString ansiColoredString =
                new AnsiColoredString().append("text").append(AnsiColor.NONE);

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo("text" + AnsiColor.NONE.toString());
    }

    @Test
    @DisplayName("Should be equal to itself")
    public void shouldBeEqualToItself() {
        AnsiColoredString ansiColoredString = new AnsiColoredString().append("test");

        assertThat(ansiColoredString).isEqualTo(ansiColoredString);
    }

    @Test
    @DisplayName("Should be equal to equivalent builder")
    public void shouldBeEqualToEquivalentBuilder() {
        AnsiColoredString string1 = new AnsiColoredString().append("test").append(AnsiColor.TEXT_RED);
        AnsiColoredString string2 = new AnsiColoredString().append("test").append(AnsiColor.TEXT_RED);

        assertThat(string1).isEqualTo(string2);
        assertThat(string2).isEqualTo(string1);
    }

    @Test
    @DisplayName("Should not be equal to null")
    public void shouldNotBeEqualToNull() {
        AnsiColoredString ansiColoredString = new AnsiColoredString().append("test");

        assertThat(ansiColoredString).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should not be equal to different type")
    public void shouldNotBeEqualToDifferentType() {
        AnsiColoredString ansiColoredString = new AnsiColoredString().append("test");

        assertThat(ansiColoredString).isNotEqualTo("test");
    }

    @Test
    @DisplayName("Should not be equal when content differs")
    public void shouldNotBeEqualWhenContentDiffers() {
        AnsiColoredString string1 = new AnsiColoredString().append("test1");
        AnsiColoredString string2 = new AnsiColoredString().append("test2");

        assertThat(string1).isNotEqualTo(string2);
    }

    @Test
    @DisplayName("Should not be equal when last color differs")
    public void shouldNotBeEqualWhenLastColorDiffers() {
        AnsiColoredString string1 = new AnsiColoredString().append("test").append(AnsiColor.TEXT_RED);
        AnsiColoredString string2 = new AnsiColoredString().append("test").append(AnsiColor.TEXT_GREEN);

        assertThat(string1).isNotEqualTo(string2);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    public void shouldHaveConsistentHashCode() {
        AnsiColoredString ansiColoredString = new AnsiColoredString().append("test");

        int hash1 = ansiColoredString.hashCode();
        int hash2 = ansiColoredString.hashCode();

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("Should have same hashCode for equal builders")
    public void shouldHaveSameHashCodeForEqualBuilders() {
        AnsiColoredString string1 = new AnsiColoredString().append("test").append(AnsiColor.TEXT_RED);
        AnsiColoredString string2 = new AnsiColoredString().append("test").append(AnsiColor.TEXT_RED);

        assertThat(string1.hashCode()).isEqualTo(string2.hashCode());
    }

    @Test
    @DisplayName("Should handle chained appends")
    public void shouldHandleChainedAppends() {
        AnsiColoredString ansiColoredString = new AnsiColoredString()
                .append(AnsiColor.TEXT_RED)
                .append("Hello")
                .append(' ')
                .append(123)
                .append(AnsiColor.NONE);

        String result = ansiColoredString.build();

        assertThat(result).isEqualTo(AnsiColor.TEXT_RED + "Hello 123" + AnsiColor.NONE);
    }

    @Test
    @DisplayName("Should handle empty string constructor")
    public void shouldHandleEmptyStringConstructor() {
        AnsiColoredString ansiColoredString = new AnsiColoredString("");

        String result = ansiColoredString.build();

        assertThat(result).isEmpty();
        assertThat(ansiColoredString.length()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle multiple color changes")
    public void shouldHandleMultipleColorChanges() {
        AnsiColoredString ansiColoredString = new AnsiColoredString()
                .append(AnsiColor.TEXT_RED)
                .append("Red")
                .append(AnsiColor.TEXT_GREEN)
                .append("Green")
                .append(AnsiColor.TEXT_BLUE)
                .append("Blue");

        String result = ansiColoredString.build();

        assertThat(result)
                .isEqualTo(AnsiColor.TEXT_RED + "Red" + AnsiColor.TEXT_GREEN + "Green" + AnsiColor.TEXT_BLUE + "Blue");
    }

    @Test
    @DisplayName("Should optimize duplicate consecutive colors")
    public void shouldOptimizeDuplicateConsecutiveColors() {
        boolean originalSupport = AnsiColor.isSupported();
        try {
            AnsiColor.setSupported(true);

            AnsiColoredString ansiColoredString = new AnsiColoredString()
                    .append(AnsiColor.TEXT_RED)
                    .append("A")
                    .append(AnsiColor.TEXT_RED)
                    .append(AnsiColor.TEXT_RED)
                    .append("B");

            String result = ansiColoredString.build();
            String redSequence = AnsiColor.TEXT_RED.toString();

            // Count occurrences of the red color sequence manually
            int count = 0;
            int index = 0;
            while ((index = result.indexOf(redSequence, index)) != -1) {
                count++;
                index += redSequence.length();
            }

            // Only one TEXT_RED should be present
            assertThat(count).isEqualTo(1);
        } finally {
            AnsiColor.setSupported(originalSupport);
        }
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
