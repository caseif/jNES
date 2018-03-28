package net.caseif.jnes.disassembler.model;

public class Cartridge {

    private final byte[] prg;
    private final byte[] chr;
    private final byte[] trainer;
    private final MirroringMode mirrorMode;
    private final boolean cartridgePrgRam;
    private final boolean ignoreMirroringControl;
    private final byte mapper;

    public Cartridge(byte[] prg, byte[] chr, byte[] trainer, MirroringMode mirrorMode, boolean cartridgePrgRam,
                     boolean ignoreMirroringControl, byte mapper) {
        this.prg = prg;
        this.chr = chr;
        this.trainer = trainer;
        this.mirrorMode = mirrorMode;
        this.cartridgePrgRam = cartridgePrgRam;
        this.ignoreMirroringControl = ignoreMirroringControl;
        this.mapper = mapper;
    }
x1`
    public enum MirroringMode {
        HORIZONTAL,
        VERTICAL
    }

}
