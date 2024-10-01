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

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Interface that contains all Verifyica annotations */
public @interface Verifyica {

    /** ArgumentSupplier annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface ArgumentSupplier {

        /**
         * Method to get parallelism
         *
         * @return parallelism
         */
        int parallelism() default 1;
    }

    /** Prepare annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Prepare {}

    /** BeforeAll annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface BeforeAll {}

    /** BeforeEach annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface BeforeEach {}

    /** Test annotation */
    @org.junit.platform.commons.annotation.Testable
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Test {}

    /** AfterEach annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface AfterEach {}

    /** AfterAll annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface AfterAll {}

    /** Conclude annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Conclude {}

    /** Order annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Order {

        /**
         * Order value
         *
         * @return the order value
         */
        int order();
    }

    /** Step annotation */
    /*
    @org.junit.platform.commons.annotation.Testable
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Step {

        /**
         * id value
         *
         * @return the id value
         */
    /*
    String id();

    /**
     * nextId value
     *
     * @return the nextId value
     */
    /*
        String nextId();
    }
    */

    /** Disabled annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Disabled {}

    /** Tag annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Tags.class)
    @interface Tag {

        /**
         * Tag value
         *
         * @return the tag value
         */
        String tag();
    }

    /** Tags annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Tags {

        /**
         * Tag values
         *
         * @return the tags
         */
        Tag[] value();
    }

    /** DisplayName annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface DisplayName {

        /**
         * DisplayName value
         *
         * @return the display name value
         */
        String name();
    }

    /** ClassInterceptorSupplier annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface ClassInterceptorSupplier {}

    /** Autowired annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Autowired {}

    /** Testable annotation */
    @org.junit.platform.commons.annotation.Testable
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Testable {}
}
