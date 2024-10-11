/*
 * Creative Commons Attribution-ShareAlike 3.0 Unported License
 *
 * Source: https://stackoverflow.com/questions/1883321/system-out-println-and-system-err-println-out-of-order
 * Author: https://stackoverflow.com/users/1217178/markus-a
 */

/*
 * Modifications Copyright (C) 2024-present Verifyica project authors and contributors
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/** Class to implement Streams */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Streams {

    private static List<OutputStream> OUTPUT_STREAMS = null;
    private static OutputStream LAST_OUTPUT_STREAM = null;

    /** Constructor */
    private Streams() {
        // INTENTIONALLY BLANK
    }

    /** Method to fix streams */
    public static void fix() {
        synchronized (Streams.class) {
            if (OUTPUT_STREAMS == null) {
                OUTPUT_STREAMS = new ArrayList<>();
                System.setErr(new PrintStream(new FixedStream(System.err)));
                System.setOut(new PrintStream(new FixedStream(System.out)));
            }
        }
    }

    /** Class to implement FixedStream */
    private static class FixedStream extends OutputStream {

        private final OutputStream outputStream;

        /**
         * Constructor
         *
         * @param outputStream outputStream
         */
        private FixedStream(OutputStream outputStream) {
            this.outputStream = outputStream;
            OUTPUT_STREAMS.add(this);
        }

        @Override
        public void write(int b) throws IOException {
            if (LAST_OUTPUT_STREAM != this) {
                flushAndSwap();
            }
            outputStream.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            if (LAST_OUTPUT_STREAM != this) {
                flushAndSwap();
            }
            outputStream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (LAST_OUTPUT_STREAM != this) {
                flushAndSwap();
            }
            outputStream.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            outputStream.flush();
        }

        @Override
        public void close() throws IOException {
            outputStream.close();
        }

        /**
         * Method to flush the last stream and swap outputStream
         *
         * @throws IOException IOException
         */
        private void flushAndSwap() throws IOException {
            if (LAST_OUTPUT_STREAM != null) {
                LAST_OUTPUT_STREAM.flush();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // INTENTIONALLY BLANK
                }
            }

            LAST_OUTPUT_STREAM = this;
        }
    }
}
