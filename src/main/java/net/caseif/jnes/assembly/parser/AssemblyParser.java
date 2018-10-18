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

import net.caseif.jnes.assembly.ExpressionPart;
import net.caseif.jnes.assembly.lexer.Token;
import net.caseif.jnes.model.cpu.AddressingMode;
import net.caseif.jnes.util.exception.ParserException;
import net.caseif.jnes.util.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AssemblyParser {

    private static final Map<Expression.TypeWithMetadata<?>, Set<ImmutableList<ExpressionPart>>> EXPRESSION_SYNTAXES = new HashMap<>();

    static {
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.COMMENT),                       COMMENT);

        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.MNEMONIC),                      MNEMONIC);

        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.LABEL_DEF),                     LABEL_DEF);

        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.LABEL_REF),                     LABEL_REF);

        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 4),                  HEX_QWORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 4),                  BIN_QWORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 2),                  HEX_DWORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 2),                  BIN_DWORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 1),                  HEX_WORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 1),                  DEC_WORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 1),                  BIN_WORD);

        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 4),                   HEX_QWORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 4),                   BIN_QWORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 2),                   HEX_DWORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 2),                   BIN_DWORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 1),                   HEX_WORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 1),                   DEC_WORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 1),                   BIN_WORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 2),                   LABEL_REF);

        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ABX),    ADDR_DWORD, COMMA, X);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ABY),    ADDR_DWORD, COMMA, Y);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ABS),    ADDR_DWORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ZPX),    ADDR_WORD, COMMA, X);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ZPY),    ADDR_WORD, COMMA, Y);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ZRP),    ADDR_WORD);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.IND),    LEFT_PAREN, ADDR_DWORD, RIGHT_PAREN);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.IZX),    LEFT_PAREN, ADDR_WORD, COMMA, X, RIGHT_PAREN);
        createSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.IZY),    LEFT_PAREN, ADDR_WORD, RIGHT_PAREN, COMMA, Y);
    }

    private static final ImmutableMap<List<Expression.Type>, Statement.Type> STATEMENT_SYNTAXES
            = ImmutableMap.<List<Expression.Type>, Statement.Type>builder()

            .put(ImmutableList.of(Expression.Type.COMMENT), Statement.Type.COMMENT)

            .put(ImmutableList.of(Expression.Type.LABEL_DEF), Statement.Type.LABEL_DEF)

            .put(ImmutableList.of(Expression.Type.MNEMONIC, Expression.Type.IMM_VALUE), Statement.Type.INSTRUCTION)
            .put(ImmutableList.of(Expression.Type.MNEMONIC, Expression.Type.LABEL_REF), Statement.Type.INSTRUCTION)
            .put(ImmutableList.of(Expression.Type.MNEMONIC, Expression.Type.TARGET), Statement.Type.INSTRUCTION)
            .put(ImmutableList.of(Expression.Type.MNEMONIC), Statement.Type.INSTRUCTION)

            .build();

    private static void createSyntax(Expression.TypeWithMetadata<?> expr, ExpressionPart... pattern) {
        EXPRESSION_SYNTAXES.computeIfAbsent(expr, k -> new HashSet<>()).add(ImmutableList.copyOf(pattern));
    }

    private static Optional<Pair<Expression<?>, Integer>> nextExpression(List<Token> tokens) {
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Empty token list passed to nextExpression");
        }

        for (Expression.TypeWithMetadata<?> expr : EXPRESSION_SYNTAXES.keySet()) {
            Optional<Pair<Expression<?>, Integer>> exprOpt = matchExpression(tokens, expr);

            if (exprOpt.isPresent()) {
                return exprOpt;
            }
        }

        return Optional.empty();
    }

    private static Optional<Pair<Expression<?>, Integer>> matchExpression(List<Token> currentTokens,
            Expression.TypeWithMetadata<?> goal) {
        if (!EXPRESSION_SYNTAXES.containsKey(goal)) {
            return Optional.empty();
        }

        for (ImmutableList<ExpressionPart> pattern : EXPRESSION_SYNTAXES.get(goal)) {
            Optional<Pair<Expression<?>, Integer>> exprOpt = tryMatchPattern(currentTokens, goal, pattern);

            if (exprOpt.isPresent()) {
                return exprOpt;
            }
        }

        return Optional.empty();
    }

    private static Optional<Pair<Expression<?>, Integer>> matchExpression(List<Token> currentTokens,
            Expression.Type goal) {
        for (Map.Entry<Expression.TypeWithMetadata<?>, Set<ImmutableList<ExpressionPart>>> e : EXPRESSION_SYNTAXES.entrySet()) {
            if (e.getKey().getType() != goal) {
                continue;
            }

            for (ImmutableList<ExpressionPart> pattern : e.getValue()) {
                Optional<Pair<Expression<?>, Integer>> exprOpt = tryMatchPattern(currentTokens, e.getKey(), pattern);

                if (exprOpt.isPresent()) {
                    return exprOpt;
                }
            }
        }

        return Optional.empty();
    }

    private static Optional<Pair<Expression<?>, Integer>> tryMatchPattern(List<Token> currentTokens,
            Expression.TypeWithMetadata<?> goal, List<ExpressionPart> pattern) {
        System.out.println("seeking expression " + goal.getType().name());
        System.out.println("  head: " + currentTokens.get(0).getType().name());

        if (currentTokens.size() < pattern.size()) {
            return Optional.empty();
        }

        Object value = null;

        for (int i = 0; i < pattern.size(); i++) {
            Token curToken = currentTokens.get(i);

            if (pattern.get(i) instanceof Token.Type) {
                // try to match the target token
                if (pattern.get(i) != curToken.getType()) {
                    // can't match, so the pattern is inapplicable
                    return Optional.empty();
                }
            } else {
                // recursively try to match the target expression
                if (!matchExpression(currentTokens, ((Expression.TypeWithMetadata<?>) pattern.get(i))).isPresent()) {
                    // can't match, so the pattern is inapplicable
                    return Optional.empty();
                }
            }

            if (curToken.getValue().isPresent()) {
                assert value == null : "Found two values in expression with type " + goal.getType().name() + ".";

                value = curToken.getValue().get();
            }
        }

        // if we've gotten this far, we've found a match

        System.out.println("  Matched " + goal.getType().name() + "!");
        return Optional.of(Pair.of(new Expression<>(goal, value), pattern.size()));
    }

    private static Optional<Pair<Statement, Integer>> nextStatement(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return Optional.empty();
        }

        for (Map.Entry<List<Expression.Type>, Statement.Type> e : STATEMENT_SYNTAXES.entrySet()) {
            Optional<Pair<Statement, Integer>> stmtOpt = matchStatement(tokens, e.getValue(), e.getKey());

            if (stmtOpt.isPresent()) {
                return stmtOpt;
            }
        }

        return Optional.empty();
    }

    private static Optional<Pair<Statement, Integer>> matchStatement(List<Token> curTokens,
            Statement.Type goal, List<Expression.Type> pattern) {
        curTokens = new ArrayList<>(curTokens);

        List<Object> values = new ArrayList<>();

        int lenInTokens = 0;

        for (Expression.Type nextExpr : pattern) {
            Optional<Pair<Expression<?>, Integer>> exprOpt = matchExpression(curTokens, nextExpr);

            if (exprOpt.isPresent()) {
                lenInTokens += exprOpt.get().second();

                if (exprOpt.get().first().getValue() != null) {
                    values.add(exprOpt.get().first().getValue());
                }

                if (exprOpt.get().first().getType().getMetadata() != null) {
                    values.add(exprOpt.get().first().getValue());
                }
            } else {
                // this pattern is a bust
                return Optional.empty();
            }
        }

        // if we've gotten this far, we've found a match

        return Optional.of(Pair.of(goal.constructStatement(values.toArray()), lenInTokens));
    }

    public static List<Statement> parse(List<Token> tokens) throws ParserException {
        return parseToStatements(tokens);
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

    private static List<Statement> parseToStatements(List<Token> tokens) throws ParserException {
        List<Statement> stmts = new ArrayList<>();

        int curLine = 1;

        while (!tokens.isEmpty()) {
            Optional<Pair<Statement, Integer>> res = nextStatement(tokens);

            if (!res.isPresent()) {
                throw new ParserException(curLine);
            }

            stmts.add(res.get().first());

            tokens = tokens.subList(res.get().second(), tokens.size());

            curLine++;
        }

        return stmts;
    }

}
