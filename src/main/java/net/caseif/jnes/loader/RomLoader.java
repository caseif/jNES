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

package net.caseif.jnes.loader;

import static net.caseif.jnes.util.IoHelper.toBuffer;

import net.caseif.jnes.model.Cartridge;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class RomLoader {

    private static final int MAGIC = 0x4E45531A;
    private static final int PRG_CHUNK_SIZE = 16384;
    private static final int CHR_CHUNK_SIZE = 8192;

    public Cartridge load(InputStream input) throws IOException {
        ByteBuffer buffer = toBuffer(input);

        if (buffer.getInt() != MAGIC) {
            throw new IllegalArgumentException("Bad NES header.");
        }

        int prgSize = buffer.get() * PRG_CHUNK_SIZE;
        int chrSize = buffer.get() * CHR_CHUNK_SIZE;

        byte flag6 = buffer.get();

        Cartridge.MirroringMode mirroring = (flag6 & 0b1) == 0
                ? Cartridge.MirroringMode.HORIZONTAL
                : Cartridge.MirroringMode.VERTICAL;

        boolean cartridgePrgRam = (flag6 & 0b10) != 0;

        boolean hasTrainer = (flag6 & 0b100) != 0;

        boolean ignoreMirroringControl = (flag6 & 0b1000) != 0;

        byte mapper = (byte) (flag6 >> 4);

        byte flag7 = (byte) (buffer.get() >> 4);

        mapper |= (flag7 & 0b11110000);

        // skip $8-15
        buffer.position(buffer.position() + 8);

        // skip trainer
        if (hasTrainer) {
            buffer.position(buffer.position() + 512);
        }

        byte[] prg = new byte[prgSize];
        buffer.get(prg);

        byte[] chr = new byte[chrSize];
        buffer.get(chr);

        return new Cartridge(prg, chr, mirroring, cartridgePrgRam, ignoreMirroringControl, mapper);
    }

}
