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
    private static final int VISIBLE_SCANLINES = 240;
    private static final int CYCLES_PER_SCANLINE = 341;

    private final CpuInterpreter cpu;

    private final PpuMmioRegisters mmioRegs = new PpuMmioRegisters();
    private final PpuInternalRegisters internalRegs = new PpuInternalRegisters();
    private final PpuStatus status = new PpuStatus();
    private final PpuMemory memory = new PpuMemory();

    private int scanlineCycle = 0;
    private int scanline = 0;
    private boolean oddFrame = false;

    public PpuEmulator(CpuInterpreter cpu) {
        this.cpu = cpu;
    }

    public void tick() {
        performCycle();

        advanceCounters();
    }

    private void performCycle() {
        if (scanline < VISIBLE_SCANLINES) {
            if (scanlineCycle == 0) {
                return; // idle cycle
            } else if (scanlineCycle <= 256) {
                int subcycle = (scanlineCycle - 1) % 8;

                switch (subcycle) {
                    case 1:
                        //TODO: fetch NT byte
                        break;
                    case 3:
                        //TODO: fetch AT byte
                        break;
                    case 5:
                        //TODO: fetch BG low byte
                        break;
                    case 7:
                        //TODO: fetch BG high byte

                        // increment hori(v)
                        if ((internalRegs.v & 0x1F) == 0) {
                            internalRegs.v = (short) ((internalRegs.v & ~0x1F) ^ 0x400);
                        } else {
                            internalRegs.v++;
                        }

                        break;
                    default:
                        return; // first cycle of memory fetch
                }

                if (scanlineCycle == 256) {
                    short v = internalRegs.v;

                    if ((v & 0x7000) != 0x7000) {
                        v += 0x1000;
                    } else {
                        v &= ~0x7000;
                        short y = (short) ((v & 0x03E0) >> 5);
                        if (y == 29) {
                            y = 0;
                            v ^= 0x0800;
                        } else if (y == 31) {
                            y = 0;
                        } else {
                            y += 1;
                        }
                        v = (short) ((v & ~0x03E0) | (y << 5));
                    }

                    internalRegs.v = v;
                }
            } else if (scanlineCycle == 257) {
                internalRegs.v = (short) ((internalRegs.v & ~0b11111) | (internalRegs.t & 0b11111));
            }
        }
    }

    private void advanceCounters() {
        // skip last cycle of last scanline on odd frames
        if (oddFrame && scanline == TOTAL_SCANLINES - 1 && scanlineCycle == CYCLES_PER_SCANLINE - 2) {
            scanlineCycle++;
        }

        if (scanlineCycle++ >= CYCLES_PER_SCANLINE) {
            scanlineCycle = 0;

            if (scanline++ >= TOTAL_SCANLINES) {
                scanline = 0;

                oddFrame = !oddFrame;
            }
        }

        if (scanline == 241 && scanlineCycle == 1) {
            // set vblank flag
            status.vblank = true;

            if (mmioRegs.control.genNmis) {
                cpu.issueInterrupt(InterruptType.NMI);
            }
        } else if (scanline == 261) {
            // reset PPU status
            status.vblank = false;
            status.sprite0Hit = false;
            status.spriteOverflow = false;
        }
    }

    public byte readMmio(byte index) {
        byte val;

        switch (index) {
            case 2:
                val = (byte) ((status.serialize() & 0b11100000) | (mmioRegs.latch & 0b00011111));

                internalRegs.w = false;

                status.spriteOverflow = false;
                status.sprite0Hit = false;

                break;
            case 4:
                val = 0; //TODO
                break;
            case 7:
                val = memory.read(internalRegs.v);
                break;
            default:
                return mmioRegs.latch; // 2C02 returns latch value if write-only register is read
        }

        mmioRegs.latch  = val; // latch is filled whenever a readable register is read

        return val;
    }

    public void writeMmio(byte index, byte val) {
        switch (index) {
            case 0:
                boolean oldGenNmis = mmioRegs.control.genNmis;

                mmioRegs.control.deserializeCtrl(val);

                internalRegs.t = (short) ((internalRegs.t & 0b1100) | (mmioRegs.control.nameTable & 0b11));

                // if the genNmis flag is newly enabled and we're in vblank, immediately generate an NMI
                if (!oldGenNmis && mmioRegs.control.genNmis && status.vblank) {
                    cpu.issueInterrupt(InterruptType.NMI);
                }

                break;
            case 1:
                mmioRegs.control.deserializeMask(val);
                break;
            case 3:
                mmioRegs.oamAddr = val;
                break;
            case 4:
                //TODO: write to OAM
                break;
            case 5:
                if (internalRegs.w) {
                    internalRegs.t = (short) ((internalRegs.t & ~0b11111) | (val << 3));
                    internalRegs.x = (byte) (val & 0b111);
                } else {
                    internalRegs.t = (short) ((internalRegs.t & 0b00001100_00011111)
                            |  ((val       & 0b111) << 12)
                            | (((val >> 6) & 0b11)  << 8)
                            | (((val >> 3) & 0b111) << 5));
                }

                internalRegs.w = !internalRegs.w;

                break;
            case 6:
                if (internalRegs.w) {
                    internalRegs.t = (short) ((internalRegs.t & 0xFF) | ((val & 0b111111) << 8));
                } else {
                    internalRegs.t = (short) ((internalRegs.t & ~0xFF) | (val & 0xFF));
                }

                internalRegs.w = !internalRegs.w;

                break;
            case 7:
                memory.write(internalRegs.v, val);
                break;
            default:
                return;
        }

        mmioRegs.latch = val;
    }

    public void writeOamDmaAddrHigh(byte addrHigh) {
        mmioRegs.oamDmaHigh = addrHigh;
    }

}
