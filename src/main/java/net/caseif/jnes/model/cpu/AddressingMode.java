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

public enum AddressingMode {

    /**
     * Immediate.
     */
    IMM(2),
    /**
     * Zero-page.
     */
    ZRP(2),
    /**
     * Zero-page X.
     */
    ZPX(2),
    /**
     * Zero-page Y.
     */
    ZPY(2),
    /**
     * Absolute.
     */
    ABS(3),
    /**
     * Absolute X.
     */
    ABX(3),
    /**
     * Absolute Y.
     */
    ABY(3),
    /**
     * Indirect.
     */
    IND(3),
    /**
     * Indirect X.
     */
    IZX(2),
    /**
     * Indirect Y.
     */
    IZY(2),
    /**
     * Relative.
     */
    REL(2),
    /**
     * Implied.
     */
    IMP(1);

    private final int length;

    AddressingMode(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

}
