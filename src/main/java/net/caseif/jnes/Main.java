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

import javax.annotation.Nullable;
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    private static final DirectoryStream.Filter<Path> ASM_FILTER = p -> p.getFileName().toString().endsWith(".asm");

    public static void main(String[] args) throws IOException {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: java -jar jnes.jar <task> <input ROM> [output file]");
            return;
        }

        String cmd = args[0].toLowerCase();

        switch (cmd) {
            case "assemble": {
                Path inputPath = Paths.get(args[1]);
                Path outputPath = null;

                if (args.length == 3) {
                    outputPath = Paths.get(args[2]);
                }

                try {
                    assemble(inputPath, outputPath);
                } catch (MalformedAssemblyException ex) {
                    ex.printStackTrace();
                    System.err.println("Failed to assemble program.");
                }

                break;
            }
            case "disassemble": {
                Path inputPath = Paths.get(args[1]);
                Path outputPath;

                if (args.length == 3) {
                    outputPath = Paths.get(args[2]);
                } else {
                    String fileName = parseFileName(inputPath);
                    outputPath = inputPath.getParent().resolve(fileName + ".asm");
                }

                PrgDisassembler disassembler = new PrgDisassembler();

                try (InputStream inputStream = Files.newInputStream(inputPath)) {
                    disassembler.read(inputStream);
                }

                try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
                    disassembler.dump(outputStream);
                }

                break;
            }
            case "dump": {
                Path inputPath = Paths.get(args[1]);
                Path outputPath;

                if (args.length == 3) {
                    outputPath = Paths.get(args[2]);
                } else {
                    String fileName = parseFileName(inputPath);
                    outputPath = inputPath.getParent().resolve(fileName + ".nesa");
                }

                Cartridge cart;
                try (InputStream inputStream = Files.newInputStream(inputPath)) {
                    cart = new RomLoader().load(inputStream);
                }

                try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
                    new RomDumper(cart).dump(outputStream);
                }

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

    private static void assemble(Path inputPath, @Nullable Path outputPath) throws IOException, MalformedAssemblyException {
        if (!Files.exists(inputPath)) {
            throw new IOException("No such file " + inputPath.toString() + ".");
        }

        if (Files.isDirectory(inputPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputPath, ASM_FILTER)) {
                for (Path child : stream) {
                    assemble(child, null);
                }
            }
        } else {
            PrgAssembler assembler = new PrgAssembler();

            try (InputStream input = Files.newInputStream(inputPath)) {
                assembler.read(input);
            }

            if (outputPath == null) {
                String fileName = parseFileName(inputPath);
                outputPath = inputPath.getParent().resolve(fileName + ".bin");
            }

            assembler.assemble(Files.newOutputStream(outputPath));
        }
    }

    private static String parseFileName(Path inputPath) {
        if (!inputPath.getFileName().toString().contains(".")) {
            return inputPath.getFileName().toString();
        }

        String[] split = inputPath.getFileName().toString().split("\\.");
        StringBuilder fileNameB = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            fileNameB.append(split[i]);
        }
        return fileNameB.toString();
    }

}
