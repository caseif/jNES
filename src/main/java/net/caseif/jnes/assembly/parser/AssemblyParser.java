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

import static net.caseif.jnes.assembly.lexer.Token.Type.ADDR_DWORD;
import static net.caseif.jnes.assembly.lexer.Token.Type.ADDR_WORD;
import static net.caseif.jnes.assembly.lexer.Token.Type.BIN_DWORD;
import static net.caseif.jnes.assembly.lexer.Token.Type.BIN_QWORD;
import static net.caseif.jnes.assembly.lexer.Token.Type.BIN_WORD;
import static net.caseif.jnes.assembly.lexer.Token.Type.COMMA;
import static net.caseif.jnes.assembly.lexer.Token.Type.COMMENT;
import static net.caseif.jnes.assembly.lexer.Token.Type.DEC_WORD;
import static net.caseif.jnes.assembly.lexer.Token.Type.HEX_DWORD;
import static net.caseif.jnes.assembly.lexer.Token.Type.HEX_QWORD;
import static net.caseif.jnes.assembly.lexer.Token.Type.HEX_WORD;
import static net.caseif.jnes.assembly.lexer.Token.Type.LABEL_DEF;
import static net.caseif.jnes.assembly.lexer.Token.Type.LABEL_REF;
import static net.caseif.jnes.assembly.lexer.Token.Type.LEFT_PAREN;
import static net.caseif.jnes.assembly.lexer.Token.Type.MNEMONIC;
import static net.caseif.jnes.assembly.lexer.Token.Type.RIGHT_PAREN;
import static net.caseif.jnes.assembly.lexer.Token.Type.X;
import static net.caseif.jnes.assembly.lexer.Token.Type.Y;

