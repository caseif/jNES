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

import net.caseif.jnes.model.Cartridge;
import net.caseif.jnes.util.IoHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoadStoreTest {

    private static CpuInterpreter ci;

    @BeforeAll
    public static void init() throws IOException, ClassNotFoundException {
        byte[] prg = IoHelper.toBuffer(LoadStoreTest.class.getResourceAsStream("/cpu_tests/load_store.prg")).array();
        Cartridge cart = new Cartridge(prg, new byte[0], Cartridge.MirroringMode.HORIZONTAL, false, false, (byte) 0);

        ci = new CpuInterpreter(cart);
    }

    @Test
    public void testLoadStore() throws ClassNotFoundException {
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(1, ci.regs.getAcc());
        assertEquals(2, ci.regs.getX());
        assertEquals(4, ci.regs.getY());

        CpuTestHelper.runCpuOnce(ci);
        assertEquals(1, ci.memory.read(0x10));
        assertEquals(1, ci.memory.read(0x90));
        assertEquals(1, ci.memory.read(0xFF));

        CpuTestHelper.runCpuOnce(ci);
        assertEquals(1, ci.memory.read(0x12));
        assertEquals(1, ci.memory.read(0x92));
        assertEquals(1, ci.memory.read(0x01));
        assertEquals(1, ci.memory.read(0xA0));
        assertEquals(1, ci.memory.read(0x00));
        assertEquals(1, ci.memory.read(0x10));

        CpuTestHelper.runCpuOnce(ci);
        assertEquals(2, ci.memory.read(0x10));
        assertEquals(2, ci.memory.read(0x90));
        assertEquals(2, ci.memory.read(0xFF));

        CpuTestHelper.runCpuOnce(ci);
        assertEquals(2, ci.memory.read(0x14));
        assertEquals(2, ci.memory.read(0x94));
        assertEquals(2, ci.memory.read(0x03));
        assertEquals(2, ci.memory.read(0xA0));
        assertEquals(2, ci.memory.read(0x00));
        assertEquals(2, ci.memory.read(0x20));
    }

}
