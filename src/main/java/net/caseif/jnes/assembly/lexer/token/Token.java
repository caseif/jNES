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

package net.caseif.jnes.assembly.lexer.token;

import net.caseif.jnes.model.cpu.Mnemonic;
import net.caseif.jnes.util.tuple.Pair;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public class Token {

    private static final Function<String, Integer> PARSE_HEX = v -> Integer.parseInt(v, 16);
    private static final Function<String, Integer> PARSE_DEC = v -> Integer.parseInt(v, 10);
    private static final Function<String, Integer> PARSE_BIN = v -> Integer.parseInt(v, 2);

    private static final Pattern RE_WHITESPACE      = Pattern.compile("^\\s+");
    private static final Pattern RE_MNEMONIC        = Pattern.compile("^([A-Z]{3})(?=\\s|$)");
    private static final Pattern RE_X               = Pattern.compile("^X");
    private static final Pattern RE_Y               = Pattern.compile("^Y");
    private static final Pattern RE_LABEL_DEF       = Pattern.compile("^([A-z][A-z0-9]*):");
    private static final Pattern RE_LABEL_REF       = Pattern.compile("^([A-z][A-z0-9]*)");
    private static final Pattern RE_HEX_QWORD       = Pattern.compile("^#\\$([0-9A-F]{8})");
    private static final Pattern RE_HEX_DWORD       = Pattern.compile("^#\\$([0-9A-F]{4})");
    private static final Pattern RE_HEX_WORD        = Pattern.compile("^#\\$([0-9A-F]{2})");
    private static final Pattern RE_DEC_WORD        = Pattern.compile("^#([0-9]{1,3})");
    private static final Pattern RE_BIN_QWORD       = Pattern.compile("^#%([01]{32})");
    private static final Pattern RE_BIN_DWORD       = Pattern.compile("^#%([01]{16})");
    private static final Pattern RE_BIN_WORD        = Pattern.compile("^#%([01]{8})");
    private static final Pattern RE_ADDR_DWORD      = Pattern.compile("^\\$([0-9A-F]{4})");
    private static final Pattern RE_ADDR_WORD       = Pattern.compile("^\\$([0-9A-F]{2})");
    private static final Pattern RE_COMMENT         = Pattern.compile("^;.*$");
    private static final Pattern RE_COMMA           = Pattern.compile("^,");
    private static final Pattern RE_LEFT_PAREN      = Pattern.compile("^\\(");
    private static final Pattern RE_RIGHT_PAREN     = Pattern.compile("^\\)");

    private static final ImmutableMap<Pattern, Type> PATTERN_MAP = ImmutableMap.<Pattern, Type>builder()
            .put(RE_COMMENT, Type.COMMENT)
            .put(RE_MNEMONIC, Type.MNEMONIC)
            .put(RE_X, Type.X)
            .put(RE_Y, Type.Y)
            .put(RE_LABEL_DEF, Type.LABEL_DEF)
            .put(RE_LABEL_REF, Type.LABEL_REF)
            .put(RE_HEX_QWORD, Type.HEX_QWORD)
            .put(RE_HEX_DWORD, Type.HEX_DWORD)
            .put(RE_HEX_WORD, Type.HEX_WORD)
            .put(RE_DEC_WORD, Type.DEC_WORD)
            .put(RE_BIN_QWORD, Type.BIN_QWORD)
            .put(RE_BIN_DWORD, Type.BIN_DWORD)
            .put(RE_BIN_WORD, Type.BIN_WORD)
            .put(RE_ADDR_DWORD, Type.ADDR_DWORD)
            .put(RE_ADDR_WORD, Type.ADDR_WORD)
            .put(RE_COMMA, Type.COMMA)
            .put(RE_LEFT_PAREN, Type.LEFT_PAREN)
            .put(RE_RIGHT_PAREN, Type.RIGHT_PAREN)
            .build();

    public static Optional<Pair<Token, Integer>> nextToken(String line, int pos) {
        int skipped = 0;

        String substr = line.substring(pos);

        Matcher wsMatcher = RE_WHITESPACE.matcher(substr);

        if (wsMatcher.find()) {
            skipped = wsMatcher.group(0).length();
            substr = substr.substring(skipped);
        }

        for (Map.Entry<Pattern, Type> e : PATTERN_MAP.entrySet()) {
            Matcher m = e.getKey().matcher(substr);

            if (!m.find()) {
                continue;
            }

            int len = m.group(0).length();

            Object val;

            if (m.groupCount() > 0) {
                try {
                    val = e.getValue().adaptValue(m.group(1));
                } catch (Throwable t) {
                    throw new IllegalArgumentException(String.format("Failed to adapt value %s for token type %s.",
                            m.group(1), e.getValue().name()), t);
                }
            } else {
                val = null;
            }

            return Optional.of(Pair.of(new Token(e.getValue(), val), len + skipped));
        }

        return Optional.empty();
    }

    private final Type type;
    private final Object val;

    private Token(Type type, @Nullable Object val) {
        this.type = type;
        this.val = val;
    }

    public Type getType() {
        return type;
    }

    public Optional<?> getValue() {
        return Optional.ofNullable(val);
    }

    public enum Type {
        LABEL_DEF,
        LABEL_REF,
        MNEMONIC(Mnemonic::valueOf),
        HEX_QWORD(PARSE_HEX),
        HEX_DWORD(PARSE_HEX),
        HEX_WORD(PARSE_HEX),
        DEC_WORD(PARSE_DEC),
        BIN_QWORD(PARSE_BIN),
        BIN_DWORD(PARSE_BIN),
        BIN_WORD(PARSE_BIN),
        ADDR_DWORD(PARSE_HEX),
        ADDR_WORD(PARSE_HEX),
        COMMENT,
        COMMA,
        X,
        Y,
        LEFT_PAREN,
        RIGHT_PAREN;

        private final Function<String, ?> valueAdapter;

        Type(Function<String, ?> valueAdapter) {
            this.valueAdapter = valueAdapter;
        }

        Type() {
            this(Functions.identity());
        }

        private Object adaptValue(String val) {
            return valueAdapter.apply(val);
        }
    }

}