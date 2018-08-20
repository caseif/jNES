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

package net.caseif.jnes.model.cpu;

import static net.caseif.jnes.model.cpu.AddressingMode.*;
import static net.caseif.jnes.model.cpu.Mnemonic.*;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Instruction {

    private static final Map<Mnemonic, Map<AddressingMode, Instruction>> CACHE = new HashMap<>();

    private static final List<Mnemonic> MNEMONIC_LIST;
    private static final List<AddressingMode> ADDR_MODE_LIST;

    private static final List<Instruction> INSTR_LIST;
    private static final Map<Instruction, Integer> OPCODE_MAP;

    private final Mnemonic mnemonic;
    private final AddressingMode addrMode;

    static {
        MNEMONIC_LIST = ImmutableList.of(
                BRK, ORA, KIL, SLO, NOP, ORA, ASL, SLO, PHP, ORA, ASL, ANC, NOP, ORA, ASL, SLO,
                BPL, ORA, KIL, SLO, NOP, ORA, ASL, SLO, CLC, ORA, NOP, SLO, NOP, ORA, ASL, SLO,
                JSR, AND, KIL, RLA, BIT, AND, ROL, RLA, PLP, AND, ROL, ANC, BIT, AND, ROL, RLA,
                BMI, AND, KIL, RLA, NOP, AND, ROL, RLA, SEC, AND, NOP, RLA, NOP, AND, ROL, RLA,
                RTI, EOR, KIL, SRE, NOP, EOR, LSR, SRE, PHA, EOR, LSR, ALR, JMP, EOR, LSR, SRE,
                BVC, EOR, KIL, SRE, NOP, EOR, LSR, SRE, CLI, EOR, NOP, SRE, NOP, EOR, LSR, SRE,
                RTS, ADC, KIL, RRA, NOP, ADC, ROR, RRA, PLA, ADC, ROR, ARR, JMP, ADC, ROR, RRA,
                BVS, ADC, KIL, RRA, NOP, ADC, ROR, RRA, SEI, ADC, NOP, RRA, NOP, ADC, ROR, RRA,
                NOP, STA, NOP, SAX, STY, STA, STX, SAX, DEY, NOP, TXA, XAA, STY, STA, STX, SAX,
                BCC, STA, KIL, AHX, STY, STA, STX, SAX, TYA, STA, TXS, TAS, SHY, STA, SHX, AHX,
                LDY, LDA, LDX, LAX, LDY, LDA, LDX, LAX, TAY, LDA, TAX, LAX, LDY, LDA, LDX, LAX,
                BCS, LDA, KIL, LAX, LDY, LDA, LDX, LAX, CLV, LDA, TSX, LAS, LDY, LDA, LDX, LAX,
                CPY, CMP, NOP, DCP, CPY, CMP, DEC, DCP, INY, CMP, DEX, AXS, CPY, CMP, DEC, DCP,
                BNE, CMP, KIL, DCP, NOP, CMP, DEC, DCP, CLD, CMP, NOP, DCP, NOP, CMP, DEC, DCP,
                CPX, SBC, NOP, ISC, CPX, SBC, INC, ISC, INX, SBC, NOP, SBC, CPX, SBC, INC, ISC,
                BEQ, SBC, KIL, ISC, NOP, SBC, INC, ISC, SED, SBC, NOP, ISC, NOP ,SBC, INC, ISC
        );
        ADDR_MODE_LIST = ImmutableList.of(
                IMP, IZX, IMP, IZX, ZRP, ZRP, ZRP, ZRP, IMP, IMM, IMP, IMM, ABS, ABS, ABS, ABS,
                REL, IZY, IMP, IZY, ZPX, ZPX, ZPX, ZPX, IMP, ABY, IMP, ABY, ABX, ABX, ABX, ABX,
                ABS, IZX, IMP, IZX, ZRP, ZRP, ZRP, ZRP, IMP, IMM, IMP, IMM, ABS, ABS, ABS, ABS,
                REL, IZY, IMP, IZY, ZPX, ZPX, ZPX, ZPX, IMP, ABY, IMP, ABY, ABX, ABX, ABX, ABX,
                IMP, IZX, IMP, IZX, ZRP, ZRP, ZRP, ZRP, IMP, IMM, IMP, IMM, ABS, ABS, ABS, ABS,
                REL, IZY, IMP, IZY, ZPX, ZPX, ZPX, ZPX, IMP, ABY, IMP, ABY, ABX, ABX, ABX, ABX,
                IMP, IZX, IMP, IZX, ZRP, ZRP, ZRP, ZRP, IMP, IMM, IMP, IMM, IND, ABS, ABS, ABS,
                REL, IZY, IMP, IZY, ZPX, ZPX, ZPX, ZPX, IMP, ABY, IMP, ABY, ABX, ABX, ABX, ABX,
                IMM, IZX, IMM, IZX, ZRP, ZRP, ZRP, ZRP, IMP, IMM, IMP, IMM, ABS, ABS, ABS, ABS,
                REL, IZY, IMP, IZY, ZPX, ZPX, ZPY, ZPY, IMP, ABY, IMP, ABY, ABX, ABX, ABY, ABY,
                IMM, IZX, IMM, IZX, ZRP, ZRP, ZRP, ZRP, IMP, IMM, IMP, IMM, ABS, ABS, ABS, ABS,
                REL, IZY, IMP, IZY, ZPX, ZPX, ZPY, ZPY, IMP, ABY, IMP, ABY, ABX, ABX, ABY, ABY,
                IMM, IZX, IMM, IZX, ZRP, ZRP, ZRP, ZRP, IMP, IMM, IMP, IMM, ABS, ABS, ABS, ABS,
                REL, IZY, IMP, IZY, ZPX, ZPX, ZPX, ZPX, IMP, ABY, IMP, ABY, ABX, ABX, ABX, ABX,
                IMM, IZX, IMM, IZX, ZRP, ZRP, ZRP, ZRP, IMP, IMM, IMP, IMM, ABS, ABS, ABS, ABS,
                REL, IZY, IMP, IZY, ZPX, ZPX, ZPX, ZPX, IMP, ABY, IMP, ABY, ABX, ABX, ABX, ABX
        );

        assert MNEMONIC_LIST.size() == 256;
        assert ADDR_MODE_LIST.size() == 256;

        INSTR_LIST = IntStream.range(0, 256)
                .mapToObj(i -> Instruction.of(MNEMONIC_LIST.get(i), ADDR_MODE_LIST.get(i)))
                .collect(ImmutableList.toImmutableList());

        OPCODE_MAP = IntStream.range(0, 256).boxed().collect(Collectors.toMap(INSTR_LIST::get, i -> i, (a, b) -> a));
    }

    private Instruction(Mnemonic mnemonic, AddressingMode addrMode) {
        this.mnemonic = mnemonic;
        this.addrMode = addrMode;
        CACHE.computeIfAbsent(mnemonic, oc -> new HashMap<>()).put(addrMode, this);
    }

    public Mnemonic getMnemonic() {
        return mnemonic;
    }

    public AddressingMode getAddressingMode() {
        return addrMode;
    }

    public int getLength() {
        return addrMode.getLength();
    }

    public static Instruction fromOpcode(byte opcode) {
        int opcodei = opcode < 0 ? opcode + 256 : opcode;
        return INSTR_LIST.get(opcodei);
    }

    public short getOpcode() {
        Preconditions.checkArgument(OPCODE_MAP.containsKey(this), "Bad instruction " + this);
        return OPCODE_MAP.get(this).shortValue();
    }

    public static Instruction of(Mnemonic mnemonic, AddressingMode mode) {
        if (CACHE.containsKey(mnemonic)) {
            if (CACHE.get(mnemonic).containsKey(mode)) {
                return CACHE.get(mnemonic).get(mode);
            }
        }
        return new Instruction(mnemonic, mode);
    }

    @Override
    public String toString() {
        return mnemonic.name() + "_" + addrMode.name();
    }

}
