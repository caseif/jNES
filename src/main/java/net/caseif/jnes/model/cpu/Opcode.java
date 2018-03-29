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

import static net.caseif.jnes.model.cpu.Opcode.Type.NONE;
import static net.caseif.jnes.model.cpu.Opcode.Type.R;
import static net.caseif.jnes.model.cpu.Opcode.Type.RW;
import static net.caseif.jnes.model.cpu.Opcode.Type.W;

public enum Opcode {

    // storage
    /**
     * Loads a value into the accumulator.
     */
    LDA(R),
    /**
     * Loads a value into the X register.
     */
    LDX(R),
    /**
     * Loads a value into the Y register.
     */
    LDY(R),
    /**
     * Transfers the accumulator to memory.
     */
    STA(W),
    /**
     * Transfers the X register to memory.
     */
    STX(W),
    /**
     * Transfers the Y register to memory.
     */
    STY(W),
    /**
     * Transfers the accumulator to the X register.
     */
    TAX(NONE),
    /**
     * Transfers the accumulator to the Y register.
     */
    TAY(NONE),
    /**
     * Transfers the stack pointer to the X register.
     */
    TSX(NONE),
    /**
     * Transfers the X register to the accumulator.
     */
    TXA(NONE),
    /**
     * Transfers the Y register to the accumulator.
     */
    TYA(NONE),
    /**
     * Transfers the X register to the stack pointer.
     */
    TXS(NONE),

    // math
    /**
     * Adds a value to the accumulator (with carry).
     */
    ADC(R),
    /**
     * Subtracts a value from the accumulator (with borrow).
     */
    SBC(R),
    /**
     * Decrements a value in memory by 1.
     */
    DEC(RW),
    /**
     * Decrements the X register by 1.
     */
    DEX(NONE),
    /**
     * Decrements the Y register by 1.
     */
    DEY(NONE),
    /**
     * Increments a value in memory by 1.
     */
    INC(RW),
    /**
     * Increments the X register by 1.
     */
    INX(NONE),
    /**
     * Increments the Y register by 1.
     */
    INY(NONE),

    // logic
    /**
     * Applies a bitwise AND to the accumulator with a value in memory.
     */
    AND(R),
    /**
     * Applies a bitwise left-shift to the accumulator or a value in memory.
     */
    ASL(RW),
    /**
     * Applies a bitwise right-shift to the accumulator or a value in memory.
     */
    LSR(RW),
    /**
     * Performs a bitwise AND between the accumulator and a value in memory and
     * sets the Z (zero) flag according to the result. The N and V flags are set
     * respectively to bits 7 and 6 of the original value from memory.
     */
    BIT(R),
    /**
     * Applies a bitwise XOR to the accumulator with a value in memory.
     */
    EOR(R),
    /**
     * Apples a bitwise OR to the accumulator with a value in memory.
     */
    ORA(R),
    /**
     * Apples a bitwise left-shift to the accumulator or a value in memory,
     * copying the carry flag to bit 0 and bit 7 to the carry flag.
     */
    ROL(RW),
    /**
     * Apples a bitwise right-shift to the accumulator or a value in memory,
     * copying the carry flag to bit 7 and bit 0 to the carry flag.
     */
    ROR(RW),

    // branching
    /**
     * Branches if the carry flag is clear.
     */
    BCC(NONE),
    /**
     * Branches if the carry flag is set.
     */
    BCS(NONE),
    /**
     * Branches if the zero flag is clear.
     */
    BNE(NONE),
    /**
     * Branches if the zero flag is set.
     */
    BEQ(NONE),
    /**
     * Branches if the negative flag is clear.
     */
    BPL(NONE),
    /**
     * Branches if the negative flag is set.
     */
    BMI(NONE),
    /**
     * Branches if the overflow flag is clear.
     */
    BVC(NONE),
    /**
     * Branches if the overflow flag is set.
     */
    BVS(NONE),

    // jumping
    /**
     * Jumps to an address.
     */
    JMP(NONE),
    /**
     * Pushes the address of the next operation minus one to the stack, then
     * jumps to an address.
     */
    JSR(NONE),
    /**
     * Returns from an interrupt, pulling processor flags followed by the
     * program counter from the stack.
     */
    RTI(NONE),
    /**
     * Returns from a subroutine, pulling the program counter from the stack.
     */
    RTS(NONE),

    // registers
    /**
     * Clears the carry flag.
     */
    CLC(NONE),
    /**
     * Clears the decimal flag.
     */
    CLD(NONE),
    /**
     * Clears the interrupt disable flag.
     */
    CLI(NONE),
    /**
     * Clears the overflow flag.
     */
    CLV(NONE),
    /**
     * Compares a value against the accumulator, setting the zero flag if equal
     * and the carry flag if A >= V.
     */
    CMP(R),
    /**
     * Compares a value against the X register, setting the zero flag if equal
     * and the carry flag if X >= V.
     */
    CPX(R),
    /**
     * Compares a value against the Y register, setting the zero flag if equal
     * and the carry flag if Y >= V.
     */
    CPY(R),
    /**
     * Sets the carry flag.
     */
    SEC(NONE),
    /**
     * Sets the decimal flag.
     */
    SED(NONE),
    /**
     * Sets the interrupt disable flag.
     */
    SEI(NONE),

    // stack
    /**
     * Pushes the accumulator to the stack.
     */
    PHA(NONE),
    /**
     * Pushes the status flags to the stack.
     */
    PHP(NONE),
    /**
     * Pulls the accumulator from the stack.
     */
    PLA(NONE),
    /**
     * Pulls the status flags from the stack.
     */
    PLP(NONE),

    // system
    /**
     * Forces an interrupt, pushing the program counter followed by processor
     * flags to the stack, then copying $FFFE/F to the program counter and
     * setting the break flag.
     */
    BRK(NONE),
    /**
     * Does nothing.
     */
    NOP(NONE),

    // undocumented
    /**
     * Halts the processor.
     */
    KIL(NONE),
    ANC(NONE),
    SLO(NONE),
    RLA(NONE),
    SRE(NONE),
    RRA(NONE),
    SAX(NONE),
    LAX(NONE),
    DCP(NONE),
    ALR(NONE),
    XAA(NONE),
    TAS(NONE),
    SHY(NONE),
    SHX(NONE),
    AHX(NONE),
    ARR(NONE),
    LAS(NONE),
    ISC(NONE),
    AXS(NONE);

    private final Type type;

    Opcode(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        NONE,
        R,
        W,
        RW;
    }

}
