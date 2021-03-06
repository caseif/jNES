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

package net.caseif.jnes.model.cpu;

public enum InterruptType {

    RESET(0xFFFA, false, true, false, false),
    NMI(0xFFFC, false, false, false, false),
    IRQ(0xFFFE, true, true, false, true),
    BRK(0xFFFE, false, true, true, true);

    private final int vectorLocation;
    private final boolean maskable;
    private final boolean pushPc;
    private final boolean setB;
    private final boolean setI;

    InterruptType(int vectorLocation, boolean maskable, boolean pushPc, boolean setB, boolean setI) {
        this.vectorLocation = vectorLocation;
        this.maskable = maskable;
        this.pushPc = pushPc;
        this.setB = setB;
        this.setI = setI;
    }

    public int getVectorLocation() {
        return vectorLocation;
    }

    public boolean isMaskable() {
        return maskable;
    }

    public boolean doesPushPc() {
        return pushPc;
    }

    public boolean doesSetB() {
        return setB;
    }

    public boolean doesSetI() {
        return setI;
    }

}
