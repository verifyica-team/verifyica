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

package org.verifyica.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ExtendedMapTest {

    @Test
    public void testCommon() {
        ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();

        extendedMap.put("foo", 1);

        assertThat((Object) extendedMap.getAs("foo")).isNotNull();
        assertThat((Object) extendedMap.getAs("foo")).isInstanceOf(Integer.class);
        assertThat((Integer) extendedMap.getAs("foo")).isEqualTo(1);

        assertThat(extendedMap.getAs("foo", Integer.class)).isNotNull();
        assertThat(extendedMap.getAs("foo", Integer.class)).isInstanceOf(Integer.class);
        assertThat(extendedMap.getAs("foo", Integer.class)).isEqualTo(1);

        Object value = extendedMap.removeAs("foo");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(Integer.class);
        assertThat((Integer) value).isEqualTo(1);

        assertThat(extendedMap).isEmpty();
    }

    @Test
    public void testInheritance() {
        ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();

        extendedMap.put("foo", new SuperClass("bar"));

        assertThat((Object) extendedMap.getAs("foo")).isNotNull();
        assertThat((Object) extendedMap.getAs("foo")).isInstanceOf(SuperClass.class);
        assertThat((Object) extendedMap.getAs("foo")).isInstanceOf(SubClass.class);
        assertThat(((SuperClass) extendedMap.getAs("foo")).getValue()).isEqualTo("bar");
        assertThat(((SubClass) extendedMap.getAs("foo")).getValue()).isEqualTo("bar");

        assertThat(extendedMap.getAs("foo", SubClass.class)).isNotNull();
        assertThat(extendedMap.getAs("foo", SubClass.class)).isInstanceOf(SubClass.class);
        assertThat(extendedMap.getAs("foo", SubClass.class).getValue()).isEqualTo("bar");

        assertThat(extendedMap.getAs("foo", SuperClass.class)).isNotNull();
        assertThat(extendedMap.getAs("foo", SuperClass.class)).isInstanceOf(SuperClass.class);
        assertThat(extendedMap.getAs("foo", SuperClass.class).getValue()).isEqualTo("bar");

        Object value = extendedMap.removeAs("foo");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(SubClass.class);
        assertThat(value).isInstanceOf(SuperClass.class);
        assertThat(((SubClass) value).getValue()).isEqualTo("bar");
        assertThat(((SuperClass) value).getValue()).isEqualTo("bar");

        assertThat(extendedMap).isEmpty();
    }

    public static class SubClass {

        private final String value;

        public SubClass(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class SuperClass extends SubClass {

        public SuperClass(String value) {
            super(value);
        }
    }
}
