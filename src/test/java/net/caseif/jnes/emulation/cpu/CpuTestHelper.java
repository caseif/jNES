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

import net.caseif.jnes.model.Cartridge;
import net.caseif.jnes.model.cpu.Instruction;
import net.caseif.jnes.model.cpu.Mnemonic;
import net.caseif.jnes.util.IoHelper;
import net.caseif.jnes.util.exception.CpuHaltedException;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;


public class CpuTestHelper {

    static CpuInterpreter loadPrg(String file) throws IOException {
        byte[] prg = IoHelper.toBuffer(CpuTestHelper.class.getResourceAsStream(file)).array();
        byte[] prgExtended = new byte[16384];
        System.arraycopy(prg, 0, prgExtended, 0, prg.length);
        Cartridge cart = new Cartridge(prgExtended, new byte[0], Cartridge.MirroringMode.HORIZONTAL, false, false, (byte) 0);

        return new CpuInterpreter(cart);
    }

    static void runCpuOnce(CpuInterpreter ci) {
        try {
            do {
                ci.tick();
            } while (Instruction.fromOpcode(ci.peekPrg()).getMnemonic() != Mnemonic.NOP);
        } catch (CpuHaltedException ex) {
            fail("CPU halted prematurely (PC=0x" + Integer.toHexString(ci.regs.getPc()) + ").");
        }
    }

}
