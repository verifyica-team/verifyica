/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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
         * @return the parallelism value
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
        int value();
    }

    /** Disabled annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Disabled {}

    /** Tag annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Tags.class)
    @interface Tag {

        /**
         * Tag value
         *
         * @return the tag value
         */
        String value();
    }

    /** Tags annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Tags {

        /**
         * Tag values
         *
         * @return the tags array
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
        String value();
    }

    /** ClassInterceptorSupplier annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface ClassInterceptorSupplier {}

    /** Autowired annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Autowired {}

    /** Testable annotation */
    @org.junit.platform.commons.annotation.Testable
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Testable {}

    /** Interface that contains all experimental Verifyica annotations */
    @interface Experimental {

        /** DependsOn annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
        @Retention(RetentionPolicy.RUNTIME)
        @Repeatable(DependsOns.class)
        @interface DependsOn {

            /**
             * Tag value
             *
             * @return the tag value
             */
            String value();
        }

        /** DependsOns annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface DependsOns {

            /**
             * DependsOn values
             *
             * @return the DependsOn array
             */
            DependsOn[] value();
        }
    }
}
