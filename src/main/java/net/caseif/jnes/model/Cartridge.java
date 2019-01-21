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

package net.caseif.jnes.model;

public class Cartridge {

    private final byte[] prg;
    private final byte[] chr;
    private final MirroringMode mirrorMode;
    private final boolean cartridgePrgRam;
    private final boolean ignoreMirroringControl;
    private final byte mapper;

    public Cartridge(byte[] prg, byte[] chr, MirroringMode mirrorMode, boolean cartridgePrgRam,
                     boolean ignoreMirroringControl, byte mapper) {
        this.prg = prg;
        this.chr = chr;
        this.mirrorMode = mirrorMode;
        this.cartridgePrgRam = cartridgePrgRam;
        this.ignoreMirroringControl = ignoreMirroringControl;
        this.mapper = mapper;
    }

    public byte[] getPrgRom() {
        return prg;
    }

    public byte[] getChrRom() {
        return chr;
    }

    public MirroringMode getMirroringMode() {
        return mirrorMode;
    }

    public boolean hasCartridgePrgRam() {
        return cartridgePrgRam;
    }

    public boolean doesIgnoreMirroringControl() {
        return ignoreMirroringControl;
    }

    public byte getMapper() {
        return mapper;
    }

    public enum MirroringMode {
        HORIZONTAL,
        VERTICAL
    }

}
