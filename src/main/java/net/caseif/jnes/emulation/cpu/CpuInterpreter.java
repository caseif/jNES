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

import static net.caseif.jnes.util.MathHelper.unsign;

import net.caseif.jnes.model.Cartridge;
import net.caseif.jnes.model.cpu.AddressingMode;
import net.caseif.jnes.model.cpu.Instruction;
import net.caseif.jnes.util.exception.CpuHaltedException;
import net.caseif.jnes.util.tuple.Pair;

public class CpuInterpreter {

    private final Cartridge cart;

    private final CpuStatus status = new CpuStatus();
    final CpuRegisters regs = new CpuRegisters();

    public final CpuMemory memory;

    public CpuInterpreter(Cartridge cart) {
        this.cart = cart;
        this.memory = new CpuMemory(cart);
    }

    public void tick() throws CpuHaltedException {
        Instruction instr = null;
        try {
            instr = Instruction.fromOpcode(readPrg());
            //System.out.println("Executing instruction " + instr.getOpcode().name()
            //        + " @ $" + String.format("%02X", regs.getPc() - 1));
            tick0(instr);
        } catch (CpuHaltedException ex) {
            throw ex;
        } catch (Throwable t) {
            throw new RuntimeException("Exception occurred while executing instruction "
                    + (instr != null ? instr : "(unknown)")
                    + " @ $" + String.format("%04X", regs.getPc() - 1), t);
        }
    }

