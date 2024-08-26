/*
 * Creative Commons Attribution-ShareAlike 3.0 Unported License
 *
 * Source: https://stackoverflow.com/questions/1883321/system-out-println-and-system-err-println-out-of-order
 * Author: https://stackoverflow.com/users/1217178/markus-a
 */

package org.antublue.verifyica.engine.common;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/** Class to implement Streams */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Streams {

    private static List<OutputStream> outputStreams = null;
    private static OutputStream lastOutputStream = null;

    /** Class to implement FixedStream */
    private static class FixedStream extends OutputStream {

        private final OutputStream outputStream;

        /**
         * Constructor
         *
         * @param outputStream outputStream
         */
        public FixedStream(OutputStream outputStream) {
            this.outputStream = outputStream;
            outputStreams.add(this);
        }

        @Override
        public void write(int b) throws IOException {
            if (lastOutputStream != this) {
                flushAndSwap();
            }
            outputStream.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            if (lastOutputStream != this) {
                flushAndSwap();
            }
            outputStream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (lastOutputStream != this) {
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
            if (lastOutputStream != null) {
                lastOutputStream.flush();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // INTENTIONALLY BLANK
                }
            }

            lastOutputStream = this;
        }
    }

    /**
     * Method to fix streams
     */
    public static void fix() {
        synchronized (Streams.class) {
            if (outputStreams == null) {
                outputStreams = new ArrayList<>();
                System.setErr(new PrintStream(new FixedStream(System.err)));
                System.setOut(new PrintStream(new FixedStream(System.out)));
            }
        }
    }
}
