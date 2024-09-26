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

package org.verifyica.engine.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.verifyica.api.Verifyica;

public class TagSupportTest {

    @Test
    public void testNoClassTags() {
        assertThat(TagSupport.getTags(TestClass1.class)).isNotNull();
        assertThat(TagSupport.getTags(TestClass1.class)).isEmpty();
    }

    @Test
    public void testClassTags() {
        Set<String> tags = TagSupport.getTags(TestClass2.class);

        assertThat(tags).isNotNull();
        assertThat(tags).hasSize(2);

        List<String> list = new ArrayList<>(tags);

        assertThat(list.get(0)).isEqualTo("tag1");
        assertThat(list.get(1)).isEqualTo("tag2");
    }

    @Test
    public void testClassTagTrimming() {
        Set<String> tags = TagSupport.getTags(TestClass3.class);

        assertThat(tags).isNotNull();
        assertThat(tags).hasSize(1);

        List<String> list = new ArrayList<>(tags);

        assertThat(list.get(0)).isEqualTo("tag1");
    }

    @Test
    public void testClassEmptyTag() {
        Set<String> tags = TagSupport.getTags(TestClass4.class);

        assertThat(tags).isNotNull();
        assertThat(tags).hasSize(0);
    }

    private static class TestClass1 {
        // INTENTIONALLY BLANK
    }

    @Verifyica.Tag(tag = "tag1")
    @Verifyica.Tag(tag = "tag2")
    private static class TestClass2 {
        // INTENTIONALLY BLANK
    }

    @Verifyica.Tag(tag = "\r\n\ttag1\r\n\t ")
    private static class TestClass3 {
        // INTENTIONALLY BLANK
    }

    @Verifyica.Tag(tag = "")
    private static class TestClass4 {
        // INTENTIONALLY BLANK
    }
}
