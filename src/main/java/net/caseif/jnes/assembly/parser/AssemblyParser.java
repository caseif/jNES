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

import net.caseif.jnes.assembly.lexer.token.Token;
import net.caseif.jnes.util.exception.ParserException;
import net.caseif.jnes.util.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssemblyParser {

    public static List<Statement> parse(List<List<Token>> tokens) throws ParserException {
        return parseToStatements(parseToExpressions(tokens));
    }

    private static List<Expression> parseToExpressions(List<List<Token>> tokens) throws ParserException {
        List<Expression> exprs = new ArrayList<>();

        int curLine = 1;

        for (List<Token> line : tokens) {
            while (!line.isEmpty()) {
                Optional<Pair<Expression<?>, Integer>> res = Expression.nextExpression(line);

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
            Optional<Pair<Statement, Integer>> res = Statement.nextStatement(exprs);

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
