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

package net.caseif.jnes;

import net.caseif.jnes.disassembly.RomDumper;
import net.caseif.jnes.loader.RomLoader;
import net.caseif.jnes.model.Cartridge;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: java -jar jnes.jar <task> <input ROM> [output file]");
            return;
        }

        String cmd = args[0].toLowerCase();

        switch (cmd) {
            case "disassemble": {
                if (args.length < 3) {
                    System.out.println("Output file required.");
                    return;
                }

                Cartridge cart;
                try (FileInputStream input = new FileInputStream(args[1])) {
                    cart = new RomLoader(input).load();
                }

                new RomDumper(cart).dump(new FileOutputStream(args[2]));

                break;
            }
            case "emulate": {
                System.err.println("Not yet implemented.");

                break;
            }
            default: {
                System.err.println("Invalid task!");

                break;
            }
        }
    }

}