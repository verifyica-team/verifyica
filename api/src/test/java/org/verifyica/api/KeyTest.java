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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class KeyTest {

    @Test
    public void test1() {
        Key key1 = Key.of("a");

        assertThat(key1.flatten()).isEqualTo("/a");

        Key key2 = key1.append("b", "c", "d");

        assertThat(key2.flatten()).isEqualTo("/a/b/c/d");

        List<String> segments = new ArrayList<>();
        segments.add("a");
        segments.add("b");
        segments.add("c");

        Key key3 = key1.append(segments);

        assertThat(key3.flatten()).isEqualTo("/a/a/b/c");
    }

    @Test
    public void test2() {
        Key key1 = Key.of("a");

        assertThat(key1.flatten()).isEqualTo("/a");

        Key key2 = Key.of("a", "b", "c");

        assertThat(key2.flatten()).isEqualTo("/a/b/c");

        Key key3 = key2.append("1");

        assertThat(key3.flatten()).isEqualTo("/a/b/c/1");

        Key key4 = key1.append(key2).append(key3);

        assertThat(key4.flatten()).isEqualTo("/a/a/b/c/a/b/c/1");
    }

    @Test
    public void test3() {
        Key key = Key.of("foo", "bar");

        assertThat(key).isNotNull();
        assertThat(key.segments()).isNotNull();
        assertThat(key.segments()).hasSize(2);
        assertThat(key.segments().get(0)).isEqualTo("foo");
        assertThat(key.segments().get(1)).isEqualTo("bar");

        Key key2 = key.remove();

        assertThat(key2).isNotNull();
        assertThat(key2.segments()).isNotNull();
        assertThat(key2.segments()).hasSize(1);
        assertThat(key2.segments().get(0)).isEqualTo("foo");

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(key2::remove);

        key2 = key2.append("bar");

        assertThat(key2).isNotNull();
        assertThat(key2.segments()).isNotNull();
        assertThat(key2.segments()).hasSize(2);
        assertThat(key2.segments().get(0)).isEqualTo("foo");
        assertThat(key2.segments().get(1)).isEqualTo("bar");

        key2 = key2.append("value");

        assertThat(key2).isNotNull();
        assertThat(key2.segments()).isNotNull();
        assertThat(key2.segments()).hasSize(3);
        assertThat(key2.segments().get(0)).isEqualTo("foo");
        assertThat(key2.segments().get(1)).isEqualTo("bar");
        assertThat(key2.segments().get(2)).isEqualTo("value");

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> key.append((String) null));

        Key classContextKey = Key.of(KeyTest.class.getName(), "class.context.key");

        assertThat(classContextKey.segments()).hasSize(2);

        Key fooKey = classContextKey.append("foo");

        assertThat(fooKey.segments()).hasSize(3);

        Key barKey = classContextKey.append("bar");

        assertThat(barKey.segments()).hasSize(3);
        assertThat(classContextKey.segments()).hasSize(2);
    }

    @Test
    public void test4() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Key.of((String) null));

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Key.of("foo", null));

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Key.of(null, "bar"));

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Key.of(null, null));

        Key key = Key.of("a");

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> key.append((String) null));

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> key.append(null, null));

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> key.append(new ArrayList<>()));

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> key.append((Key) null));
    }
}
