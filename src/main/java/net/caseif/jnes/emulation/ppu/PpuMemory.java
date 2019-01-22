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

public class PpuMemory {

    private final byte[] patternTables = new byte[0x2000];
    private final byte[] nameTables = new byte[0x1F00];
    private final byte[] palettes = new byte[0x100];

    public byte read(short addr) {
        addr %= 0x4000;

        if (addr < 0x2000) {
            return patternTables[addr];
        } else if (addr < 0x3F00) {
            return nameTables[addr - 0x2000];
        } else {
            return palettes[addr - 0x3F00];
        }
    }

    public void write(short addr, byte val) {
        addr %= 0x4000;

        if (addr < 0x2000) {
            patternTables[addr] = val;
        } else if (addr < 0x3F00) {
            nameTables[addr - 0x2000] = val;
        } else {
            palettes[addr - 0x3F00] = val;
        }
    }

}
