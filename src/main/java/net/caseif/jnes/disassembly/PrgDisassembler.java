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

import com.google.common.collect.ImmutableSet;
import net.caseif.jnes.model.cpu.AddressingMode;
import net.caseif.jnes.model.cpu.Instruction;
import net.caseif.jnes.model.cpu.Opcode;

import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.caseif.jnes.util.IoHelper.toBuffer;

public class PrgDisassembler {

    private static final Set<Opcode> BRANCH_INSTRS = ImmutableSet.of(
            Opcode.BCC, Opcode.BCS, Opcode.BEQ, Opcode.BMI, Opcode.BNE, Opcode.BPL, Opcode.BVC, Opcode.BVS
    );

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
        // maps PRG locations to intermediate label indices
        Map<Integer, Integer> intermediateLabelMap = new HashMap<>();

        // maps PRG locations to the intermediate index of the label they reference
        Map<Integer, Integer> labelReferences = new HashMap<>();

        int lineCount = 0;

        // intermediate buffer for generated assembly code
        List<String> lines = new ArrayList<>();

        int nextLabelIndex = 0;

        // convert PRG to assembly code, mostly
        while (prgBuf.hasRemaining()) {
            try {
                StringBuilder sb = new StringBuilder();

                byte opcode = prgBuf.get();

                Instruction instr = Instruction.fromOpcode(opcode);

                sb.append(instr.getOpcode().name());

                if (instr.getLength() > 1) {
                    sb.append(' ');

                    // handle it specially since we have to generate a label
                    //
                    // we generate intermediate label indices based on the order
                    // we discover them, then reorganize them later
                    if (BRANCH_INSTRS.contains(instr.getOpcode())) {
                        // generate a placeholder
                        sb.append("{{").append(nextLabelIndex).append("}}");

                        assert instr.getAddressingMode() == AddressingMode.REL;

                        byte offset = prgBuf.get();

                        boolean newLabel = intermediateLabelMap.putIfAbsent(lineCount + offset, nextLabelIndex) == null;

                        // store the reference so we can replace it later
                        labelReferences.put(lineCount, nextLabelIndex);

                        if (newLabel) {
                            nextLabelIndex++;
                        }
                    } else {
                        sb.append(formatValue(instr.getAddressingMode(), prgBuf));
                    }
                }

                lines.add(sb.toString());
            } catch (BufferUnderflowException ex) {
                break;
            }

            lineCount++;
        }

        // now we loop through the labels we discovered and reorganize them by the PRG location they reference

        // maps PRG locations to label indices
        Map<Integer, Integer> labelMap = new HashMap<>();

        int nextLabelToAssign = 0;

        // maps intermediate label indices to their final values
        Map<Integer, Integer> intermediateToReal = new HashMap<>();

        for (Map.Entry<Integer, Integer> e : intermediateLabelMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList())) {
            labelMap.put(e.getKey(), nextLabelToAssign);

            intermediateToReal.put(e.getValue(), nextLabelToAssign);

            nextLabelToAssign++;
        }

        for (Map.Entry<Integer, Integer> e : labelReferences.entrySet()) {
            lines.set(e.getKey(), lines.get(e.getKey())
                    .replace("{{" + e.getValue() + "}}", "label" + intermediateToReal.get(e.getValue())));
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < lines.size(); i++) {
            if (labelMap.containsKey(i)) {
                sb.append("label").append(labelMap.get(i)).append(": ").append('\n');
            }

            sb.append(lines.get(i)).append('\n');
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
