package net.caseif.jnes.emulation.cpu;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static net.caseif.jnes.emulation.cpu.CpuTestHelper.loadPrg;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MathTest {

    private static CpuInterpreter ci;

    @BeforeAll
    public static void init() throws IOException {
        ci = loadPrg("/cpu_tests/math.bin");
    }

    @Test
    public void testMath() {
        CpuTestHelper.runCpuOnce(ci);
        assertEquals(0x02, ci.regs.getAcc());
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.CARRY));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.ZERO));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.OVERFLOW));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.NEGATIVE));

        CpuTestHelper.runCpuOnce(ci);
        assertEquals(0x01, ci.regs.getAcc());
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.CARRY));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.ZERO));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.OVERFLOW));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.NEGATIVE));

        CpuTestHelper.runCpuOnce(ci);
        assertEquals(0x80, ci.regs.getAcc());
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.CARRY));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.ZERO));
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.OVERFLOW));
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.NEGATIVE));

        CpuTestHelper.runCpuOnce(ci);
        assertEquals(0x00, ci.regs.getAcc());
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.CARRY));
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.ZERO));
        assertTrue(ci.getStatus().getFlag(CpuStatus.Flag.OVERFLOW));
        assertFalse(ci.getStatus().getFlag(CpuStatus.Flag.NEGATIVE));
    }

}
