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

package net.caseif.jnes.disassembly;

import net.caseif.jnes.model.cpu.AddressingMode;
import net.caseif.jnes.model.cpu.Instruction;

import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import static net.caseif.jnes.util.IoHelper.toBuffer;

public class PrgDisassembler {

    private ByteBuffer prgBuf;

    public void read(InputStream input) throws IOException {
        prgBuf = toBuffer(input);
    }

    public void dump(OutputStream output) throws IOException {
        try (Writer writer = new OutputStreamWriter(output)) {
            writer.write(prgToString());
        }
    }

    public String prgToString() {
        StringBuilder sb = new StringBuilder();
        while (prgBuf.hasRemaining()) {
            try {
                byte opcode = prgBuf.get();
                Instruction instr = Instruction.fromOpcode(opcode);

                sb.append(instr.getOpcode().name());

                if (instr.getLength() > 1) {
                    sb.append(' ').append(formatValue(instr.getAddressingMode(), prgBuf));
                }

                sb.append('\n');
            } catch (BufferUnderflowException ex) {
                break;
            }
        }

        return sb.toString();
    }

    private String formatValue(AddressingMode mode, ByteBuffer buf) {
        switch (mode) {
            case IMM:
                return "#$" + String.format("%02X", buf.get());
            case ZRP:
                return "$" + String.format("%02X", buf.get());
            case ZPX:
                return "$" + String.format("%02X", buf.get()) + ",X";
            case ZPY:
                return "$" + String.format("%02X", buf.get()) + ",Y";
            case ABS:
                return "$" + String.format("%02X", buf.get()) + String.format("%02X", buf.get());
            case ABX:
                return "$" + String.format("%02X", buf.get()) + String.format("%02X", buf.get()) + ",X";
            case ABY:
                return "$" + String.format("%02X", buf.get()) + String.format("%02X", buf.get()) + ",Y";
            case IND:
                return "($" + String.format("%02X", buf.get()) + String.format("%02X", buf.get()) + ")";
            case IZX:
                return "($" + String.format("%02X", buf.get()) + ",X)";
            case IZY:
                return "($" + String.format("%02X", buf.get()) + "),Y";
            case REL:
                return "$" + String.format("%02X", buf.get());
            default:
                throw new AssertionError("Unhandled addressing mode " + mode.name());
        }
    }

}
