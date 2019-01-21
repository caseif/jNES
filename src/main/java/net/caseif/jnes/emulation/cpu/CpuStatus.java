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

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class CpuStatus {

    private final Map<Flag, Boolean> flags = Arrays.stream(Flag.values()).collect(Collectors.toMap(f -> f, f -> false));

    public boolean getFlag(Flag flag) {
        return flags.get(flag);
    }

    public void setFlag(Flag flag) {
        flags.put(flag, true);
    }

    public void clearFlag(Flag flag) {
        flags.put(flag, false);
    }

    public byte serialize() {
        return Arrays.stream(Flag.values()).map(this::shift).reduce((byte) 0, (r, b) -> (byte) (r | b));
    }

    public void deserialize(byte serial) {
        Arrays.stream(Flag.values()).forEach(f -> flags.put(f, (serial & (1 << f.getPosition())) != 0));
    }

    private byte shift(Flag flag) {
        return getFlag(flag) ? (byte) (1 << flag.getPosition()) : 0;
    }

    public enum Flag {
        CARRY(0),
        ZERO(1),
        INTERRUPT_DISABLE(2),
        // 2A03 doesn't support decimal mode
        BREAK_COMMAND(4),
        OVERFLOW(6),
        NEGATIVE(7);

        private final int pos;

        Flag(int pos) {
            this.pos = pos;
        }

        private int getPosition() {
            return pos;
        }
    }

}
