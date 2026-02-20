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

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.*;

@DisplayName("AnsiColor Tests")
public class AnsiColorTest {

    private boolean originalSupport;

    @BeforeEach
    public void setUp() {
        originalSupport = AnsiColor.isSupported();
    }

    @AfterEach
    public void tearDown() {
        AnsiColor.setSupported(originalSupport);
    }

    @Nested
    @DisplayName("Color Constants Tests")
    public class ColorConstantsTests {

        @Test
        @DisplayName("Should have NONE constant")
        public void shouldHaveNoneConstant() {
            assertThat(AnsiColor.NONE).isNotNull();
        }

        @Test
        @DisplayName("Should have regular color constants")
        public void shouldHaveRegularColorConstants() {
            assertThat(AnsiColor.TEXT_BLACK).isNotNull();
            assertThat(AnsiColor.TEXT_RED).isNotNull();
            assertThat(AnsiColor.TEXT_GREEN).isNotNull();
            assertThat(AnsiColor.TEXT_YELLOW).isNotNull();
            assertThat(AnsiColor.TEXT_BLUE).isNotNull();
            assertThat(AnsiColor.TEXT_PURPLE).isNotNull();
            assertThat(AnsiColor.TEXT_CYAN).isNotNull();
            assertThat(AnsiColor.TEXT_WHITE).isNotNull();
        }

        @Test
        @DisplayName("Should have bold color constants")
        public void shouldHaveBoldColorConstants() {
            assertThat(AnsiColor.TEXT_BLACK_BOLD).isNotNull();
            assertThat(AnsiColor.TEXT_RED_BOLD).isNotNull();
            assertThat(AnsiColor.TEXT_GREEN_BOLD).isNotNull();
            assertThat(AnsiColor.TEXT_YELLOW_BOLD).isNotNull();
            assertThat(AnsiColor.TEXT_BLUE_BOLD).isNotNull();
            assertThat(AnsiColor.TEXT_PURPLE_BOLD).isNotNull();
            assertThat(AnsiColor.TEXT_CYAN_BOLD).isNotNull();
            assertThat(AnsiColor.TEXT_WHITE_BOLD).isNotNull();
        }

        @Test
        @DisplayName("Should have underlined color constants")
        public void shouldHaveUnderlinedColorConstants() {
            assertThat(AnsiColor.TEXT_BLACK_UNDERLINED).isNotNull();
            assertThat(AnsiColor.TEXT_RED_UNDERLINED).isNotNull();
            assertThat(AnsiColor.TEXT_GREEN_UNDERLINED).isNotNull();
            assertThat(AnsiColor.TEXT_YELLOW_UNDERLINED).isNotNull();
            assertThat(AnsiColor.TEXT_BLUE_UNDERLINED).isNotNull();
            assertThat(AnsiColor.TEXT_PURPLE_UNDERLINED).isNotNull();
            assertThat(AnsiColor.TEXT_CYAN_UNDERLINED).isNotNull();
            assertThat(AnsiColor.TEXT_WHITE_UNDERLINED).isNotNull();
        }

        @Test
        @DisplayName("Should have bright color constants")
        public void shouldHaveBrightColorConstants() {
            assertThat(AnsiColor.TEXT_BLACK_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_RED_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_GREEN_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_YELLOW_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_BLUE_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_PURPLE_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_CYAN_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_WHITE_BRIGHT).isNotNull();
        }

        @Test
        @DisplayName("Should have bold bright color constants")
        public void shouldHaveBoldBrightColorConstants() {
            assertThat(AnsiColor.TEXT_BLACK_BOLD_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_RED_BOLD_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_GREEN_BOLD_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_YELLOW_BOLD_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_BLUE_BOLD_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_PURPLE_BOLD_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_CYAN_BOLD_BRIGHT).isNotNull();
            assertThat(AnsiColor.TEXT_WHITE_BOLD_BRIGHT).isNotNull();
        }

