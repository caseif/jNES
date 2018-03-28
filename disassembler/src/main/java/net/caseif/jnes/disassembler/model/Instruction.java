package net.caseif.jnes.disassembler.model;

import static net.caseif.jnes.disassembler.model.AddressingMode.*;
import static net.caseif.jnes.disassembler.model.Opcode.*;

import com.google.common.collect.ImmutableList;
import net.caseif.jnes.disassembler.util.CollectionHelper;

import java.util.List;
import java.util.stream.IntStream;

public class Instruction {

    private static final List<Opcode> OPCODE_LIST;
    private static final List<AddressingMode> ADDR_MODE_LIST;

    private static final List<Instruction> INSTR_LIST;

    private final Opcode opcode;
    private final AddressingMode addrMode;

    static {
        OPCODE_LIST = ImmutableList.of(
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

        assert OPCODE_LIST.size() == 256;
        assert ADDR_MODE_LIST.size() == 256;

        INSTR_LIST = IntStream.range(0, 256)
                .mapToObj(i -> new Instruction(OPCODE_LIST.get(i), ADDR_MODE_LIST.get(i)))
                .collect(CollectionHelper.toImmutableList());
    }

    public Instruction(Opcode opcode, AddressingMode addrMode) {
        this.opcode = opcode;
        this.addrMode = addrMode;
    }

    public Opcode getOpcode() {
        return opcode;
    }

    public AddressingMode getAddressingMode() {
        return addrMode;
    }

    public int getLength() {
        return addrMode.getLength();
    }

    public static Instruction fromOpcode(byte opcode) {
        return INSTR_LIST.get(opcode);
    }

}
