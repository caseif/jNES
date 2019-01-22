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

package net.caseif.jnes.emulation.ppu;

import net.caseif.jnes.emulation.cpu.CpuInterpreter;
import net.caseif.jnes.model.cpu.InterruptType;

public class PpuEmulator {

    private static final int TOTAL_SCANLINES = 262;
    private static final int CYCLES_PER_SCANLINE = 341;

    private final CpuInterpreter cpu;

    private final PpuRegisters regs = new PpuRegisters();
    private final PpuMemory memory = new PpuMemory();

    private int scanlineProgress = 0;
    private int scanline = 0;
    private boolean oddFrame = false;

    public PpuEmulator(CpuInterpreter cpu) {
        this.cpu = cpu;
    }

    public void tick() {
        if (scanlineProgress++ >= CYCLES_PER_SCANLINE) {
            scanlineProgress = 0;

            if (scanline++ >= TOTAL_SCANLINES) {
                scanline = 0;

                oddFrame = !oddFrame;
            }
        }

        if (scanline == 241 && scanlineProgress == 1) {
            // set vblank flag
            regs.status.vblank = true;

            if (regs.control.genNmis) {
                cpu.issueInterrupt(InterruptType.NMI);
            }
        } else if (scanline == 261) {
            // reset PPU status
            regs.status.vblank = false;
            regs.status.sprite0Hit = false;
            regs.status.spriteOverflow = false;
        }
    }

    public byte readMmio(byte index) {
        byte val;

        switch (index) {
            case 2:
                val = (byte) ((regs.status.serialize() & 0b11100000) | (regs.latch & 0b00011111));
                break;
            case 4:
                val = 0; //TODO
                break;
            case 7:
                val = memory.read((short) ((regs.addrHigh << 8) | regs.addrLow));
                break;
            default:
                return regs.latch; // 2C02 returns latch value if write-only register is read
        }

        regs.latch  = val; // latch is filled whenever a readable register is read

        return val;
    }

    public void writeMmio(byte index, byte val) {
        switch (index) {
            case 0:
                boolean oldGenNmis = regs.control.genNmis;

                regs.control.deserializeCtrl(val);

                // if the genNmis flag is newly enabled and we're in vblank, immediately generate an NMI
                if (!oldGenNmis && regs.control.genNmis && regs.status.vblank) {
                    cpu.issueInterrupt(InterruptType.NMI);
                }

                break;
            case 1:
                regs.control.deserializeMask(val);
                break;
            case 3:
                regs.oamAddr = val;
                break;
            case 4:
                //TODO: write to OAM
                break;
            case 5:
                if (regs.scrollWrittenOnce) {
                    regs.yScroll = val;
                } else {
                    regs.xScroll = val;
                }

                regs.scrollWrittenOnce = !regs.scrollWrittenOnce;

                break;
            case 6:
                if (regs.addrHighWritten) {
                    regs.addrLow = val;
                } else {
                    regs.addrHigh = val;
                }

                regs.addrHighWritten = !regs.addrHighWritten;

                break;
            case 7:
                memory.write((short) ((regs.addrHigh << 8) | regs.addrLow), val);
                break;
            default:
                return;
        }

        regs.latch = val;
    }

    public void writeOamDmaAddrHigh(byte addrHigh) {
        regs.oamDmaHigh = addrHigh;
    }

}
