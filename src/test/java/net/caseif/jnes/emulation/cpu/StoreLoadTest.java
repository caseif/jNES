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

public class StoreLoadTest {

    private static CpuInterpreter ci;

    @BeforeAll
    public static void init() throws IOException {
        ci = loadPrg("/cpu_tests/store_load.bin");
    }

    @Test
    public void testLoadStore() {
        // ACCUMULATOR TESTS
        // test zero-page addressing
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(1, ci.memory.read(0x10));
        assertEquals(1, ci.memory.read(0x90));
        assertEquals(1, ci.memory.read(0xFF));

        // test zero-page loading
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(1, ci.regs.getAcc());
        assertEquals(1, ci.regs.getX());
        assertEquals(1, ci.regs.getY());

        // test zero-page (x-indexed) addressing
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(1, ci.memory.read(0x12));
        assertEquals(1, ci.memory.read(0x92));
        assertEquals(1, ci.memory.read(0x01));
        assertEquals(1, ci.memory.read(0xA1));
        assertEquals(1, ci.memory.read(0x02));
        assertEquals(1, ci.memory.read(0x11));

        // test zero-page (x-indexed) loading
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(6, ci.regs.getAcc());

        // test absolute addressing
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(1, ci.memory.read(0x0023));
        assertEquals(1, ci.memory.read(0x0303));
        assertEquals(1, ci.memory.read(0x0103));
        assertEquals(1, ci.memory.read(0x0203));
        assertEquals(1, ci.memory.read(0x0303));

        // test absolute (x-indexed) addressing
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(1, ci.memory.read(0x0025));
        assertEquals(1, ci.memory.read(0x0305));
        assertEquals(1, ci.memory.read(0x0105));
        assertEquals(1, ci.memory.read(0x0205));
        assertEquals(1, ci.memory.read(0x0305));
        assertEquals(1, ci.memory.read(0x0005));

        // test absolute (y-indexed) addressing
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(1, ci.memory.read(0x0026));
        assertEquals(1, ci.memory.read(0x0006));

        // test indexed indirect addressing
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(1, ci.memory.read(0x0213));
        assertEquals(1, ci.memory.read(0x1302));
        assertEquals(1, ci.memory.read(0x0302));

        // test indirect indexed addressing
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(1, ci.memory.read(0x0215));
        assertEquals(1, ci.memory.read(0x1304));

        // X REGISTER TESTS
        // test zero-page addressing
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(2, ci.memory.read(0x10));
        assertEquals(2, ci.memory.read(0x90));
        assertEquals(2, ci.memory.read(0xFF));

        // test zero-page (y-indexed) addressing
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(2, ci.memory.read(0x14));
        assertEquals(2, ci.memory.read(0x94));
        assertEquals(2, ci.memory.read(0x03));
        assertEquals(2, ci.memory.read(0xA0));
        assertEquals(2, ci.memory.read(0x00));
        assertEquals(2, ci.memory.read(0x20));

        // Y REGISTER TESTS
        // test zero-page addressing
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(4, ci.memory.read(0x10));
        assertEquals(4, ci.memory.read(0x90));
        assertEquals(4, ci.memory.read(0xFF));

        // transfer tests
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(0x42, ci.memory.read(0x08));
        assertEquals(0x52, ci.memory.read(0x09));
    }

}