import net.caseif.jnes.assembly.lexer.Token;
import net.caseif.jnes.model.cpu.AddressingMode;
import net.caseif.jnes.util.exception.ParserException;
import net.caseif.jnes.util.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AssemblyParser {

    private static final ImmutableMap<List<Token.Type>, Expression.TypeWithMetadata<?>> EXPRESSION_SYNTAXES
            = ImmutableMap.<List<Token.Type>, Expression.TypeWithMetadata<?>>builder()

            .put(ImmutableList.of(COMMENT),     Expression.TypeWithMetadata.of(Expression.Type.COMMENT))

            .put(ImmutableList.of(MNEMONIC),    Expression.TypeWithMetadata.of(Expression.Type.MNEMONIC))

            .put(ImmutableList.of(LABEL_DEF),   Expression.TypeWithMetadata.of(Expression.Type.LABEL_DEF))

            .put(ImmutableList.of(LABEL_REF),   Expression.TypeWithMetadata.of(Expression.Type.LABEL_REF))

            .put(ImmutableList.of(HEX_QWORD),   Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 4))
            .put(ImmutableList.of(BIN_QWORD),   Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 4))
            .put(ImmutableList.of(HEX_DWORD),   Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 2))
            .put(ImmutableList.of(BIN_DWORD),   Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 2))
            .put(ImmutableList.of(HEX_WORD),    Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 1))
            .put(ImmutableList.of(DEC_WORD),    Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 1))
            .put(ImmutableList.of(BIN_WORD),    Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 1))

            .put(ImmutableList.of(ADDR_DWORD, COMMA, X),                            Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ABX))
            .put(ImmutableList.of(ADDR_DWORD, COMMA, Y),                            Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ABY))
            .put(ImmutableList.of(ADDR_DWORD),                                      Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ABS))
            .put(ImmutableList.of(ADDR_WORD, COMMA, X),                             Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ZPX))
            .put(ImmutableList.of(ADDR_WORD, COMMA, Y),                             Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ZPY))
            .put(ImmutableList.of(ADDR_WORD),                                       Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ZRP))
            .put(ImmutableList.of(LEFT_PAREN, ADDR_DWORD, RIGHT_PAREN),             Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.IND))
            .put(ImmutableList.of(LEFT_PAREN, ADDR_WORD, COMMA, X, RIGHT_PAREN),    Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.IZX))
            .put(ImmutableList.of(LEFT_PAREN, ADDR_WORD, RIGHT_PAREN, COMMA, Y),    Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.IZY))

            .build();

    private static final ImmutableMap<List<Expression.Type>, Statement.Type> STATEMENT_SYNTAXES
            = ImmutableMap.<List<Expression.Type>, Statement.Type>builder()

            .put(ImmutableList.of(Expression.Type.COMMENT), Statement.Type.COMMENT)

            .put(ImmutableList.of(Expression.Type.LABEL_DEF), Statement.Type.LABEL_DEF)

            .put(ImmutableList.of(Expression.Type.MNEMONIC, Expression.Type.IMM_VALUE), Statement.Type.INSTRUCTION)
            .put(ImmutableList.of(Expression.Type.MNEMONIC, Expression.Type.LABEL_REF), Statement.Type.INSTRUCTION)
            .put(ImmutableList.of(Expression.Type.MNEMONIC, Expression.Type.TARGET), Statement.Type.INSTRUCTION)
            .put(ImmutableList.of(Expression.Type.MNEMONIC), Statement.Type.INSTRUCTION)

            .build();


    private static Optional<Pair<Expression<?>, Integer>> nextExpression(List<Token> tokens) {
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Empty token list passed to nextExpression");
        }

        outer:
        for (Map.Entry<List<Token.Type>, Expression.TypeWithMetadata<?>> e : EXPRESSION_SYNTAXES.entrySet()) {
            if (tokens.size() < e.getKey().size()) {
                continue;
            }

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

    private static Optional<Pair<Statement, Integer>> nextStatement(List<Expression> exprs) {
        if (exprs.isEmpty()) {
            return Optional.empty();
        }

        outer:
        for (Map.Entry<List<Expression.Type>, Statement.Type> e : STATEMENT_SYNTAXES.entrySet()) {
            List<Object> values = new ArrayList<>();

            for (int i = 0; i < e.getKey().size(); i++) {
                Expression curExpr = exprs.get(i);

                if (e.getKey().get(i) != curExpr.getType().getType()) {
                    continue outer;
                }

                if (curExpr.getValue() != null) {
                    values.add(curExpr.getValue());
                }

                if (curExpr.getType().getMetadata() != null) {
                    values.add(curExpr.getType().getMetadata());
                }
            }

            // if we've gotten this far, we've found a match

            return Optional.of(Pair.of(e.getValue().constructStatement(values.toArray()), e.getKey().size()));
        }

        return Optional.empty();
    }

    public static List<Statement> parse(List<List<Token>> tokens) throws ParserException {
        return parseToStatements(parseToExpressions(tokens));
    }

    private static List<Expression> parseToExpressions(List<List<Token>> tokens) throws ParserException {
        List<Expression> exprs = new ArrayList<>();

        int curLine = 1;

        for (List<Token> line : tokens) {
            while (!line.isEmpty()) {
                Optional<Pair<Expression<?>, Integer>> res = nextExpression(line);

                if (!res.isPresent()) {
                    throw new ParserException(curLine);
                }

                exprs.add(res.get().first());

                line = line.subList(res.get().second(), line.size());
            }

            curLine++;
        }

        return exprs;
    }

    private static List<Statement> parseToStatements(List<Expression> exprs) throws ParserException {
        List<Statement> stmts = new ArrayList<>();

        int curLine = 1;

        while (!exprs.isEmpty()) {
            Optional<Pair<Statement, Integer>> res = nextStatement(exprs);

            if (!res.isPresent()) {
                throw new ParserException(curLine);
            }

            stmts.add(res.get().first());

            exprs = exprs.subList(res.get().second(), exprs.size());

            curLine++;
        }

        return stmts;
    }

}
