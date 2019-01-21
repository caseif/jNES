/*
 * This file is a part of jNES.
 * Copyright (c) 2018-2019, Max Roncace <mproncace@gmail.com>
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

import static net.caseif.jnes.model.cpu.AddressingMode.ABS;
import static net.caseif.jnes.model.cpu.AddressingMode.ABX;
import static net.caseif.jnes.model.cpu.AddressingMode.ABY;
import static net.caseif.jnes.model.cpu.AddressingMode.IMM;
import static net.caseif.jnes.model.cpu.AddressingMode.IMP;
import static net.caseif.jnes.model.cpu.AddressingMode.IND;
import static net.caseif.jnes.model.cpu.AddressingMode.IZX;
import static net.caseif.jnes.model.cpu.AddressingMode.IZY;
import static net.caseif.jnes.model.cpu.AddressingMode.REL;
import static net.caseif.jnes.model.cpu.AddressingMode.ZPX;
import static net.caseif.jnes.model.cpu.AddressingMode.ZPY;
import static net.caseif.jnes.model.cpu.AddressingMode.ZRP;
import static net.caseif.jnes.model.cpu.Mnemonic.ADC;
import static net.caseif.jnes.model.cpu.Mnemonic.AHX;
import static net.caseif.jnes.model.cpu.Mnemonic.ALR;
import static net.caseif.jnes.model.cpu.Mnemonic.ANC;
import static net.caseif.jnes.model.cpu.Mnemonic.AND;
import static net.caseif.jnes.model.cpu.Mnemonic.ARR;
import static net.caseif.jnes.model.cpu.Mnemonic.ASL;
import static net.caseif.jnes.model.cpu.Mnemonic.AXS;
import static net.caseif.jnes.model.cpu.Mnemonic.BCC;
import static net.caseif.jnes.model.cpu.Mnemonic.BCS;
import static net.caseif.jnes.model.cpu.Mnemonic.BEQ;
import static net.caseif.jnes.model.cpu.Mnemonic.BIT;
import static net.caseif.jnes.model.cpu.Mnemonic.BMI;
import static net.caseif.jnes.model.cpu.Mnemonic.BNE;
import static net.caseif.jnes.model.cpu.Mnemonic.BPL;
import static net.caseif.jnes.model.cpu.Mnemonic.BRK;
import static net.caseif.jnes.model.cpu.Mnemonic.BVC;
import static net.caseif.jnes.model.cpu.Mnemonic.BVS;
import static net.caseif.jnes.model.cpu.Mnemonic.CLC;
import static net.caseif.jnes.model.cpu.Mnemonic.CLD;
import static net.caseif.jnes.model.cpu.Mnemonic.CLI;
import static net.caseif.jnes.model.cpu.Mnemonic.CLV;
import static net.caseif.jnes.model.cpu.Mnemonic.CMP;
import static net.caseif.jnes.model.cpu.Mnemonic.CPX;
import static net.caseif.jnes.model.cpu.Mnemonic.CPY;
import static net.caseif.jnes.model.cpu.Mnemonic.DCP;
import static net.caseif.jnes.model.cpu.Mnemonic.DEC;
import static net.caseif.jnes.model.cpu.Mnemonic.DEX;
import static net.caseif.jnes.model.cpu.Mnemonic.DEY;
import static net.caseif.jnes.model.cpu.Mnemonic.EOR;
import static net.caseif.jnes.model.cpu.Mnemonic.INC;
import static net.caseif.jnes.model.cpu.Mnemonic.INX;
import static net.caseif.jnes.model.cpu.Mnemonic.INY;
import static net.caseif.jnes.model.cpu.Mnemonic.ISC;
import static net.caseif.jnes.model.cpu.Mnemonic.JMP;
import static net.caseif.jnes.model.cpu.Mnemonic.JSR;
import static net.caseif.jnes.model.cpu.Mnemonic.KIL;
import static net.caseif.jnes.model.cpu.Mnemonic.LAS;
import static net.caseif.jnes.model.cpu.Mnemonic.LAX;
import static net.caseif.jnes.model.cpu.Mnemonic.LDA;
import static net.caseif.jnes.model.cpu.Mnemonic.LDX;
import static net.caseif.jnes.model.cpu.Mnemonic.LDY;
import static net.caseif.jnes.model.cpu.Mnemonic.LSR;
import static net.caseif.jnes.model.cpu.Mnemonic.NOP;
import static net.caseif.jnes.model.cpu.Mnemonic.ORA;
import static net.caseif.jnes.model.cpu.Mnemonic.PHA;
import static net.caseif.jnes.model.cpu.Mnemonic.PHP;
import static net.caseif.jnes.model.cpu.Mnemonic.PLA;
import static net.caseif.jnes.model.cpu.Mnemonic.PLP;
import static net.caseif.jnes.model.cpu.Mnemonic.RLA;
import static net.caseif.jnes.model.cpu.Mnemonic.ROL;
import static net.caseif.jnes.model.cpu.Mnemonic.ROR;
import static net.caseif.jnes.model.cpu.Mnemonic.RRA;
import static net.caseif.jnes.model.cpu.Mnemonic.RTI;
import static net.caseif.jnes.model.cpu.Mnemonic.RTS;
import static net.caseif.jnes.model.cpu.Mnemonic.SAX;
import static net.caseif.jnes.model.cpu.Mnemonic.SBC;
import static net.caseif.jnes.model.cpu.Mnemonic.SEC;
import static net.caseif.jnes.model.cpu.Mnemonic.SED;
import static net.caseif.jnes.model.cpu.Mnemonic.SEI;
import static net.caseif.jnes.model.cpu.Mnemonic.SHX;
import static net.caseif.jnes.model.cpu.Mnemonic.SHY;
import static net.caseif.jnes.model.cpu.Mnemonic.SLO;
import static net.caseif.jnes.model.cpu.Mnemonic.SRE;
import static net.caseif.jnes.model.cpu.Mnemonic.STA;
import static net.caseif.jnes.model.cpu.Mnemonic.STX;
import static net.caseif.jnes.model.cpu.Mnemonic.STY;
import static net.caseif.jnes.model.cpu.Mnemonic.TAS;
import static net.caseif.jnes.model.cpu.Mnemonic.TAX;
import static net.caseif.jnes.model.cpu.Mnemonic.TAY;
import static net.caseif.jnes.model.cpu.Mnemonic.TSX;
import static net.caseif.jnes.model.cpu.Mnemonic.TXA;
import static net.caseif.jnes.model.cpu.Mnemonic.TXS;
import static net.caseif.jnes.model.cpu.Mnemonic.TYA;
import static net.caseif.jnes.model.cpu.Mnemonic.XAA;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public static Optional<Instruction> lookup(Mnemonic mnemonic, AddressingMode mode) {
        if (!CACHE.containsKey(mnemonic) || !CACHE.get(mnemonic).containsKey(mode)) {
            return Optional.empty();
        }

        Instruction res = CACHE.get(mnemonic).get(mode);

        if (!OPCODE_MAP.containsKey(res)) {
            return Optional.empty();
        }

        return Optional.of(res);
    }

    @Override
    public String toString() {
        return mnemonic.name() + "_" + addrMode.name();
    }

}
