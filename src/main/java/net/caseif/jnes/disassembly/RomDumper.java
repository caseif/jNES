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

package net.caseif.jnes.disassembly;

import net.caseif.jnes.model.Cartridge;
import net.caseif.jnes.model.cpu.Instruction;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class RomDumper {

    private final Cartridge cart;

    public RomDumper(Cartridge cart) {
        this.cart = cart;
    }

    public void dump(OutputStream output) throws IOException {
        try (Writer writer = new OutputStreamWriter(output)) {
            writer.write("Mirroring mode: " + cart.getMirroringMode().name() + "\n");
            writer.write("Ignore mirroring control: " + cart.doesIgnoreMirroringControl() + "\n");
            writer.write("Cartridge PRG RAM: " + cart.hasCartridgePrgRam() + "\n");
            writer.write("Mapper: " + cart.getMapper() + "\n");
            writer.write('\n');

            byte[] prg = cart.getPrgRom();
            byte[] chr = cart.getChrRom();

            writer.write("PRG size: " + prg.length + "\n");
            writer.write("==== BEGIN PRG DUMP ====\n");

            ByteBuffer prgBuf = ByteBuffer.wrap(prg);
            while (prgBuf.hasRemaining()) {
                try {
                    StringBuilder sb = new StringBuilder();

                    byte opcode = prgBuf.get();
                    Instruction instr = Instruction.fromOpcode(opcode);

                    sb.append(instr.getMnemonic().name()).append("_").append(instr.getAddressingMode().name());

                    if (instr.getLength() > 1) {
                        sb.append(" $");
                        for (int i = 0; i < instr.getLength() - 1; i++) {
                            sb.append(String.format("%02X", prgBuf.get()));
                        }
                    }

                    sb.append('\n');

                    writer.write(sb.toString());
                } catch (BufferUnderflowException ex) {
                    break;
                }
            }

            writer.write('\n');
            writer.write("CHR size: " + chr.length + "\n");
            writer.write("Not dumping CHR ROM\n");
        }
    }

}
