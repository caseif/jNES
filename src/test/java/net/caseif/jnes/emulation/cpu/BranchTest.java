package net.caseif.jnes.emulation.cpu;

import static net.caseif.jnes.emulation.cpu.CpuTestHelper.loadPrg;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BranchTest {

    private static CpuInterpreter ci;

    @BeforeAll
    public static void init() throws IOException {
        ci = loadPrg("/cpu_tests/branch.bin");
    }

    @Test
    public void testBranch() {
        // test JMP
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(1, ci.regs.getAcc());
        assertEquals(0, ci.regs.getX());

        // test BEQ
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(1, ci.regs.getAcc());
        assertEquals(0, ci.regs.getY());
    }

}
