package net.caseif.jnes.model.cpu;

public enum InterruptType {

    RESET(0xFFFA, false, true, false, false),
    NMI(0xFFFC, false, false, false, false),
    IRQ(0xFFFE, true, true, false, true),
    BRK(0xFFFE, false, true, true, true);

    private final int vectorLocation;
    private final boolean maskable;
    private final boolean pushPc;
    private final boolean setB;
    private final boolean setI;

    InterruptType(int vectorLocation, boolean maskable, boolean pushPc, boolean setB, boolean setI) {
        this.vectorLocation = vectorLocation;
        this.maskable = maskable;
        this.pushPc = pushPc;
        this.setB = setB;
        this.setI = setI;
    }

    public int getVectorLocation() {
        return vectorLocation;
    }

    public boolean isMaskable() {
        return maskable;
    }

    public boolean doesPushPc() {
        return pushPc;
    }

    public boolean doesSetB() {
        return setB;
    }

    public boolean doesSetI() {
        return setI;
    }

}