        @Test
        @DisplayName("Should have background color constants")
        public void shouldHaveBackgroundColorConstants() {
            assertThat(AnsiColor.BACKGROUND_BLACK).isNotNull();
            assertThat(AnsiColor.BACKGROUND_RED).isNotNull();
            assertThat(AnsiColor.BACKGROUND_GREEN).isNotNull();
            assertThat(AnsiColor.BACKGROUND_YELLOW).isNotNull();
            assertThat(AnsiColor.BACKGROUND_BLUE).isNotNull();
            assertThat(AnsiColor.BACKGROUND_PURPLE).isNotNull();
            assertThat(AnsiColor.BACKGROUND_CYAN).isNotNull();
            assertThat(AnsiColor.BACKGROUND_WHITE).isNotNull();
        }

        @Test
        @DisplayName("Should have bright background color constants")
        public void shouldHaveBrightBackgroundColorConstants() {
            assertThat(AnsiColor.BACKGROUND_BLACK_BRIGHT).isNotNull();
            assertThat(AnsiColor.BACKGROUND_RED_BRIGHT).isNotNull();
            assertThat(AnsiColor.BACKGROUND_GREEN_BRIGHT).isNotNull();
            assertThat(AnsiColor.BACKGROUND_YELLOW_BRIGHT).isNotNull();
            assertThat(AnsiColor.BACKGROUND_BLUE_BRIGHT).isNotNull();
            assertThat(AnsiColor.BACKGROUND_PURPLE_BRIGHT).isNotNull();
            assertThat(AnsiColor.BACKGROUND_CYAN_BRIGHT).isNotNull();
            assertThat(AnsiColor.BACKGROUND_WHITE_BRIGHT).isNotNull();
        }
    }

    @Nested
    @DisplayName("Wrap Tests")
    public class WrapTests {

        @Test
        @DisplayName("Should wrap string with color when supported")
        public void shouldWrapStringWithColorWhenSupported() {
            AnsiColor.setSupported(true);

            String result = AnsiColor.TEXT_RED.wrap("test");

            assertThat(result).isNotEqualTo("test").contains("test");
        }

