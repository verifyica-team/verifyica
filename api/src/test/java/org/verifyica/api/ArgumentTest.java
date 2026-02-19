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

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Argument Tests")
public class ArgumentTest {

    @Test
    @DisplayName("Should create argument with name and payload")
    public void testOf1() {
        String name = "foo";
        String payload = "bar";

        Argument<?> argument = Argument.of(name, payload);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(name);
        assertThat(argument.toString()).isEqualTo(name);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(String.class);
        assertThat((String) argument.getPayload()).isEqualTo(payload);
        assertThat(argument.getPayloadAs(String.class)).isInstanceOf(String.class);
        assertThat(argument.getPayloadAs(String.class)).isEqualTo(payload);
    }

    @Test
    @DisplayName("Should create argument with trimmed name")
    public void testOf2() {
        String name = " foo ";
        String payload = "bar";

        Argument<?> argument = Argument.of(name, payload);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(name);
        assertThat(argument.toString()).isEqualTo(name);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(String.class);
        assertThat((String) argument.getPayload()).isEqualTo(payload);
        assertThat(argument.getPayloadAs(String.class)).isInstanceOf(String.class);
        assertThat(argument.getPayloadAs(String.class)).isEqualTo(payload);
    }

    @Test
    @DisplayName("Should reject invalid names")
    public void testOfBadNames() {
        String[] names =
                new String[] {null, "", " ", " \t ", "\t", "\r", "\n", "\r\n", "\t\r\n", " \t", " \t\r", " \t\r\n"};

        for (String name : names) {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Argument.of(name, null));
        }
    }

    @Test
    @DisplayName("Should create argument with null payload")
    public void testOfNullPayload() {
        String name = "foo";

        Argument<?> argument = Argument.of(name, null);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(name);
        assertThat(argument.toString()).isEqualTo(name);
        assertThat(argument.hasPayload()).isFalse();
        assertThat(argument.getPayload()).isNull();
    }

    @Test
    @DisplayName("Should return singleton empty argument")
    public void testEmpty() {
        Argument<?> argument1 = Argument.EMPTY;

        assertThat(argument1).isNotNull();
        assertThat(argument1.getName()).isEqualTo("---");
        assertThat(argument1.toString()).isEqualTo("---");
        assertThat(argument1.hasPayload()).isFalse();
        assertThat(argument1.getPayload()).isNull();

        Argument<?> argument2 = Argument.EMPTY;

        assertThat(argument2).isNotNull();
        assertThat(argument2.getName()).isEqualTo("---");
        assertThat(argument2.toString()).isEqualTo("---");
        assertThat(argument2.hasPayload()).isFalse();
        assertThat(argument2.getPayload()).isNull();

        assertThat(argument2).isSameAs(argument1);
    }

    @Test
    @DisplayName("Should create argument with boolean payload")
    public void testOfBoolean() {
        String name = "foo";
        boolean value = true;
        Argument<?> argument = Argument.of(name, value);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(name);
        assertThat(argument.toString()).isEqualTo(name);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(Boolean.class);
        assertThat((Boolean) argument.getPayload()).isEqualTo(value);
        assertThat(argument.getPayloadAs(Boolean.class)).isInstanceOf(Boolean.class);
        assertThat(argument.getPayloadAs(Boolean.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    @Test
    @DisplayName("Should create argument with byte payload")
    public void testOfByte() {
        String name = "foo";
        byte value = 1;
        Argument<?> argument = Argument.of(name, value);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(name);
        assertThat(argument.toString()).isEqualTo(name);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(Byte.class);
        assertThat((Byte) argument.getPayload()).isEqualTo(value);
        assertThat(argument.getPayloadAs(Byte.class)).isInstanceOf(Byte.class);
        assertThat(argument.getPayloadAs(Byte.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    @Test
    @DisplayName("Should create argument with char payload")
    public void testOfChar() {
        String name = "foo";
        char value = 'x';
        Argument<?> argument = Argument.of(name, value);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(name);
        assertThat(argument.toString()).isEqualTo(name);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(Character.class);
        assertThat((Character) argument.getPayload()).isEqualTo(value);
        assertThat(argument.getPayloadAs(Character.class)).isInstanceOf(Character.class);
        assertThat(argument.getPayloadAs(Character.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    @Test
    @DisplayName("Should create argument with short payload")
    public void testOfShort() {
        String name = "foo";
        short value = 1;
        Argument<?> argument = Argument.of(name, value);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(name);
        assertThat(argument.toString()).isEqualTo(name);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(Short.class);
        assertThat((Short) argument.getPayload()).isEqualTo(value);
        assertThat(argument.getPayloadAs(Short.class)).isInstanceOf(Short.class);
        assertThat(argument.getPayloadAs(Short.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    @Test
    @DisplayName("Should create argument with int payload")
    public void testOfInt() {
        String name = "foo";
        int value = 1;
        Argument<?> argument = Argument.of(name, value);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(name);
        assertThat(argument.toString()).isEqualTo(name);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(Integer.class);
        assertThat((Integer) argument.getPayload()).isEqualTo(value);
        assertThat(argument.getPayloadAs(Integer.class)).isInstanceOf(Integer.class);
        assertThat(argument.getPayloadAs(Integer.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    @Test
    @DisplayName("Should create argument with long payload")
    public void testOfLong() {
        String name = "foo";
        long value = 1;
        Argument<?> argument = Argument.of(name, value);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(name);
        assertThat(argument.toString()).isEqualTo(name);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(Long.class);
        assertThat((Long) argument.getPayload()).isEqualTo(value);
        assertThat(argument.getPayloadAs(Long.class)).isInstanceOf(Long.class);
        assertThat(argument.getPayloadAs(Long.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    @Test
    @DisplayName("Should create argument with float payload")
    public void testOfFloat() {
        String name = "foo";
        float value = 1.0f;
        Argument<?> argument = Argument.of(name, value);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(name);
        assertThat(argument.toString()).isEqualTo(name);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(Float.class);
        assertThat((Float) argument.getPayload()).isEqualTo(value);
        assertThat(argument.getPayloadAs(Float.class)).isInstanceOf(Float.class);
        assertThat(argument.getPayloadAs(Float.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    @Test
    @DisplayName("Should create argument with double payload")
    public void testOfDouble() {
        String name = "foo";
        double value = 1.0;
        Argument<?> argument = Argument.of(name, value);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(name);
        assertThat(argument.toString()).isEqualTo(name);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(Double.class);
        assertThat((Double) argument.getPayload()).isEqualTo(value);
        assertThat(argument.getPayloadAs(Double.class)).isInstanceOf(Double.class);
        assertThat(argument.getPayloadAs(Double.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    @Test
    @DisplayName("Should create argument with BigInteger payload using of()")
    public void testOfBigInteger1() {
        String name = "foo";
        String value = "1";
        BigInteger bigIntegerValue = new BigInteger(value);
        Argument<?> argument = Argument.of(name, bigIntegerValue);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(name);
        assertThat(argument.toString()).isEqualTo(name);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(BigInteger.class);
        assertThat((BigInteger) argument.getPayload()).isEqualTo(bigIntegerValue);
        assertThat(argument.getPayloadAs(BigInteger.class)).isInstanceOf(BigInteger.class);
        assertThat(argument.getPayloadAs(BigInteger.class)).isEqualTo(bigIntegerValue);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    @Test
    @DisplayName("Should create argument with BigInteger payload using ofBigInteger()")
    public void testOfBigInteger2() {
        String value = "1";
        BigInteger bigIntegerValue = new BigInteger(value);
        Argument<?> argument = Argument.ofBigInteger(bigIntegerValue);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(value);
        assertThat(argument.toString()).isEqualTo(value);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(BigInteger.class);
        assertThat((BigInteger) argument.getPayload()).isEqualTo(bigIntegerValue);
        assertThat(argument.getPayloadAs(BigInteger.class)).isInstanceOf(BigInteger.class);
        assertThat(argument.getPayloadAs(BigInteger.class)).isEqualTo(bigIntegerValue);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    @Test
    @DisplayName("Should create argument with BigDecimal payload using of()")
    public void testOfBigDecimal1() {
        String name = "foo";
        String value = "1.0";
        BigDecimal bigDecimalValue = new BigDecimal(value);
        Argument<?> argument = Argument.of(name, bigDecimalValue);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(name);
        assertThat(argument.toString()).isEqualTo(name);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(BigDecimal.class);
        assertThat((BigDecimal) argument.getPayload()).isEqualTo(bigDecimalValue);
        assertThat(argument.getPayloadAs(BigDecimal.class)).isInstanceOf(BigDecimal.class);
        assertThat(argument.getPayloadAs(BigDecimal.class)).isEqualTo(bigDecimalValue);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    @Test
    @DisplayName("Should create argument with BigDecimal payload using ofBigDecimal()")
    public void testOfBigDecimal2() {
        String value = "1.0";
        BigDecimal bigDecimalValue = new BigDecimal(value);
        Argument<?> argument = Argument.ofBigDecimal(bigDecimalValue);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(value);
        assertThat(argument.toString()).isEqualTo(value);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(BigDecimal.class);
        assertThat((BigDecimal) argument.getPayload()).isEqualTo(bigDecimalValue);
        assertThat(argument.getPayloadAs(BigDecimal.class)).isInstanceOf(BigDecimal.class);
        assertThat(argument.getPayloadAs(BigDecimal.class)).isEqualTo(bigDecimalValue);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    @Test
    @DisplayName("Should create argument with String payload using ofString()")
    public void testOfString() {
        String nameAndValue = "foo";
        Argument<?> argument = Argument.ofString(nameAndValue);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo(nameAndValue);
        assertThat(argument.toString()).isEqualTo(nameAndValue);
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(String.class);
        assertThat((String) argument.getPayload()).isEqualTo(nameAndValue);
        assertThat(argument.getPayloadAs(String.class)).isInstanceOf(String.class);
        assertThat(argument.getPayloadAs(String.class)).isEqualTo(nameAndValue);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(Integer.class));
    }

    @Test
    @DisplayName("Should create argument with null string using ofString()")
    public void testOfStringNull() {
        Argument<?> argument = Argument.ofString(null);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo("String=/null/");
        assertThat(argument.toString()).isEqualTo("String=/null/");
        assertThat(argument.hasPayload()).isFalse();
        assertThat(argument.getPayload()).isNull();
    }

    @Test
    @DisplayName("Should create argument with empty string using ofString()")
    public void testOfStringEmpty() {
        Argument<?> argument = Argument.ofString("");

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo("String=/empty/");
        assertThat(argument.toString()).isEqualTo("String=/empty/");
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isEqualTo("");
    }

    @Test
    @DisplayName("Should create argument with null BigInteger")
    public void testOfBigIntegerNull() {
        Argument<?> argument = Argument.ofBigInteger((BigInteger) null);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo("BigInteger=/null/");
        assertThat(argument.hasPayload()).isFalse();
        assertThat(argument.getPayload()).isNull();
    }

    @Test
    @DisplayName("Should create argument with null BigInteger string")
    public void testOfBigIntegerStringNull() {
        Argument<?> argument = Argument.ofBigInteger((String) null);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo("BigInteger=/null/");
        assertThat(argument.hasPayload()).isFalse();
        assertThat(argument.getPayload()).isNull();
    }

    @Test
    @DisplayName("Should create argument with valid BigInteger string")
    public void testOfBigIntegerStringValid() {
        Argument<?> argument = Argument.ofBigInteger("12345678901234567890");

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo("12345678901234567890");
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(BigInteger.class);
        assertThat(argument.getPayload()).isEqualTo(new BigInteger("12345678901234567890"));
    }

    @Test
    @DisplayName("Should throw exception for invalid BigInteger string")
    public void testOfBigIntegerStringInvalidNumber() {
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(() -> Argument.ofBigInteger("not-a-number"));
    }

    @Test
    @DisplayName("Should create argument with null BigDecimal")
    public void testOfBigDecimalNull() {
        Argument<?> argument = Argument.ofBigDecimal((BigDecimal) null);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo("BigDecimal=/null/");
        assertThat(argument.hasPayload()).isFalse();
        assertThat(argument.getPayload()).isNull();
    }

    @Test
    @DisplayName("Should create argument with null BigDecimal string")
    public void testOfBigDecimalStringNull() {
        Argument<?> argument = Argument.ofBigDecimal((String) null);

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo("BigDecimal=/null/");
        assertThat(argument.hasPayload()).isFalse();
        assertThat(argument.getPayload()).isNull();
    }

    @Test
    @DisplayName("Should create argument with valid BigDecimal string")
    public void testOfBigDecimalStringValid() {
        Argument<?> argument = Argument.ofBigDecimal("12345.67890");

        assertThat(argument).isNotNull();
        assertThat(argument.getName()).isEqualTo("12345.67890");
        assertThat(argument.hasPayload()).isTrue();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isInstanceOf(BigDecimal.class);
        assertThat(argument.getPayload()).isEqualTo(new BigDecimal("12345.67890"));
    }

    @Test
    @DisplayName("Should throw exception for invalid BigDecimal string")
    public void testOfBigDecimalStringInvalidNumber() {
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(() -> Argument.ofBigDecimal("not-a-number"));
    }

    @Test
    @DisplayName("Should create arguments for all primitive wrapper types")
    public void testOfPrimitiveWrapperTypes() {
        // Boolean wrapper
        Argument<Boolean> boolArg = Argument.ofBoolean(true);
        assertThat(boolArg.getPayload()).isInstanceOf(Boolean.class);
        assertThat(boolArg.getName()).isEqualTo("true");

        // Byte wrapper
        Argument<Byte> byteArg = Argument.ofByte((byte) 42);
        assertThat(byteArg.getPayload()).isInstanceOf(Byte.class);
        assertThat(byteArg.getName()).isEqualTo("42");

        // Character wrapper
        Argument<Character> charArg = Argument.ofChar('A');
        assertThat(charArg.getPayload()).isInstanceOf(Character.class);
        assertThat(charArg.getName()).isEqualTo("A");

        // Short wrapper
        Argument<Short> shortArg = Argument.ofShort((short) 1000);
        assertThat(shortArg.getPayload()).isInstanceOf(Short.class);
        assertThat(shortArg.getName()).isEqualTo("1000");

        // Integer wrapper
        Argument<Integer> intArg = Argument.ofInt(42);
        assertThat(intArg.getPayload()).isInstanceOf(Integer.class);
        assertThat(intArg.getName()).isEqualTo("42");

        // Long wrapper
        Argument<Long> longArg = Argument.ofLong(123456789L);
        assertThat(longArg.getPayload()).isInstanceOf(Long.class);
        assertThat(longArg.getName()).isEqualTo("123456789");

        // Float wrapper
        Argument<Float> floatArg = Argument.ofFloat(3.14f);
        assertThat(floatArg.getPayload()).isInstanceOf(Float.class);
        assertThat(floatArg.getName()).isEqualTo("3.14");

        // Double wrapper
        Argument<Double> doubleArg = Argument.ofDouble(3.14159);
        assertThat(doubleArg.getPayload()).isInstanceOf(Double.class);
        assertThat(doubleArg.getName()).isEqualTo("3.14159");
    }

    @Test
    @DisplayName("Should throw exception when payload type is null")
    public void testPayloadTypeNullThrows() {
        Argument<String> argument = Argument.of("test", "value");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> argument.getPayloadAs(null))
                .withMessage("type is null");
    }

    @Test
    @DisplayName("Should support type casting of payload")
    public void testCasting() {
        Argument<?> argument = Argument.of("testObject1", new TestObject1());

        assertThat(argument).isNotNull();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload().getClass()).isEqualTo(TestObject1.class);
        assertThat(argument.getPayloadAs(TestObject1.class)).isNotNull();
        assertThat(argument.getPayloadAs(TestObject1.class).getClass()).isEqualTo(TestObject1.class);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    @Test
    @DisplayName("Should support inheritance casting of payload")
    public void testInheritanceCasting() {
        Object object = new TestObject2();
        Argument<?> argument = Argument.of("testObject1", object);

        assertThat(argument).isNotNull();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isSameAs(object);
        assertThat(argument.getPayload().getClass()).isEqualTo(TestObject2.class);
        assertThat(argument.getPayloadAs(TestObject2.class)).isNotNull();
        assertThat(argument.getPayload()).isSameAs(object);
        assertThat(argument.getPayloadAs(TestObject1.class)).isNotNull();
        assertThat(argument.getPayload()).isSameAs(object);
        assertThat(argument.getPayloadAs(TestObject1.class).getClass()).isEqualTo(TestObject2.class);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayloadAs(String.class));
    }

    public static class TestObject1 {

        public TestObject1() {
            // INTENTIONALLY EMPTY
        }
    }

    public static class TestObject2 extends TestObject1 {

        public TestObject2() {
            super();
        }
    }
}
