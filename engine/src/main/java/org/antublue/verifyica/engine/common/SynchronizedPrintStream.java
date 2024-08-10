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

package org.antublue.verifyica.engine.common;

import java.io.OutputStream;
import java.io.PrintStream;

public class SynchronizedPrintStream extends PrintStream {

    public SynchronizedPrintStream(OutputStream out) {
        super(out);
    }

    @Override
    public void flush() {
        synchronized (this) {
            super.flush();
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            super.close();
        }
    }

    @Override
    public boolean checkError() {
        synchronized (this) {
            return super.checkError();
        }
    }

    @Override
    protected void setError() {
        synchronized (this) {
            super.setError();
        }
    }

    @Override
    public void write(int b) {
        synchronized (this) {
            super.write(b);
        }
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        synchronized (this) {
            super.write(buf, off, len);
        }
    }

    @Override
    public void print(boolean b) {
        synchronized (this) {
            super.print(b);
        }
    }

    @Override
    public void print(char c) {
        synchronized (this) {
            super.print(c);
        }
    }

    @Override
    public void print(int i) {
        synchronized (this) {
            super.print(i);
        }
    }

    @Override
    public void print(long l) {
        synchronized (this) {
            super.print(l);
        }
    }

    @Override
    public void print(float f) {
        synchronized (this) {
            super.print(f);
        }
    }

    @Override
    public void print(double d) {
        synchronized (this) {
            super.print(d);
        }
    }

    @Override
    public void print(char[] s) {
        synchronized (this) {
            super.print(s);
        }
    }

    @Override
    public void print(String s) {
        synchronized (this) {
            super.print(s);
        }
    }

    @Override
    public void print(Object obj) {
        synchronized (this) {
            super.print(obj);
        }
    }

    @Override
    public void println() {
        synchronized (this) {
            super.println();
        }
    }

    @Override
    public void println(boolean x) {
        synchronized (this) {
            super.println(x);
        }
    }

    @Override
    public void println(char x) {
        synchronized (this) {
            super.println(x);
        }
    }

    @Override
    public void println(int x) {
        synchronized (this) {
            super.println(x);
        }
    }

    @Override
    public void println(long x) {
        synchronized (this) {
            super.println(x);
        }
    }

    @Override
    public void println(float x) {
        synchronized (this) {
            super.println(x);
        }
    }

    @Override
    public void println(double x) {
        synchronized (this) {
            super.println(x);
        }
    }

    @Override
    public void println(char[] x) {
        synchronized (this) {
            super.println(x);
        }
    }

    @Override
    public void println(String x) {
        synchronized (this) {
            super.println(x);
        }
    }

    @Override
    public void println(Object x) {
        synchronized (this) {
            super.println(x);
        }
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        synchronized (this) {
            return super.printf(format, args);
        }
    }

    @Override
    public PrintStream printf(java.util.Locale l, String format, Object... args) {
        synchronized (this) {
            return super.printf(l, format, args);
        }
    }

    @Override
    public PrintStream format(String format, Object... args) {
        synchronized (this) {
            return super.format(format, args);
        }
    }

    @Override
    public PrintStream format(java.util.Locale l, String format, Object... args) {
        synchronized (this) {
            return super.format(l, format, args);
        }
    }

    @Override
    public PrintStream append(CharSequence csq) {
        synchronized (this) {
            return super.append(csq);
        }
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        synchronized (this) {
            return super.append(csq, start, end);
        }
    }

    @Override
    public PrintStream append(char c) {
        synchronized (this) {
            return super.append(c);
        }
    }
}