        @Test
        @DisplayName("Should return plain string when not supported")
        public void shouldReturnPlainStringWhenNotSupported() {
            AnsiColor.setSupported(false);

            String result = AnsiColor.TEXT_RED.wrap("test");

            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("Should wrap integer with color")
        public void shouldWrapIntegerWithColor() {
            AnsiColor.setSupported(true);

            String result = AnsiColor.TEXT_GREEN.wrap(123);

            assertThat(result).contains("123");
        }

        @Test
        @DisplayName("Should wrap null object")
        public void shouldWrapNullObject() {
            AnsiColor.setSupported(false);

            String result = AnsiColor.TEXT_BLUE.wrap(null);

            assertThat(result).isEqualTo("null");
        }

        @Test
        @DisplayName("Should wrap empty string")
        public void shouldWrapEmptyString() {
            AnsiColor.setSupported(false);

            String result = AnsiColor.TEXT_CYAN.wrap("");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should wrap string with special characters")
        public void shouldWrapStringWithSpecialCharacters() {
            AnsiColor.setSupported(false);

            String result = AnsiColor.TEXT_YELLOW.wrap("test\n\t<>&");

            assertThat(result).isEqualTo("test\n\t<>&");
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    public class ToStringTests {

        @Test
        @DisplayName("Should return escape sequence when supported")
        public void shouldReturnEscapeSequenceWhenSupported() {
            AnsiColor.setSupported(true);

            String result = AnsiColor.TEXT_RED.toString();

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should return empty string when not supported")
        public void shouldReturnEmptyStringWhenNotSupported() {
            AnsiColor.setSupported(false);

            String result = AnsiColor.TEXT_RED.toString();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return consistent value on multiple calls")
        public void shouldReturnConsistentValueOnMultipleCalls() {
            String result1 = AnsiColor.TEXT_GREEN.toString();
            String result2 = AnsiColor.TEXT_GREEN.toString();

            assertThat(result1).isEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    public class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        public void shouldBeEqualToItself() {
            assertThat(AnsiColor.TEXT_RED).isEqualTo(AnsiColor.TEXT_RED);
        }

        @Test
        @DisplayName("Should not be equal to different color")
        public void shouldNotBeEqualToDifferentColor() {
            assertThat(AnsiColor.TEXT_RED).isNotEqualTo(AnsiColor.TEXT_GREEN);
        }

        @Test
        @DisplayName("Should not be equal to null")
        public void shouldNotBeEqualToNull() {
            assertThat(AnsiColor.TEXT_RED).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        public void shouldNotBeEqualToDifferentType() {
            assertThat(AnsiColor.TEXT_RED).isNotEqualTo("red");
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        public void shouldHaveConsistentHashCode() {
            int hash1 = AnsiColor.TEXT_BLUE.hashCode();
            int hash2 = AnsiColor.TEXT_BLUE.hashCode();

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("Should have same hashCode for equal colors")
        public void shouldHaveSameHashCodeForEqualColors() {
            AnsiColor color1 = AnsiColor.ofSequence("\033[0;31m");
            AnsiColor color2 = AnsiColor.ofSequence("\033[0;31m");

            assertThat(color1.hashCode()).isEqualTo(color2.hashCode());
        }
    }

    @Nested
    @DisplayName("StripAnsiEscapeSequences Tests")
    public class StripAnsiEscapeSequencesTests {

        @Test
        @DisplayName("Should strip ANSI escape sequences from string")
        public void shouldStripAnsiEscapeSequencesFromString() {
            String input = "\033[0;31mRed Text\033[0m";

            String result = AnsiColor.stripAnsiEscapeSequences(input);

            assertThat(result).isEqualTo("Red Text");
        }

        @Test
        @DisplayName("Should return same string when no ANSI codes")
        public void shouldReturnSameStringWhenNoAnsiCodes() {
            String input = "Plain text";

            String result = AnsiColor.stripAnsiEscapeSequences(input);

            assertThat(result).isEqualTo("Plain text");
        }

        @Test
        @DisplayName("Should handle null input")
        public void shouldHandleNullInput() {
            String result = AnsiColor.stripAnsiEscapeSequences(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle empty string")
        public void shouldHandleEmptyString() {
            String result = AnsiColor.stripAnsiEscapeSequences("");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should strip multiple ANSI codes")
        public void shouldStripMultipleAnsiCodes() {
            String input = "\033[0;31mRed\033[0m \033[0;32mGreen\033[0m";

            String result = AnsiColor.stripAnsiEscapeSequences(input);

            assertThat(result).isEqualTo("Red Green");
        }

        @Test
        @DisplayName("Should strip complex ANSI codes")
        public void shouldStripComplexAnsiCodes() {
            String input = "\033[1;38;5;196mBold Red\033[0m";

            String result = AnsiColor.stripAnsiEscapeSequences(input);

            assertThat(result).isEqualTo("Bold Red");
        }
    }

    @Nested
    @DisplayName("IsSupported Tests")
    public class IsSupportedTests {

        @Test
        @DisplayName("Should return support status")
        public void shouldReturnSupportStatus() {
            boolean supported = AnsiColor.isSupported();

            assertThat(supported).isIn(true, false);
        }

        @Test
        @DisplayName("Should reflect set value")
        public void shouldReflectSetValue() {
            AnsiColor.setSupported(true);

            assertThat(AnsiColor.isSupported()).isTrue();

            AnsiColor.setSupported(false);

            assertThat(AnsiColor.isSupported()).isFalse();
        }
    }

    @Nested
    @DisplayName("SetSupported Tests")
    public class SetSupportedTests {

        @Test
        @DisplayName("Should enable ANSI color support")
        public void shouldEnableAnsiColorSupport() {
            AnsiColor.setSupported(true);

            assertThat(AnsiColor.isSupported()).isTrue();
        }

        @Test
        @DisplayName("Should disable ANSI color support")
        public void shouldDisableAnsiColorSupport() {
            AnsiColor.setSupported(false);

            assertThat(AnsiColor.isSupported()).isFalse();
        }

        @Test
        @DisplayName("Should affect wrap behavior")
        public void shouldAffectWrapBehavior() {
            AnsiColor.setSupported(false);
            String noColor = AnsiColor.TEXT_RED.wrap("test");

            AnsiColor.setSupported(true);
            String withColor = AnsiColor.TEXT_RED.wrap("test");

            assertThat(noColor).isEqualTo("test");
            assertThat(withColor).isNotEqualTo("test");
        }

        @Test
        @DisplayName("Should affect toString behavior")
        public void shouldAffectToStringBehavior() {
            AnsiColor.setSupported(false);
            String noColor = AnsiColor.TEXT_GREEN.toString();

            AnsiColor.setSupported(true);
            String withColor = AnsiColor.TEXT_GREEN.toString();

            assertThat(noColor).isEmpty();
            assertThat(withColor).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("OfSequence Tests")
    public class OfSequenceTests {

        @Test
        @DisplayName("Should create color from escape sequence")
        public void shouldCreateColorFromEscapeSequence() {
            AnsiColor color = AnsiColor.ofSequence("\033[0;31m");

            assertThat(color).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception for null sequence")
        public void shouldThrowExceptionForNullSequence() {
            assertThatThrownBy(() -> AnsiColor.ofSequence(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("escapeSequence is null");
        }

        @Test
        @DisplayName("Should throw exception for blank sequence")
        public void shouldThrowExceptionForBlankSequence() {
            assertThatThrownBy(() -> AnsiColor.ofSequence("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("escapeSequence is blank");
        }

        @Test
        @DisplayName("Should throw exception for empty sequence")
        public void shouldThrowExceptionForEmptySequence() {
            assertThatThrownBy(() -> AnsiColor.ofSequence(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("escapeSequence is blank");
        }

        @Test
        @DisplayName("Should create color that wraps text")
        public void shouldCreateColorThatWrapsText() {
            AnsiColor.setSupported(false);
            AnsiColor color = AnsiColor.ofSequence("\033[0;35m");

            String result = color.wrap("test");

            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("Should create colors with different sequences")
        public void shouldCreateColorsWithDifferentSequences() {
            AnsiColor color1 = AnsiColor.ofSequence("\033[0;31m");
            AnsiColor color2 = AnsiColor.ofSequence("\033[0;32m");

            assertThat(color1).isNotEqualTo(color2);
        }

        @Test
        @DisplayName("Should create equal colors with same sequence")
        public void shouldCreateEqualColorsWithSameSequence() {
            AnsiColor color1 = AnsiColor.ofSequence("\033[0;31m");
            AnsiColor color2 = AnsiColor.ofSequence("\033[0;31m");

            assertThat(color1).isEqualTo(color2);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    public class IntegrationTests {

        @Test
        @DisplayName("Should wrap and strip ANSI codes")
        public void shouldWrapAndStripAnsiCodes() {
            AnsiColor.setSupported(true);
            String wrapped = AnsiColor.TEXT_RED.wrap("test");
            String stripped = AnsiColor.stripAnsiEscapeSequences(wrapped);

            assertThat(stripped).isEqualTo("test");
        }

        @Test
        @DisplayName("Should handle multiple colors in sequence")
        public void shouldHandleMultipleColorsInSequence() {
            AnsiColor.setSupported(false);

            String result = AnsiColor.TEXT_RED.wrap("red") + " "
                    + AnsiColor.TEXT_GREEN.wrap("green") + " "
                    + AnsiColor.TEXT_BLUE.wrap("blue");

            assertThat(result).isEqualTo("red green blue");
        }

        @Test
        @DisplayName("Should work with different color styles")
        public void shouldWorkWithDifferentColorStyles() {
            AnsiColor.setSupported(false);

            String regular = AnsiColor.TEXT_RED.wrap("regular");
            String bold = AnsiColor.TEXT_RED_BOLD.wrap("bold");
            String bright = AnsiColor.TEXT_RED_BRIGHT.wrap("bright");

            assertThat(regular).isEqualTo("regular");
            assertThat(bold).isEqualTo("bold");
            assertThat(bright).isEqualTo("bright");
        }
    }

    @Nested
    @DisplayName("Additional Edge Case Tests")
    public class AdditionalEdgeCaseTests {

        @Test
        @DisplayName("Should wrap null object when supported")
        public void shouldWrapNullObjectWhenSupported() {
            AnsiColor.setSupported(true);

            String result = AnsiColor.TEXT_BLUE.wrap(null);

            assertThat(result).contains("null");
        }

        @Test
        @DisplayName("Should wrap object with custom toString")
        public void shouldWrapObjectWithCustomToString() {
            AnsiColor.setSupported(false);
            Object customObject = new Object() {
                @Override
                public String toString() {
                    return "custom";
                }
            };

            String result = AnsiColor.TEXT_GREEN.wrap(customObject);

            assertThat(result).isEqualTo("custom");
        }

        @Test
        @DisplayName("Should strip ANSI codes from string with only ANSI codes")
        public void shouldStripAnsiCodesFromStringWithOnlyAnsiCodes() {
            String input = "\033[0;31m\033[0m";

            String result = AnsiColor.stripAnsiEscapeSequences(input);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle string with embedded ANSI codes")
        public void shouldHandleStringWithEmbeddedAnsiCodes() {
            String input = "Hello\033[0;31mWorld\033[0m!";

            String result = AnsiColor.stripAnsiEscapeSequences(input);

            assertThat(result).isEqualTo("HelloWorld!");
        }

        @Test
        @DisplayName("Should create equal colors with same sequence and verify equals contract")
        public void shouldCreateEqualColorsWithSameSequenceAndVerifyEqualsContract() {
            AnsiColor color1 = AnsiColor.ofSequence("\033[0;31m");
            AnsiColor color2 = AnsiColor.ofSequence("\033[0;31m");

            assertThat(color1).isEqualTo(color2);
            assertThat(color2).isEqualTo(color1);
            assertThat(color1).hasSameHashCodeAs(color2);
        }

        @Test
        @DisplayName("Should verify equals is symmetric")
        public void shouldVerifyEqualsIsSymmetric() {
            AnsiColor color1 = AnsiColor.ofSequence("\033[0;35m");
            AnsiColor color2 = AnsiColor.ofSequence("\033[0;35m");

            assertThat(color1.equals(color2)).isEqualTo(color2.equals(color1));
        }

        @Test
        @DisplayName("Should verify equals is transitive")
        public void shouldVerifyEqualsIsTransitive() {
            AnsiColor color1 = AnsiColor.ofSequence("\033[0;35m");
            AnsiColor color2 = AnsiColor.ofSequence("\033[0;35m");
            AnsiColor color3 = AnsiColor.ofSequence("\033[0;35m");

            assertThat(color1.equals(color2)).isTrue();
            assertThat(color2.equals(color3)).isTrue();
            assertThat(color1.equals(color3)).isTrue();
        }

        @Test
        @DisplayName("Should verify equals is consistent")
        public void shouldVerifyEqualsIsConsistent() {
            AnsiColor color1 = AnsiColor.TEXT_RED;
            AnsiColor color2 = AnsiColor.TEXT_RED;

            boolean first = color1.equals(color2);
            boolean second = color1.equals(color2);

            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("Should handle wrap with empty string when supported")
        public void shouldHandleWrapWithEmptyStringWhenSupported() {
            AnsiColor.setSupported(true);

            String result = AnsiColor.TEXT_CYAN.wrap("");

            assertThat(result).isNotEmpty();
            assertThat(AnsiColor.stripAnsiEscapeSequences(result)).isEmpty();
        }

        @Test
        @DisplayName("Should handle wrap with whitespace string")
        public void shouldHandleWrapWithWhitespaceString() {
            AnsiColor.setSupported(false);

            String result = AnsiColor.TEXT_YELLOW.wrap("   ");

            assertThat(result).isEqualTo("   ");
        }

        @Test
        @DisplayName("Should handle stripAnsiEscapeSequences with consecutive ANSI codes")
        public void shouldHandleStripAnsiEscapeSequencesWithConsecutiveAnsiCodes() {
            String input = "\033[0;31m\033[1m\033[0mtext";

            String result = AnsiColor.stripAnsiEscapeSequences(input);

            assertThat(result).isEqualTo("text");
        }

        @Test
        @DisplayName("Should handle stripAnsiEscapeSequences with 256 color codes")
        public void shouldHandleStripAnsiEscapeSequencesWith256ColorCodes() {
            String input = "\033[38;5;196mRed256\033[0m";

            String result = AnsiColor.stripAnsiEscapeSequences(input);

            assertThat(result).isEqualTo("Red256");
        }

        @Test
        @DisplayName("Should handle stripAnsiEscapeSequences with RGB color codes")
        public void shouldHandleStripAnsiEscapeSequencesWithRgbColorCodes() {
            String input = "\033[38;2;255;0;0mRGB Red\033[0m";

            String result = AnsiColor.stripAnsiEscapeSequences(input);

            assertThat(result).isEqualTo("RGB Red");
        }

        @Test
        @DisplayName("Should handle ofSequence with tab character as blank")
        public void shouldHandleOfSequenceWithTabCharacterAsBlank() {
            assertThatThrownBy(() -> AnsiColor.ofSequence("\t"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("escapeSequence is blank");
        }

        @Test
        @DisplayName("Should handle ofSequence with newline character as blank")
        public void shouldHandleOfSequenceWithNewlineCharacterAsBlank() {
            assertThatThrownBy(() -> AnsiColor.ofSequence("\n"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("escapeSequence is blank");
        }

        @Test
        @DisplayName("Should wrap all predefined color constants")
        public void shouldWrapAllPredefinedColorConstants() {
            AnsiColor.setSupported(false);

            assertThat(AnsiColor.NONE.wrap("test")).isEqualTo("test");
            assertThat(AnsiColor.TEXT_BLACK.wrap("test")).isEqualTo("test");
            assertThat(AnsiColor.TEXT_RED.wrap("test")).isEqualTo("test");
            assertThat(AnsiColor.TEXT_GREEN.wrap("test")).isEqualTo("test");
            assertThat(AnsiColor.TEXT_YELLOW.wrap("test")).isEqualTo("test");
            assertThat(AnsiColor.TEXT_BLUE.wrap("test")).isEqualTo("test");
            assertThat(AnsiColor.TEXT_PURPLE.wrap("test")).isEqualTo("test");
            assertThat(AnsiColor.TEXT_CYAN.wrap("test")).isEqualTo("test");
            assertThat(AnsiColor.TEXT_WHITE.wrap("test")).isEqualTo("test");
        }

        @Test
        @DisplayName("Should handle multiple setSupported calls")
        public void shouldHandleMultipleSetSupportedCalls() {
            AnsiColor.setSupported(true);
            assertThat(AnsiColor.isSupported()).isTrue();

            AnsiColor.setSupported(false);
            assertThat(AnsiColor.isSupported()).isFalse();

            AnsiColor.setSupported(true);
            assertThat(AnsiColor.isSupported()).isTrue();

            AnsiColor.setSupported(false);
            assertThat(AnsiColor.isSupported()).isFalse();
        }
    }
}
