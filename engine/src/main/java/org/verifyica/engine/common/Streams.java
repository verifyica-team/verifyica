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

package org.verifyica.engine.common;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/** Class to implement Streams */
public class Streams {

    private static boolean fixed = false;

    /** Constructor */
    private Streams() {
        // INTENTIONALLY BLANK
    }

    /** Method to fix streams */
    public static void fix() {
        synchronized (Streams.class) {
            if (!fixed) {
                // System.setErr(new SynchronizedPrintStream(System.err));
                // System.setOut(new SynchronizedPrintStream(System.out));
                fixed = true;
            }
        }
    }

    /**
     * Class to implement SynchronizedPrintStream
     */
    public static class SynchronizedPrintStream extends PrintStream {

        private static final Object LOCK = new Object();

        private final PrintStream delegate;
        private final ThreadLocal<ByteArrayOutputStream> threadLocalByteArrayOutputStream =
                ThreadLocal.withInitial(ByteArrayOutputStream::new);

        /**
         * Constructor
         *
         * @param delegate delegate
         */
        public SynchronizedPrintStream(PrintStream delegate) {
            super(delegate, true);
            this.delegate = delegate;
        }

        @Override
        public synchronized void write(byte[] buffer, int offset, int length) {
            ByteArrayOutputStream byteArrayOutputStream = threadLocalByteArrayOutputStream.get();
            byteArrayOutputStream.write(buffer, offset, length);
            if (new String(buffer, offset, length).contains(System.lineSeparator())) {
                flush();
            }
        }

        @Override
        public synchronized void flush() {
            ByteArrayOutputStream byteArrayOutputStream = threadLocalByteArrayOutputStream.get();
            synchronized (LOCK) {
                delegate.write(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());
                delegate.flush();
            }
            byteArrayOutputStream.reset();
        }
    }
}
