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
import static net.caseif.jnes.assembly.lexer.Token.Type.POUND;
import static net.caseif.jnes.assembly.lexer.Token.Type.RIGHT_PAREN;
import static net.caseif.jnes.assembly.lexer.Token.Type.X;
import static net.caseif.jnes.assembly.lexer.Token.Type.Y;

import net.caseif.jnes.assembly.ExpressionPart;
import net.caseif.jnes.assembly.lexer.Token;
import net.caseif.jnes.model.cpu.AddressingMode;

import com.google.common.collect.ImmutableList;
import net.caseif.jnes.util.exception.ParserException;
import net.caseif.jnes.util.tuple.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AssemblyParser {

    private static final Map<Expression.TypeWithMetadata<?>, Set<ImmutableList<ExpressionPart>>> EXPRESSION_SYNTAXES = new LinkedHashMap<>();
    private static final Map<Statement.Type, Set<ImmutableList<Expression.Type>>> STATEMENT_SYNTAXES = new LinkedHashMap<>();

    static {
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.COMMENT),                       COMMENT);

        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.MNEMONIC),                      MNEMONIC);

        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.LABEL_DEF),                     LABEL_DEF);

        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.LABEL_REF),                     LABEL_REF);

        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ABX),    HEX_DWORD, COMMA, X);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ABY),    HEX_DWORD, COMMA, Y);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ABS),    HEX_DWORD);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ZPX),    HEX_WORD, COMMA, X);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ZPY),    HEX_WORD, COMMA, Y);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.ZRP),    HEX_WORD);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.IND),    LEFT_PAREN, HEX_DWORD, RIGHT_PAREN);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.IZX),    LEFT_PAREN, HEX_WORD, COMMA, X, RIGHT_PAREN);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.TARGET, AddressingMode.IZY),    LEFT_PAREN, HEX_WORD, RIGHT_PAREN, COMMA, Y);

        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 4),                   HEX_QWORD);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 4),                   BIN_QWORD);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 2),                   HEX_DWORD);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 2),                   BIN_DWORD);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 1),                   HEX_WORD);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 1),                   DEC_WORD);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 1),                   BIN_WORD);
        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.CONSTANT, 2),                   LABEL_REF);

        createExpressionSyntax(Expression.TypeWithMetadata.of(Expression.Type.IMM_VALUE, 4),                  POUND, Expression.Type.CONSTANT);

        createStatementSyntax(Statement.Type.COMMENT, Expression.Type.COMMENT);
        createStatementSyntax(Statement.Type.LABEL_DEF, Expression.Type.LABEL_DEF);
        createStatementSyntax(Statement.Type.INSTRUCTION, Expression.Type.MNEMONIC, Expression.Type.IMM_VALUE);
        createStatementSyntax(Statement.Type.INSTRUCTION, Expression.Type.MNEMONIC, Expression.Type.LABEL_REF);
        createStatementSyntax(Statement.Type.INSTRUCTION, Expression.Type.MNEMONIC, Expression.Type.TARGET);
        createStatementSyntax(Statement.Type.INSTRUCTION, Expression.Type.MNEMONIC);
    }

    private static void createExpressionSyntax(Expression.TypeWithMetadata<?> expr, ExpressionPart... pattern) {
        EXPRESSION_SYNTAXES.computeIfAbsent(expr, k -> new LinkedHashSet<>()).add(ImmutableList.copyOf(pattern));
    }

    private static void createStatementSyntax(Statement.Type stmt, Expression.Type... pattern) {
        STATEMENT_SYNTAXES.computeIfAbsent(stmt, k -> new LinkedHashSet<>()).add(ImmutableList.copyOf(pattern));
    }

    public static List<Statement> parse(List<Token> tokens) throws ParserException {
        List<Statement> stmts = new ArrayList<>();

        while (tokens.size() > 0) {
            // try to match the next available statement
            Pair<Statement, Integer> res = matchNextStatement(tokens);

            // add it to the list
            stmts.add(res.first());

            // shift the head of the token list past the ones we've already parsed
            tokens = tokens.subList(res.second(), tokens.size());
        }

        return stmts;
    }

    // matches whatever statement can be found next
    private static Pair<Statement, Integer> matchNextStatement(List<Token> curTokens) throws ParserException {
        for (Statement.Type goal : STATEMENT_SYNTAXES.keySet()) {
            // try to match against a specific goal
            Optional<Pair<Statement, Integer>> res = matchStatement(curTokens, goal);

            // check if we found a valid statement
            if (res.isPresent()) {
                return res.get();
            }
        }

        // no statements matched
        throw new ParserException("Failed to match any statement.", curTokens.get(0).getLine());
    }

    // matches a token list against a specific statement type
    private static Optional<Pair<Statement, Integer>> matchStatement(List<Token> curTokens, Statement.Type goal) {
        for (List<Expression.Type> pattern : STATEMENT_SYNTAXES.get(goal)) {
            // try to match against a specific pattern specified by this goal
            Optional<Pair<Statement, Integer>> res = matchStatementWithPattern(curTokens, goal, pattern);

            // check if we found a valid statement
            if (res.isPresent()) {
                return res;
            }
        }

        // we didn't find anything valid so just return empty
        return Optional.empty();
    }

    // matches a token list against a specific statement type AND pattern
    private static Optional<Pair<Statement, Integer>> matchStatementWithPattern(List<Token> curTokens,
            Statement.Type goal, List<Expression.Type> pattern) {
        // track the token count so we can return it to the caller
        int tokenCount = 0;

        int line = -1;

        // values obtained from the expressions constituating the statement
        List<Object> values = new ArrayList<>();

        for (Expression.Type nextExpr : pattern) {
            Optional<Pair<Expression<?>, Integer>> res = matchExpression(curTokens, nextExpr);

            // if empty, this pattern doesn't work
            if (!res.isPresent()) {
                return Optional.empty();
            }

            // add the values from the expression we found
            if (res.get().first().getValue() != null) {
                values.add(res.get().first().getValue());
            }

            // add the metadata value too since we need it later
            if (res.get().first().getType().getMetadata() != null) {
                values.add(res.get().first().getType().getMetadata());
            }

            // set the statement's line number if we haven't already
            if (line == -1) {
                line = res.get().first().getLine();
            }

            // update the token count
            tokenCount += res.get().second();
            // adjust the token list to account for the ones we just consumed
            curTokens = curTokens.subList(res.get().second(), curTokens.size());
        }

        System.out.println("Line " + line + ": Matched statement " + goal.name() + pattern + "(v:" + values + ")");

        return Optional.of(Pair.of(goal.constructStatement(line, values.toArray()), tokenCount));
    }

    // matches a token list against a specific expression
    private static Optional<Pair<Expression<?>, Integer>> matchExpression(List<Token> curTokens, Expression.Type goal) {
        // we have to do it this way since the map stores types _with metadata_ as keys
        for (Map.Entry<Expression.TypeWithMetadata<?>, Set<ImmutableList<ExpressionPart>>> e : EXPRESSION_SYNTAXES.entrySet()) {
            if (e.getKey().getType() != goal) {
                // skip since it's the wrong type
                continue;
            }

            for (ImmutableList<ExpressionPart> pattern : e.getValue()) {
                Optional<Pair<Expression<?>, Integer>> res = matchExpressionWithPattern(curTokens, e.getKey(), pattern);

                if (res.isPresent()) {
                    return res;
                }
            }
        }

        // we didn't find any valid expressions
        return Optional.empty();
    }

    // matches a token list against a specific expression AND pattern
    private static Optional<Pair<Expression<?>, Integer>> matchExpressionWithPattern(List<Token> curTokens,
            Expression.TypeWithMetadata<?> goal, List<ExpressionPart> pattern) {
        // track the token count so we can return it to the caller
        int tokenCount = 0;

        Object value = null;

        int line = -1;

        for (ExpressionPart nextPart : pattern) {
            if (nextPart instanceof Token.Type) {
                // if the next token isn't what we expect, then the pattern fails
                if (curTokens.get(0).getType() != nextPart) {
                    return Optional.empty();
                }

                // set the value, if applicable
                if (curTokens.get(0).getValue().isPresent()) {
                    value = curTokens.get(0).getValue().get();
                }

                // set the expression's line number if we haven't already
                if (line == -1) {
                    line = curTokens.get(0).getLine();
                }

                // increment the token count since we just consumed the head
                tokenCount++;
                // update the token list as well
                curTokens = curTokens.subList(1, curTokens.size());
            } else { // it's a recursive expression
                Optional<Pair<Expression<?>, Integer>> res = matchExpression(curTokens, (Expression.Type) nextPart);

                // if we can't match the expression, the pattern fails
                if (!res.isPresent()) {
                    return Optional.empty();
                }

                // set the value, if applicable
                if (res.get().first().getValue() != null) {
                    value = res.get().first().getValue();
                }

                // set the expression's line number if we haven't already
                if (line == -1) {
                    line = res.get().first().getLine();
                }

                // update the token count to account for however many we just consumed
                tokenCount += res.get().second();
                // update the token list as well
                curTokens = curTokens.subList(res.get().second(), curTokens.size());
            }
        }

        System.out.println("Line " + line + ": Matched expression " + goal.getType().name() + "(md:" + goal.getMetadata() + ")(v:" + value + ")");

        return Optional.of(Pair.of(new Expression<>(goal, value, line), tokenCount));
    }

}
