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

package net.caseif.jnes.emulation.cpu;

import static net.caseif.jnes.util.MathHelper.unsign;

public class CpuRegisters {

    private short pc = (short) 0x8000;
    private byte sp = (byte) 0xFF;
    private byte acc = 0x00;
    private byte x = 0x00;
    private byte y = 0x00;

    public int getPc() {
        return unsign(pc);
    }

    public int popPc() {
        return unsign(pc++);
    }

    public short getSp() {
        return unsign(sp);
    }

    public short getAcc() {
        return unsign(acc);
    }

    public short getX() {
        return unsign(x);
    }

    public short getY() {
        return unsign(y);
    }

    public void setPc(short val) {
        pc = val;
    }

    public void setSp(byte val) {
        sp = val;
    }

    public void setAcc(byte val) {
        acc = val;
    }

    public void setX(byte val) {
        x = val;
    }

    public void setY(byte val) {
        y = val;
    }

    public enum Register {
        A,
        X,
        Y,
        PC,
        SP
    }

}
