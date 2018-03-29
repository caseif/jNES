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

import com.google.common.base.Preconditions;
import net.caseif.jnes.model.cpu.AddressingMode;
import net.caseif.jnes.model.cpu.Instruction;
import net.caseif.jnes.model.cpu.Opcode;
import net.caseif.jnes.util.exception.MalformedAssemblyException;
import net.caseif.jnes.util.tuple.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrgAssembler {

    private static final Pattern LINE_PATTERN = Pattern.compile("^([A-Z]{3})(?:_([A-Z]{3}))?(?:\\s+\\$([\\dA-F]{2,4}))?(?:\\s*;(?:.*))?$");

    private List<Pair<Instruction, Integer>> prg;

    public void read(InputStream input) throws IOException, MalformedAssemblyException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            prg = new ArrayList<>();
            String line;
            while (reader.ready()) {
                line = reader.readLine().trim();
                if (line.startsWith(";") || line.isEmpty()) {
                    continue; // ignore the comment / blank line
                }
                Matcher m = LINE_PATTERN.matcher(line);
                if (!m.find()) {
                    throw new MalformedAssemblyException("Bad assembly line `" + line + "`");
                }
                String opcodeStr = m.group(1);
                String modeStr = m.group(2);
                String valStr = m.group(3);

                try {
                    Opcode opcode = Opcode.valueOf(opcodeStr);
                    AddressingMode mode = modeStr != null ? AddressingMode.valueOf(modeStr) : AddressingMode.IMP;
                    int val = valStr != null ? Integer.parseInt(valStr, 16) : 0;

                    Instruction instr = Instruction.of(opcode, mode);
                    if ((instr.getLength() - 1) != (valStr == null ? 0 : valStr.length() / 2)) {
                        throw new MalformedAssemblyException("Bad assembly line `" + line + "` (bad value for mode)");
                    }
                    Instruction.getOpcode(instr);
                    prg.add(Pair.of(instr, val));
                } catch (IllegalArgumentException ex) {
                    throw new MalformedAssemblyException("Bad assembly line `" + line + "`", ex);
                }
            }
        }
    }

    public void assemble(OutputStream output) throws IOException {
        Preconditions.checkState(prg != null, "No program loaded.");
        for (Pair<Instruction, Integer> p : prg) {
            output.write(Instruction.getOpcode(p.first()));
            if (p.first().getLength() == 2) {
                output.write(p.second());
            } else if (p.first().getLength() == 3) {
                output.write(p.second() >> 4); // write high bits
                output.write(p.second());      // write low bits
            }
        }
        output.close();
    }

}
