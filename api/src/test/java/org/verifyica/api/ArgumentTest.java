/*
 * Copyright (C) Verifyica project authors and contributors
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
import org.junit.jupiter.api.Test;

public class ArgumentTest {

    @Test
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
        assertThat(argument.getPayload(String.class)).isInstanceOf(String.class);
        assertThat(argument.getPayload(String.class)).isEqualTo(payload);
    }

    @Test
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
        assertThat(argument.getPayload(String.class)).isInstanceOf(String.class);
        assertThat(argument.getPayload(String.class)).isEqualTo(payload);
    }

    @Test
    public void testOfBadNames() {
        String[] names =
                new String[] {null, "", " ", " \t ", "\t", "\r", "\n", "\r\n", "\t\r\n", " \t", " \t\r", " \t\r\n"};

        for (String name : names) {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Argument.of(name, null));
        }
    }

    @Test
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
        assertThat(argument.getPayload(Boolean.class)).isInstanceOf(Boolean.class);
        assertThat(argument.getPayload(Boolean.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
    }

    @Test
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
        assertThat(argument.getPayload(Byte.class)).isInstanceOf(Byte.class);
        assertThat(argument.getPayload(Byte.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
    }

    @Test
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
        assertThat(argument.getPayload(Character.class)).isInstanceOf(Character.class);
        assertThat(argument.getPayload(Character.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
    }

    @Test
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
        assertThat(argument.getPayload(Short.class)).isInstanceOf(Short.class);
        assertThat(argument.getPayload(Short.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
    }

    @Test
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
        assertThat(argument.getPayload(Integer.class)).isInstanceOf(Integer.class);
        assertThat(argument.getPayload(Integer.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
    }

    @Test
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
        assertThat(argument.getPayload(Long.class)).isInstanceOf(Long.class);
        assertThat(argument.getPayload(Long.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
    }

    @Test
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
        assertThat(argument.getPayload(Float.class)).isInstanceOf(Float.class);
        assertThat(argument.getPayload(Float.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
    }

    @Test
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
        assertThat(argument.getPayload(Double.class)).isInstanceOf(Double.class);
        assertThat(argument.getPayload(Double.class)).isEqualTo(value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
    }

    @Test
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
        assertThat(argument.getPayload(BigInteger.class)).isInstanceOf(BigInteger.class);
        assertThat(argument.getPayload(BigInteger.class)).isEqualTo(bigIntegerValue);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
    }

    @Test
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
        assertThat(argument.getPayload(BigInteger.class)).isInstanceOf(BigInteger.class);
        assertThat(argument.getPayload(BigInteger.class)).isEqualTo(bigIntegerValue);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
    }

    @Test
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
        assertThat(argument.getPayload(BigDecimal.class)).isInstanceOf(BigDecimal.class);
        assertThat(argument.getPayload(BigDecimal.class)).isEqualTo(bigDecimalValue);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
    }

    @Test
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
        assertThat(argument.getPayload(BigDecimal.class)).isInstanceOf(BigDecimal.class);
        assertThat(argument.getPayload(BigDecimal.class)).isEqualTo(bigDecimalValue);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
    }

    @Test
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
        assertThat(argument.getPayload(String.class)).isInstanceOf(String.class);
        assertThat(argument.getPayload(String.class)).isEqualTo(nameAndValue);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(Integer.class));
    }

    @Test
    public void testCasting() {
        Argument<?> argument = Argument.of("testObject1", new TestObject1());

        assertThat(argument).isNotNull();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload().getClass()).isEqualTo(TestObject1.class);
        assertThat(argument.getPayload(TestObject1.class)).isNotNull();
        assertThat(argument.getPayload(TestObject1.class).getClass()).isEqualTo(TestObject1.class);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
    }

    @Test
    public void testInheritanceCasting() {
        Object object = new TestObject2();
        Argument<?> argument = Argument.of("testObject1", object);

        assertThat(argument).isNotNull();
        assertThat(argument.getPayload()).isNotNull();
        assertThat(argument.getPayload()).isSameAs(object);
        assertThat(argument.getPayload().getClass()).isEqualTo(TestObject2.class);
        assertThat(argument.getPayload(TestObject2.class)).isNotNull();
        assertThat(argument.getPayload()).isSameAs(object);
        assertThat(argument.getPayload(TestObject1.class)).isNotNull();
        assertThat(argument.getPayload()).isSameAs(object);
        assertThat(argument.getPayload(TestObject1.class).getClass()).isEqualTo(TestObject2.class);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> argument.getPayload(String.class));
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