    private void tick0(Instruction instr) throws CpuHaltedException {
        Pair<Byte, Short> mp = getM(instr.getAddressingMode());
        byte m = mp.first();
        short addr = mp.second();

        switch (instr.getOpcode()) {
            // storage
            case LDA:
                regs.setAcc(m);
                break;
            case LDX:
                regs.setX(m);
                break;
            case LDY:
                regs.setY(m);
                break;
            case STA:
                memory.write(addr, (byte) regs.getAcc());
                break;
            case STX:
                memory.write(addr, (byte) regs.getX());
                break;
            case STY:
                memory.write(addr, (byte) regs.getY());
                break;
            case TAX:
                regs.setX((byte) regs.getAcc());
                break;
            case TAY:
                regs.setY((byte) regs.getAcc());
                break;
            case TSX:
                regs.setX((byte) regs.getSp());
                break;
            case TXA:
                regs.setAcc((byte) regs.getX());
                break;
            case TYA:
                regs.setAcc((byte) regs.getX());
                break;
            case TXS:
                regs.setSp((byte) regs.getX());
                break;
            // math
            case ADC: {
                byte acc0 = (byte) regs.getAcc();
                regs.setAcc((byte) (acc0 + m));

                boolean carry = (acc0 >> 7) + (m >> 7) + (((regs.getAcc() & 0x40) & (m & 0x40)) >> 6) > 1;
                if (carry) {
                    status.setFlag(CpuStatus.Flag.CARRY);
                } else {
                    status.clearFlag(CpuStatus.Flag.CARRY);
                }

                boolean overflow = ((acc0 ^ regs.getAcc()) & (m ^ regs.getAcc()) & 0x80) != 0;
                if (overflow) {
                    status.setFlag(CpuStatus.Flag.OVERFLOW);
                } else {
                    status.clearFlag(CpuStatus.Flag.OVERFLOW);
                }
                break;
            }
            case SBC: {
                byte acc0 = (byte) regs.getAcc();
                regs.setAcc((byte) (acc0 + m));

                boolean borrow = (acc0 >> 7) + ((255 - m) >> 7) + (((regs.getAcc() & 0x40) & ((255 - m) & 0x40)) >> 6) <= 1;
                if (borrow) {
                    status.setFlag(CpuStatus.Flag.CARRY);
                } else {
                    status.clearFlag(CpuStatus.Flag.CARRY);
                }

                boolean overflow = ((acc0 ^ regs.getAcc()) & ((255 - m) ^ regs.getAcc()) & 0x80) != 0;
                if (overflow) {
                    status.setFlag(CpuStatus.Flag.OVERFLOW);
                } else {
                    status.clearFlag(CpuStatus.Flag.OVERFLOW);
                }
                break;
            }
            case DEC:
                memory.write(addr, (byte) (m - 1));
                break;
            case DEX:
                regs.setX((byte) (regs.getX() - 1));
                break;
            case DEY:
                regs.setY((byte) (regs.getY() - 1));
                break;
            case INC:
                memory.write(addr, (byte) (m + 1));
                break;
            case INX:
                regs.setX((byte) (regs.getX() + 1));
                break;
            case INY:
                regs.setY((byte) (regs.getY() + 1));
                break;
            // logic
            case AND:
                regs.setAcc((byte) (regs.getAcc() & m));
                break;
            case ASL:
                shift(instr, false, false, m, addr);
                break;
            case LSR:
                shift(instr, true, false, m, addr);
                break;
            case EOR:
                regs.setAcc((byte) (regs.getAcc() ^ m));
                break;
            case ORA:
                regs.setAcc((byte) (regs.getAcc() | m));
            case ROL:
                shift(instr, false, true, m, addr);
                break;
            case ROR:
                shift(instr, true, true, m, addr);
                break;
            // branching
            case BCC:
                if (!status.getFlag(CpuStatus.Flag.CARRY)) {
                    branch(m);
                }
                break;
            case BCS:
                if (status.getFlag(CpuStatus.Flag.CARRY)) {
                    branch(m);
                }
                break;
            case BNE:
                if (!status.getFlag(CpuStatus.Flag.ZERO)) {
                    branch(m);
                }
                break;
            case BEQ:
                if (status.getFlag(CpuStatus.Flag.ZERO)) {
                    branch(m);
                }
                break;
            case BPL:
                if (!status.getFlag(CpuStatus.Flag.NEGATIVE)) {
                    branch(m);
                }
                break;
            case BMI:
                if (status.getFlag(CpuStatus.Flag.NEGATIVE)) {
                    branch(m);
                }
                break;
            case BVC:
                if (!status.getFlag(CpuStatus.Flag.OVERFLOW)) {
                    branch(m);
                }
                break;
            case BVS:
                if (status.getFlag(CpuStatus.Flag.OVERFLOW)) {
                    branch(m);
                }
                break;
            case JMP:
                regs.setPc(m);
                break;
            case JSR:
                memory.push(regs, (byte) (((regs.getPc() >> 8) & 0xFF) - 1)); // push MSB of PC
                memory.push(regs, (byte) ((regs.getPc() & 0xFF) - 1)); // push LSB of PC
                break;
            case RTS: {
                byte pcl = memory.pop(regs); // pop LSB of PC
                byte pcm = memory.pop(regs); // pop MSB of PC
                regs.setPc((short) ((pcm & pcl) + 1));
                break;
            }
            case RTI: {
                status.deserialize(memory.pop(regs)); // pop flags
                byte pcl = memory.pop(regs); // pop LSB of PC
                byte pcm = memory.pop(regs); // pop MSB of PC
                regs.setPc((short) ((pcm & pcl) + 1));
                break;
            }
            // registers
            case CLC:
                status.clearFlag(CpuStatus.Flag.CARRY);
                break;
            case CLD:
                // no-op
                break;
            case CLI:
                status.clearFlag(CpuStatus.Flag.INTERRUPT_DISABLE);
                break;
            case CLV:
                status.clearFlag(CpuStatus.Flag.OVERFLOW);
                break;
            case CMP:
                cmp(regs.getAcc(), m);
                break;
            case CPX:
                cmp(regs.getX(), m);
                break;
            case CPY:
                cmp(regs.getY(), m);
                break;
            case SEC:
                status.setFlag(CpuStatus.Flag.CARRY);
                break;
            case SED:
                // no-op
                break;
            case SEI:
                status.setFlag(CpuStatus.Flag.INTERRUPT_DISABLE);
                break;
            // stack
            case PHA:
                memory.push(regs, (byte) regs.getAcc());
                break;
            case PHP:
                memory.push(regs, status.serialize());
                break;
            case PLA:
                regs.setAcc(memory.pop(regs));
                break;
            case PLP:
                status.deserialize(memory.pop(regs));
                break;
            // system
            case BRK: {
                if (!status.getFlag(CpuStatus.Flag.INTERRUPT_DISABLE)) {
                    break;
                }

                memory.push(regs, (byte) ((regs.getPc() >> 8) & 0xFF)); // push MSB of PC
                memory.push(regs, (byte) (regs.getPc() & 0xFF)); // push LSB of PC
                memory.push(regs, status.serialize()); // push flags

                // load interrupt vector from ROM
                regs.setPc((short) (memory.read(0xFFFE) & (memory.read(0xFFFF) << 8)));

                status.setFlag(CpuStatus.Flag.BREAK_COMMAND);
                break;
            }
            case NOP:
                // no-op
                break;
            case KIL:
                throw new CpuHaltedException();
            default:
                //TODO
                // no-op
                for (int i = 0; i < instr.getLength() - 1; i++) {
                    readPrg();
                }
                break;
            //default:
            //    throw new UnsupportedOperationException("Unsupported instruction " + instr.getOpcode().name());
        }

        if (regs.getPc() - 0x8000 >= cart.getPrgRom().length) {
            throw new CpuHaltedException();
        }
    }

