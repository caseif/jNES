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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static net.caseif.jnes.emulation.cpu.CpuTestHelper.loadPrg;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdditionTest {

    private static CpuInterpreter ci;

    @BeforeAll
    public static void init() throws IOException {
        ci = loadPrg("/cpu_tests/addition.bin");
    }

    @Test
    public void testAddition() {
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(0x02, ci.regs.getAcc());
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.CARRY));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.ZERO));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.OVERFLOW));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.NEGATIVE));

        CpuTestHelper.runCpuOnce(ci);
        assertEquals(0x01, ci.regs.getAcc());
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.CARRY));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.ZERO));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.OVERFLOW));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.NEGATIVE));

        CpuTestHelper.runCpuOnce(ci);
        assertEquals(0x80, ci.regs.getAcc());
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.CARRY));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.ZERO));
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.OVERFLOW));
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.NEGATIVE));

        CpuTestHelper.runCpuOnce(ci);
        assertEquals(0x00, ci.regs.getAcc());
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.CARRY));
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.ZERO));
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.OVERFLOW));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.NEGATIVE));

        CpuTestHelper.runCpuOnce(ci);
        assertEquals(0x02, ci.regs.getAcc());
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.CARRY));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.ZERO));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.OVERFLOW));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.NEGATIVE));

        CpuTestHelper.runCpuOnce(ci);
        assertEquals(0x80, ci.regs.getAcc());
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.CARRY));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.ZERO));
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.OVERFLOW));
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.NEGATIVE));

        CpuTestHelper.runCpuOnce(ci);
        assertEquals(0x00, ci.regs.getAcc());
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.CARRY));
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.ZERO));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.OVERFLOW));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.NEGATIVE));
    }

}
