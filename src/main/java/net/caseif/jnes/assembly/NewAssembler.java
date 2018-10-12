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

package net.caseif.jnes.assembly;

import net.caseif.jnes.assembly.lexer.AssemblyLexer;
import net.caseif.jnes.assembly.lexer.Token;
import net.caseif.jnes.assembly.parser.AssemblyParser;
import net.caseif.jnes.assembly.parser.Statement;
import net.caseif.jnes.util.exception.MalformedAssemblyException;

import com.google.common.base.Preconditions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class NewAssembler {

    private List<Statement> statements;

    public void read(InputStream input) throws IOException, MalformedAssemblyException {
        System.out.println("Lexing assembly...");

        List<List<Token>> tokenized = AssemblyLexer.lex(input);

        System.out.println("Parsing assembly...");

        statements = AssemblyParser.parse(tokenized);
    }

    public void assemble(OutputStream output) throws IOException, MalformedAssemblyException {
        Preconditions.checkState(statements != null, "No program loaded.");

        ByteArrayOutputStream intermediate = new ByteArrayOutputStream();

        final int OFFSET = 0x8000; //TODO: read this from a .org directive

        int pc = 0;

        for (Statement stmt : statements) {
            switch (stmt.getType()) {
                case INSTRUCTION: {
                    Statement.InstructionStatement instrStmt = (Statement.InstructionStatement) stmt;

                    //TODO

                    break;
                }
                case LABEL_DEF: {
                    Statement.LabelDefinitionStatement lblStmt = (Statement.LabelDefinitionStatement) stmt;

                    //TODO

                    break;
                }
                case COMMENT: {
                    continue;
                }
                default: {
                    throw new AssertionError("Unhandled case " + stmt.getType().name());
                }
            }
        }

        byte[] bytes = intermediate.toByteArray();

        output.write(bytes);

        output.close();
    }

}