    private void cmp(short reg, byte m) {
        if (m >= reg) {
            status.setFlag(CpuStatus.Flag.ZERO);
            if (m == regs.getAcc()) {
                status.setFlag(CpuStatus.Flag.CARRY);
            } else {
                status.clearFlag(CpuStatus.Flag.CARRY);
            }
        } else {
            status.clearFlag(CpuStatus.Flag.ZERO);
        }
    }

    private void shift(Instruction instr, boolean right, boolean rotate, byte m, short addr) {
        byte val = instr.getAddressingMode() == AddressingMode.IMP ? (byte) regs.getAcc() : m;

        if ((val & 0x80) != 0) {
            status.setFlag(CpuStatus.Flag.CARRY);
        } else {
            status.clearFlag(CpuStatus.Flag.CARRY);
        }

        byte rMask = 0;
        if (rotate) {
            rMask = (byte) (status.getFlag(CpuStatus.Flag.CARRY) ? 1 : 0);
            if (right) {
                rMask <<= 7;
            }
        }

        if (instr.getAddressingMode() == AddressingMode.IMP) {
            regs.setAcc((byte) ((right ? (regs.getAcc() >> 1) : (regs.getAcc() << 1)) | rMask));
        } else {
            memory.write(addr, (byte) ((right ? (m >> 1) : (m << 1)) | rMask));
        }
    }

    private void branch(byte m) {
        regs.setPc((short) (regs.getPc() + m));
    }

    /**
     * Returns value M, along with the address it twas obtained from, if applicable.
     * @param mode
     * @return
     */
    private Pair<Byte, Short> getM(AddressingMode mode) {
        switch (mode) {
            case IMM: {
                return Pair.of(readPrg(), (short) 0);
            }
            case REL: {
                return Pair.of(readPrg(), (short) 0);
            }
            case ZRP: {
                short addr = unsign(readPrg());
                return Pair.of(memory.read(addr), addr);
            }
            case ZPX: {
                byte addr = readPrg();
                addr += regs.getX();
                return Pair.of(memory.read(addr), unsign(addr));
            }
            case ZPY: {
                byte addr = readPrg();
                addr += regs.getY();
                return Pair.of(memory.read(addr), unsign(addr));
            }
            case ABS: {
                short addr = readShort();
                return Pair.of(memory.read(addr), addr);
            }
            case ABX: {
                short addr = (short) (regs.getX() + readShort());
                return Pair.of(memory.read(addr), addr);
            }
            case ABY: {
                short addr = (short) (regs.getY() + readShort());
                return Pair.of(memory.read(addr), addr);
            }
            case IND: {
                short origAddr = readShort();
                byte addrLow = memory.read(origAddr);
                byte addrHigh = memory.read(origAddr + 1);
                short addr = (short) (addrLow & (addrHigh << 8));
                return Pair.of(memory.read(addr), addr);
            }
            case IZX: {
                short origAddr = (short) (regs.getX() + unsign(readPrg()));
                byte addrLow = memory.read(origAddr);
                byte addrHigh = memory.read(origAddr + 1);
                short addr = (short) (addrLow | (addrHigh << 8));
                return Pair.of(memory.read(addr), addr);
            }
            case IZY: {
                byte origAddr = readPrg();
                byte addrLow = memory.read(origAddr);
                byte addrHigh = memory.read(origAddr + 1);
                short addr = (short) (regs.getY() + (addrLow | (addrHigh << 8)));
                return Pair.of(memory.read(addr), addr);
            }
            case IMP: {
                return Pair.of((byte) 0, (short) 0);
            }
            default: {
                throw new AssertionError("Unhandled addressing mode " + mode.name());
            }
        }
    }

    private byte readPrg() {
        return memory.read(regs.popPc());
    }

    private short readShort() {
        // ORDER IS IMPORTANT
        return (short) (unsign(readPrg()) | (unsign(readPrg()) << 8));
    }

    byte peekPrg() {
        return memory.read(regs.getPc());
    }

}
