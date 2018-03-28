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

package net.caseif.jnes.emulation.cpu;

import net.caseif.jnes.model.Cartridge;
import net.caseif.jnes.model.cpu.AddressingMode;
import net.caseif.jnes.model.cpu.Instruction;

public class CpuInterpreter {

    private final Cartridge cart;

    private final CpuStatus status = new CpuStatus();
    private final CpuRegisters regs = new CpuRegisters();

    private final CpuMemory memory;

    public CpuInterpreter(Cartridge cart) {
        this.cart = cart;
        this.memory = new CpuMemory(cart);
    }

    public void tick() {
        Instruction instr = Instruction.fromOpcode(readPrg());

        byte m = getM(instr.getAddressingMode());

        switch (instr.getOpcode()) {
            case CLC:
                status.clearFlag(CpuStatus.Flag.CARRY);
                break;
            case CLD:
                status.clearFlag(CpuStatus.Flag.DECIMAL_MODE);
                break;
            case CLI:
                status.clearFlag(CpuStatus.Flag.INTERRUPT_DISABLE);
                break;
            case CLV:
                status.clearFlag(CpuStatus.Flag.OVERFLOW);
                break;
            case SEC:
                status.setFlag(CpuStatus.Flag.CARRY);
                break;
            case SED:
                status.setFlag(CpuStatus.Flag.DECIMAL_MODE);
                break;
            case SEI:
                status.setFlag(CpuStatus.Flag.INTERRUPT_DISABLE);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported instruction " + instr.getOpcode().name());
        }
    }

    public byte getM(AddressingMode mode) {
        byte m = 0;
        switch (mode) {
            case IMM: {
                return readPrg();
            }
            case REL: {
                return readPrg();
            }
            case ZRP: {
                return memory.read(readPrg());
            }
            case ZPX: {
                byte addr = readPrg();
                addr += regs.getX();
                return memory.read(addr);
            }
            case ZPY: {
                byte addr = readPrg();
                addr += regs.getY();
                return memory.read(addr);
            }
            case ABS: {
                // ORDER IS IMPORTANT
                return memory.read((short) (readPrg() & (readPrg() << 8)));
            }
            case ABX: {
                // ORDER IS IMPORTANT
                return memory.read(regs.getX() + (short) (readPrg() & (readPrg() << 8)));
            }
            case ABY: {
                // ORDER IS IMPORTANT
                return memory.read(regs.getY() + (short) (readPrg() & (readPrg() << 8)));
            }
            case IND: {
                // ORDER IS IMPORTANT
                short origAddr = (short) (readPrg() & (readPrg() << 8));
                byte addrLow = memory.read(origAddr);
                byte addrHigh = memory.read(origAddr + 1);
                return memory.read(addrLow & (addrHigh << 8));
            }
            case IZX: {
                // ORDER IS IMPORTANT
                short origAddr = (short) (regs.getX() + (short) (readPrg() & (readPrg() << 8)));
                byte addrLow = memory.read(origAddr);
                byte addrHigh = memory.read(origAddr + 1);
                return memory.read(addrLow & (addrHigh << 8));
            }
            case IZY: {
                // ORDER IS IMPORTANT
                short origAddr = (short) (readPrg() & (readPrg() << 8));
                byte addrLow = memory.read(origAddr);
                byte addrHigh = memory.read(origAddr + 1);
                return memory.read(regs.getY() + (addrLow & (addrHigh << 8)));
            }
            case IMP: {
                return 0;
            }
        }
        return 0;
    }

    public byte readPrg() {
        return memory.read(regs.popPc());
    }

}
