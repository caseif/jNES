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

public class PpuControl {

    // PPUCTRL
    public byte nameTable = 0;
    public boolean verticalIncrement = false;
    public byte spriteTable = 0;
    public byte backgroundTable = 0;
    public boolean tallSprites = false;
    public boolean extMaster = false;
    public boolean genNmis = false;

    // PPUMASK
    public boolean grayscale = false;
    public boolean clipBackground = false;
    public boolean clipSprites = false;
    public boolean showBackground = true;
    public boolean showSprites = true;
    public boolean emRed = false;
    public boolean emGreen = false;
    public boolean emBlue = false;

    public void deserializeCtrl(byte serial) {
        this.nameTable          = (byte)  (serial & 0b00000011);
        this.verticalIncrement  =         (serial & 0b00000100) != 0;
        this.spriteTable        = (byte) ((serial & 0b00001000) >> 3);
        this.backgroundTable    = (byte) ((serial & 0b00010000) >> 4);
        this.tallSprites        =         (serial & 0b00100000) != 0;
        this.extMaster          =         (serial & 0b01000000) != 0;
        this.genNmis            =         (serial & 0b10000000) != 0;
    }

    public void deserializeMask(byte serial) {
        this.grayscale      = (serial & 0b00000001) != 0;
        this.clipBackground = (serial & 0b00000010) != 0;
        this.clipSprites    = (serial & 0b00000100) != 0;
        this.showBackground = (serial & 0b00001000) != 0;
        this.showSprites    = (serial & 0b00010000) != 0;
        this.emRed          = (serial & 0b00100000) != 0;
        this.emGreen        = (serial & 0b01000000) != 0;
        this.emBlue         = (serial & 0b10000000) != 0;
    }

}
