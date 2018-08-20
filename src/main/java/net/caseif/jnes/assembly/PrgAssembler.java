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
import com.google.common.collect.ImmutableList;
import net.caseif.jnes.model.cpu.AddressingMode;
import net.caseif.jnes.model.cpu.Instruction;
import net.caseif.jnes.model.cpu.Mnemonic;
import net.caseif.jnes.util.exception.MalformedAssemblyException;
import net.caseif.jnes.util.tuple.Pair;
import net.caseif.jnes.util.tuple.Triple;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrgAssembler {

    private static final Pattern LINE_PATTERN
            = Pattern.compile("^(?:([A-z0-9]*):\\s*)?(?:(?:([A-Z]{3}))(?:\\s+(.*?))??)?(?:\\s*;.*)?$");

    private static final Pattern MODE_IMM_PATTERN = Pattern.compile("^#\\$([0-9A-F]{2})$");
    private static final Pattern MODE_ZRP_PATTERN = Pattern.compile("^\\$([0-9A-F]{2})$");
    private static final Pattern MODE_ZPX_PATTERN = Pattern.compile("^\\$([0-9A-F]{2}),X$");
    private static final Pattern MODE_ZPY_PATTERN = Pattern.compile("^\\$([0-9A-F]{2}),Y$");
    private static final Pattern MODE_IZX_PATTERN = Pattern.compile("^\\(\\$([0-9A-F]{2}),X\\)$");
    private static final Pattern MODE_IZY_PATTERN = Pattern.compile("^\\(\\$([0-9A-F]{2})\\),Y$");
    private static final Pattern MODE_ABS_PATTERN = Pattern.compile("^\\$([0-9A-F]{4})$");
    private static final Pattern MODE_ABS_LABEL_PATTERN = Pattern.compile("^([A-z0-9]+)$");
    private static final Pattern MODE_ABX_PATTERN = Pattern.compile("^\\$([0-9A-F]{4}),X$");
    private static final Pattern MODE_ABY_PATTERN = Pattern.compile("^\\$([0-9A-F]{4}),Y$");
    private static final Pattern MODE_IND_PATTERN = Pattern.compile("^\\(\\$([0-9A-F]{4})\\)$");

    private static final ImmutableList<Pair<Pattern, AddressingMode>> MODE_PATTERNS = ImmutableList.of(
            Pair.of(MODE_IMM_PATTERN, AddressingMode.IMM),
            Pair.of(MODE_ZRP_PATTERN, AddressingMode.ZRP),
            Pair.of(MODE_ZPX_PATTERN, AddressingMode.ZPX),
            Pair.of(MODE_ZPY_PATTERN, AddressingMode.ZPY),
            Pair.of(MODE_IZX_PATTERN, AddressingMode.IZX),
            Pair.of(MODE_IZY_PATTERN, AddressingMode.IZY),
            Pair.of(MODE_ABX_PATTERN, AddressingMode.ABX),
            Pair.of(MODE_ABY_PATTERN, AddressingMode.ABY),
            Pair.of(MODE_IND_PATTERN, AddressingMode.IND),
            Pair.of(MODE_ABS_PATTERN, AddressingMode.ABS),
            Pair.of(MODE_ABS_LABEL_PATTERN, AddressingMode.ABS)
    );

    private List<Pair<Instruction, Object>> prg;
    private Map<String, Integer> vars;

    public void read(InputStream input) throws IOException, MalformedAssemblyException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            prg = new ArrayList<>();
            vars = new HashMap<>();

            String line;
            int lineNum = 0;
            int addr = 0;
            while (reader.ready()) {
                lineNum++;
                line = reader.readLine().trim();
                if (line.startsWith(";") || line.isEmpty()) {
                    continue; // ignore the comment / blank line
                }
                Matcher m = LINE_PATTERN.matcher(line);
                if (!m.find()) {
                    throw new MalformedAssemblyException("Cannot parse line " + lineNum + ".");
                }

                String label = m.group(1);

                if (label != null) {
                    vars.put(label, addr);
                }

                String mnemonicStr = m.group(2);
                String valStr = m.group(3);

                if (mnemonicStr == null) {
                    continue;
                }

                try {
                    Mnemonic mnemonic;
                    try {
                        mnemonic = Mnemonic.valueOf(mnemonicStr);
                    } catch (IllegalArgumentException ex) {
                        throw new MalformedAssemblyException("Invalid mnemonic " + mnemonicStr + " on line " + lineNum + ".");
                    }

                    AddressingMode mode = null;

                    Object val = null;

                    if (valStr != null) {
                        for (Pair<Pattern, AddressingMode> p : MODE_PATTERNS) {
                            Matcher vm = p.first().matcher(valStr);
                            if (vm.find()) {
                                mode = p.second();
                                if (p.first() == MODE_ABS_LABEL_PATTERN) {
                                    val = vm.group(1);
                                    if (mnemonic.getType() != Mnemonic.Type.BRANCH) {
                                        throw new MalformedAssemblyException("Label cannot be applied to non-branch "
                                                + "instruction on line " + lineNum + ".");
                                    }
                                } else {
                                    val = Integer.parseInt(vm.group(1), 16);
                                }
                                if (mode == AddressingMode.ABS && (mnemonic.getType() == Mnemonic.Type.BRANCH
                                        && mnemonic != Mnemonic.JMP && mnemonic != Mnemonic.JSR)) {
                                    mode = AddressingMode.REL;
                                }
                            }
                        }

                        if (mode == null) {
                            throw new MalformedAssemblyException("Malformed value `" + valStr + "` on line " + lineNum + ".");
                        }
                    } else {
                        mode = AddressingMode.IMP;
                    }

                    Instruction instr = Instruction.of(mnemonic, mode);

                    addr += instr.getLength();

                    // verify the instruction is valid
                    try {
                        instr.getOpcode();
                    } catch (IllegalArgumentException ex) {
                        throw new MalformedAssemblyException("Invalid instruction `" + instr + "` on line " + lineNum + ".", ex);
                    }

                    prg.add(Pair.of(instr, val));
                } catch (IllegalArgumentException ex) {
                    throw new MalformedAssemblyException("Cannot parse line " + lineNum + ".", ex);
                }
            }
        }
    }

    public void assemble(OutputStream output) throws IOException, MalformedAssemblyException {
        Preconditions.checkState(prg != null, "No program loaded.");

        ByteArrayOutputStream intermediate = new ByteArrayOutputStream();

        final int OFFSET = 0x8000; //TODO: read this from a .org directive

        int addr = 0;
        int line = 0;
        int pc = 0;
        for (Pair<Instruction, Object> p : prg) {
            line++;
            intermediate.write(p.first().getOpcode());
            pc++;
            int val;
            if (p.second() == null) {
                val = 0;
            } else if (p.second() instanceof Integer) {
                val = (Integer) p.second();
            } else {
                assert p.second() instanceof String;
                if (p.first().getMnemonic() == Mnemonic.JMP || p.first().getMnemonic() == Mnemonic.JSR) {
                    val = vars.get((String) p.second()) + OFFSET;
                } else {
                    val = vars.get((String) p.second()) - addr - p.first().getLength();
                    if (val < Byte.MIN_VALUE || val > Byte.MAX_VALUE) {
                        throw new MalformedAssemblyException("Bad reference to label at instruction " + line
                                + " (offset too large).");
                    }
                }
            }

            addr += p.first().getLength();

            if (p.first().getLength() == 2) {
                intermediate.write(val);
                pc++;
            } else if (p.first().getLength() == 3) {
                intermediate.write(val & 0xFF);      // write low bits
                intermediate.write(val >> 8); // write high bits
                pc += 2;
            }
        }

        byte[] bytes = intermediate.toByteArray();

        output.write(bytes);

        output.close();
    }

    private class LocationReference {

        private final String name;
        private final int location;
        private final int length;

        private LocationReference(String name, int location, int length) {
            this.name = name;
            this.location = location;
            this.length = length;
        }

        public String getName() {
            return name;
        }

        public int getLocation() {
            return location;
        }

        public int getLength() {
            return length;
        }
    }

}
