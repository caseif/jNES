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

import net.caseif.jnes.assembly.PrgAssembler;
import net.caseif.jnes.disassembly.PrgDisassembler;
import net.caseif.jnes.disassembly.RomDumper;
import net.caseif.jnes.emulation.cpu.CpuInterpreter;
import net.caseif.jnes.loader.RomLoader;
import net.caseif.jnes.model.Cartridge;
import net.caseif.jnes.util.exception.CpuHaltedException;
import net.caseif.jnes.util.exception.MalformedAssemblyException;

import java.io.File;
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
            case "assemble": {
                File inputFile = new File(args[1]);
                File outputFile;

                if (args.length == 3) {
                    outputFile = new File(args[2]);
                } else {
                    String fileName = parseFileName(inputFile);
                    outputFile = new File(inputFile.getParentFile(), fileName + ".prg");
                }

                try {
                    PrgAssembler assembler = new PrgAssembler();
                    try (FileInputStream input = new FileInputStream(args[1])) {
                        assembler.read(input);
                    }

                    assembler.assemble(new FileOutputStream(outputFile));
                    break;
                } catch (MalformedAssemblyException ex) {
                    ex.printStackTrace();
                    System.err.println("Failed to assemble program.");
                    return;
                }
            }
            case "disassemble": {
                File inputFile = new File(args[1]);
                File outputFile;

                if (args.length == 3) {
                    outputFile = new File(args[2]);
                } else {
                    String fileName = parseFileName(inputFile);
                    outputFile = new File(inputFile.getParentFile(), fileName + ".asm");
                }

                PrgDisassembler disassembler = new PrgDisassembler();
                try (FileInputStream input = new FileInputStream(inputFile)) {
                    disassembler.read(input);
                }
                disassembler.dump(new FileOutputStream(outputFile));

                break;
            }
            case "dump": {
                File inputFile = new File(args[1]);
                File outputFile;

                if (args.length == 3) {
                    outputFile = new File(args[2]);
                } else {
                    String fileName = parseFileName(inputFile);
                    outputFile = new File(inputFile.getParentFile(), fileName + ".nesa");
                }

                Cartridge cart;
                try (FileInputStream input = new FileInputStream(inputFile)) {
                    cart = new RomLoader().load(input);
                }

                new RomDumper(cart).dump(new FileOutputStream(outputFile));

                break;
            }
            case "emulate": {
                Cartridge cart;
                try (FileInputStream input = new FileInputStream(args[1])) {
                    cart = new RomLoader().load(input);
                }

                CpuInterpreter ci = new CpuInterpreter(cart);

                long time = System.nanoTime();
                int cycles = 100000000;
                for (int i = 0; i < cycles; i++) {
                    try {
                        ci.tick();
                    } catch (CpuHaltedException ex) {
                        System.out.println("Halted.");
                        break;
                    }
                }
                time = System.nanoTime() - time;
                System.out.println("Average speed: " + ((double) cycles / time * 1000000000.0) + " cycles/sec");

                break;
            }
            default: {
                System.err.println("Invalid task!");

                break;
            }
        }
    }

    private static String parseFileName(File inputFile) {
        if (!inputFile.getName().contains(".")) {
            return inputFile.getName();
        }

        String[] split = inputFile.getName().split("\\.");
        StringBuilder fileNameB = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            fileNameB.append(split[i]);
        }
        return fileNameB.toString();
    }

}
