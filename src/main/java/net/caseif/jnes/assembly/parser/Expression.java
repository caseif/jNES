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

import static com.google.common.base.Preconditions.checkArgument;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.ADDR_DWORD;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.ADDR_WORD;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.BIN_DWORD;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.BIN_QWORD;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.BIN_WORD;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.COMMA;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.COMMENT;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.DEC_WORD;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.HEX_DWORD;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.HEX_QWORD;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.HEX_WORD;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.LABEL_DEF;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.LABEL_REF;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.LEFT_PAREN;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.MNEMONIC;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.RIGHT_PAREN;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.X;
import static net.caseif.jnes.assembly.lexer.token.Token.Type.Y;

import net.caseif.jnes.assembly.lexer.token.Token;
import net.caseif.jnes.model.cpu.AddressingMode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import net.caseif.jnes.util.tuple.Pair;

class Expression<T> {

    private static final ImmutableMap<List<Token.Type>, TypeWithMetadata<?>> EXPRESSION_SYNTAXES = ImmutableMap.<List<Token.Type>, TypeWithMetadata<?>>builder()
            .put(ImmutableList.of(COMMENT),     TypeWithMetadata.of(Type.COMMENT))

            .put(ImmutableList.of(MNEMONIC),    TypeWithMetadata.of(Type.MNEMONIC))

            .put(ImmutableList.of(LABEL_DEF),   TypeWithMetadata.of(Type.LABEL_DEF))

            .put(ImmutableList.of(LABEL_REF),   TypeWithMetadata.of(Type.LABEL_REF))

            .put(ImmutableList.of(HEX_QWORD),   TypeWithMetadata.of(Type.IMM_VALUE, 4))
            .put(ImmutableList.of(BIN_QWORD),   TypeWithMetadata.of(Type.IMM_VALUE, 4))
            .put(ImmutableList.of(HEX_DWORD),   TypeWithMetadata.of(Type.IMM_VALUE, 2))
            .put(ImmutableList.of(BIN_DWORD),   TypeWithMetadata.of(Type.IMM_VALUE, 2))
            .put(ImmutableList.of(HEX_WORD),    TypeWithMetadata.of(Type.IMM_VALUE, 1))
            .put(ImmutableList.of(DEC_WORD),    TypeWithMetadata.of(Type.IMM_VALUE, 1))
            .put(ImmutableList.of(BIN_WORD),    TypeWithMetadata.of(Type.IMM_VALUE, 1))

            .put(ImmutableList.of(ADDR_DWORD, COMMA, X),                            TypeWithMetadata.of(Type.TARGET, AddressingMode.ABX))
            .put(ImmutableList.of(ADDR_DWORD, COMMA, Y),                            TypeWithMetadata.of(Type.TARGET, AddressingMode.ABY))
            .put(ImmutableList.of(ADDR_DWORD),                                      TypeWithMetadata.of(Type.TARGET, AddressingMode.ABS))
            .put(ImmutableList.of(ADDR_WORD, COMMA, X),                             TypeWithMetadata.of(Type.TARGET, AddressingMode.ZPX))
            .put(ImmutableList.of(ADDR_WORD, COMMA, Y),                             TypeWithMetadata.of(Type.TARGET, AddressingMode.ZPY))
            .put(ImmutableList.of(ADDR_WORD),                                       TypeWithMetadata.of(Type.TARGET, AddressingMode.ZRP))
            .put(ImmutableList.of(LEFT_PAREN, ADDR_DWORD, RIGHT_PAREN),             TypeWithMetadata.of(Type.TARGET, AddressingMode.IND))
            .put(ImmutableList.of(LEFT_PAREN, ADDR_WORD, COMMA, X, RIGHT_PAREN),    TypeWithMetadata.of(Type.TARGET, AddressingMode.IZX))
            .put(ImmutableList.of(LEFT_PAREN, ADDR_WORD, RIGHT_PAREN, COMMA, Y),    TypeWithMetadata.of(Type.TARGET, AddressingMode.IZY))

            .build();

    static Optional<Pair<Expression<?>, Integer>> nextExpression(List<Token> tokens) {
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Empty token list passed to nextExpression");
        }

        outer:
        for (Map.Entry<List<Token.Type>, TypeWithMetadata<?>> e : EXPRESSION_SYNTAXES.entrySet()) {
            Object value = null;

            for (int i = 0; i < e.getKey().size(); i++) {
                Token curToken = tokens.get(i);

                if (e.getKey().get(i) != curToken.getType()) {
                    continue outer;
                }

                if (curToken.getValue().isPresent()) {
                    assert value == null : "Found two values in expression with type " + e.getValue().getType().name() + ".";

                    value = curToken.getValue().get();
                }
            }

            // if we've gotten this far, we've found a match

            return Optional.of(Pair.of(new Expression<>(e.getValue(), value), e.getKey().size()));
        }

        return Optional.empty();
    }

    private final TypeWithMetadata<T> type;
    private final Object value;

    private Expression(TypeWithMetadata<T> type, @Nullable Object value) {
        this.type = type;
        this.value = value;
    }

    TypeWithMetadata<T> getType() {
        return type;
    }

    Object getValue() {
        return value;
    }

    enum Type {
        MNEMONIC,
        TARGET,
        IMM_VALUE,
        LABEL_DEF,
        LABEL_REF,
        COMMENT
    }

    static class TypeWithMetadata<T> {

        private static <T> TypeWithMetadata<T> of(Type type, T metadata) {
            return new TypeWithMetadata<>(type, metadata);
        }

        private static TypeWithMetadata<Void> of(Type type) {
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
