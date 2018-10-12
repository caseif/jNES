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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.caseif.jnes.model.cpu.AddressingMode;
import net.caseif.jnes.model.cpu.Mnemonic;
import net.caseif.jnes.util.tuple.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Statement {

    private static final ImmutableMap<List<Expression.Type>, Type> STATEMENT_SYNTAXES = ImmutableMap.<List<Expression.Type>, Type>builder()
            .put(ImmutableList.of(Expression.Type.COMMENT), Type.COMMENT)

            .put(ImmutableList.of(Expression.Type.LABEL_DEF), Type.LABEL_DEF)

            .put(ImmutableList.of(Expression.Type.MNEMONIC, Expression.Type.IMM_VALUE), Type.INSTRUCTION)
            .put(ImmutableList.of(Expression.Type.MNEMONIC, Expression.Type.LABEL_REF), Type.INSTRUCTION)
            .put(ImmutableList.of(Expression.Type.MNEMONIC, Expression.Type.TARGET), Type.INSTRUCTION)
            .put(ImmutableList.of(Expression.Type.MNEMONIC), Type.INSTRUCTION)

            .build();

    static Optional<Pair<Statement, Integer>> nextStatement(List<Expression> exprs) {
        if (exprs.isEmpty()) {
            return Optional.empty();
        }

        outer:
        for (Map.Entry<List<Expression.Type>, Type> e : STATEMENT_SYNTAXES.entrySet()) {
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

    enum Type {
        INSTRUCTION(InstructionStatement.class),
        LABEL_DEF(LabelDefinitionStatement.class),
        COMMENT(CommentStatement.class);

        private final Constructor<? extends Statement> ctor;

        Type(Class<? extends Statement> clazz) {
            try {
                this.ctor = clazz.getDeclaredConstructor(Statement.class, Object[].class);
            } catch (NoSuchMethodException ex) {
                throw new AssertionError(String.format(
                        "Supplied class %s for type %s does not have an appropriate constructor.",
                        clazz.getName(),
                        name()
                ));
            }
        }

        private Statement constructStatement(Object... values) {
            try {
                return ctor.newInstance(null, (Object[]) values);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class InstructionStatement extends Statement {

        private final Mnemonic mnemonic;
        private final AddressingMode addrMode;
        private final int targetAddr;
        private final int immValue;
        private final int valueSize;

        InstructionStatement(Object[] values) {
            this.mnemonic = (Mnemonic) values[0];
            if (values.length == 3) {
                if (values[2] instanceof AddressingMode) {
                    targetAddr = (int) values[1];
                    addrMode = (AddressingMode) values[2];

                    immValue = -1;
                    valueSize = 0;
                } else {
                    immValue = (int) values[1];
                    valueSize = (int) values[2];

                    targetAddr = -1;
                    addrMode = AddressingMode.IMM;
                }
            } else {
                addrMode = AddressingMode.IMP;

                targetAddr = -1;
                immValue = -1;
                valueSize = 0;
            }
        }

        Mnemonic getMnemonic() {
            return mnemonic;
        }

        AddressingMode getAddressingMode() {
            return addrMode;
        }

        int getTargetAddress() {
            return targetAddr;
        }

        int getImmediateValue() {
            return immValue;
        }

        int getValueSize() {
            return valueSize;
        }

    }

    public class LabelDefinitionStatement extends Statement {

        private final String id;

        LabelDefinitionStatement(Object[] values) {
            this.id = (String) values[0];
        }

        String getId() {
            return id;
        }

    }

    public class CommentStatement extends Statement {

        CommentStatement(Object[] values) {
        }

    }

}
