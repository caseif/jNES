package net.caseif.jnes.disassembler.model;

public enum AddressingMode {

    /**
     * Immediate.
     */
    IMM(2),
    /**
     * Zero-page.
     */
    ZRP(2),
    /**
     * Zero-page X.
     */
    ZPX(2),
    /**
     * Zero-page Y.
     */
    ZPY(2),
    /**
     * Absolute.
     */
    ABS(3),
    /**
     * Absolute X.
     */
    ABX(3),
    /**
     * Absolute Y.
     */
    ABY(3),
    /**
     * Indirect.
     */
    IND(3),
    /**
     * Indirect X.
     */
    IZX(2),
    /**
     * Indirect Y.
     */
    IZY(2),
    /**
     * Relative.
     */
    REL(2),
    /**
     * Implied.
     */
    IMP(1);

    private final int length;

    AddressingMode(int length) {
        this.length = length;
    }

    int getLength() {
        return length;
    }

}
