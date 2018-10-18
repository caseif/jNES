/*
 * This file is a part of jNES.
 * Copyright (c) 2018, Max Roncace <mproncace@gmail.com>
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.caseif.jnes.assembly.parser;

import net.caseif.jnes.assembly.ExpressionPart;

import javax.annotation.Nullable;

class Expression<T> {

    private final TypeWithMetadata<T> type;
    private final Object value;
    private final int line;

    Expression(TypeWithMetadata<T> type, @Nullable Object value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    TypeWithMetadata<T> getType() {
        return type;
    }

    Object getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    enum Type implements ExpressionPart {
        MNEMONIC,
        TARGET,
        IMM_VALUE,
        LABEL_DEF,
        LABEL_REF,
        QWORD,
        DWORD,
        WORD,
        NUMBER,
        CONSTANT,
        COMMENT
    }

    static class TypeWithMetadata<T> {

        static <T> TypeWithMetadata<T> of(Type type, T metadata) {
            return new TypeWithMetadata<>(type, metadata);
        }

        static TypeWithMetadata<Void> of(Type type) {
            return of(type, null);
        }

        private final Type type;
        private final T metadata;

        private TypeWithMetadata(Type type, @Nullable T metadata) {
            this.type = type;
            this.metadata = metadata;
        }

        Type getType() {
            return type;
        }

        T getMetadata() {
            return metadata;
        }
    }

}
