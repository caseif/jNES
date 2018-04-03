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

public class CpuMemory {

    private final Cartridge cart;

    private final byte[] sysMemory = new byte[2048];
    private final byte[] ppuIoRegs = new byte[8];

    public CpuMemory(Cartridge cart) {
        this.cart = cart;
    }

    public byte read(byte addr) {
        return read(unsign(addr));
    }

    public byte read(short addr) {
        return read(unsign(addr));
    }

    public byte read(int addr) {
        if (addr < 0x2000) {
            return sysMemory[addr % 0x800];
        } else if (addr < 0x401F) {
            return ppuIoRegs[addr % 8];
        } else if (addr < 0x8000) {
            return 0; // TODO
        } else {
            addr -= 0x8000;
            // ROM is mirrored if cartridge only has 1 bank
            if (cart.getPrgRom().length <= 16384) {
                addr %= 0x4000;
            }
            return cart.getPrgRom()[addr];
        }
    }

    public void write(byte addr, byte value) {
        write(unsign(addr), value);
    }

    public void write(short addr, byte value) {
        write(unsign(addr), value);
    }

    public void write(int addr, byte value) {
        if (addr < 0x2000) {
            sysMemory[addr % 0x800] = value;
        } else if (addr < 0x4000) {
            ppuIoRegs[addr % 8] = value;
        }
        // attempts to write to ROM fail silently
    }

    public void push(CpuRegisters regs, byte value) {
        sysMemory[0x100 + regs.getSp()] = value;
        regs.setSp((byte) (regs.getSp() - 1));
    }

    public byte pop(CpuRegisters regs) {
        regs.setSp((byte) (regs.getSp() + 1));
        return sysMemory[0x100 + regs.getSp()];
    }

}
