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

import static com.google.common.base.Preconditions.checkState;

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

public abstract class Statement {

    private final Type type;

    Statement(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
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

        Statement constructStatement(Object... values) {
            try {
                return ctor.newInstance(null, (Object[]) values);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public class InstructionStatement extends Statement {

        private final Mnemonic mnemonic;
        private final AddressingMode addrMode;
        private final int operand;
        private final int valueSize;

        InstructionStatement(Object[] values) {
            super(Type.INSTRUCTION);

            this.mnemonic = (Mnemonic) values[0];
            if (values.length == 3) {
                operand = (int) values[1];

                if (values[2] instanceof AddressingMode) {
                    addrMode = (AddressingMode) values[2];

                    valueSize = 0;
                } else {
                    valueSize = (int) values[2];

                    addrMode = AddressingMode.IMM;
                }
            } else {
                addrMode = AddressingMode.IMP;

                operand = 0;
                valueSize = 0;
            }
        }

        public Mnemonic getMnemonic() {
            return mnemonic;
        }

        public AddressingMode getAddressingMode() {
            return addrMode;
        }

        public int getOperand() {
            checkState(addrMode != AddressingMode.IMP, "Cannot get operand for implicit instruction.");

            return operand;
        }

        public int getImmediateValueSize() {
            checkState(addrMode == AddressingMode.IMM, "Cannot get immediate value size for non-immediate instruction.");

            return valueSize;
        }

    }

    public class LabelDefinitionStatement extends Statement {

        private final String id;

        LabelDefinitionStatement(Object[] values) {
            super(Type.LABEL_DEF);

            this.id = (String) values[0];
        }

        public String getId() {
            return id;
        }

    }

    public class CommentStatement extends Statement {

        CommentStatement(Object[] values) {
            super(Type.COMMENT);
        }

    }

}
