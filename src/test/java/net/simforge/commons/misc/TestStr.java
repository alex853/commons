package net.simforge.commons.misc;

import junit.framework.TestCase;

public class TestStr extends TestCase {
    public void test_s() {
        assertEquals("", Str.s(0));
        assertEquals(" ", Str.s(1));
        assertEquals("     ", Str.s(5));
    }

    public void test_al() {
        assertEquals("1", Str.al("1", 0));
        assertEquals("1", Str.al("1", 1));
        assertEquals("1 ", Str.al("1", 2));
        assertEquals("1    ", Str.al("1", 5));
    }

    public void test_ar() {
        assertEquals("1", Str.ar("1", 0));
        assertEquals("1", Str.ar("1", 1));
        assertEquals(" 1", Str.ar("1", 2));
        assertEquals("    1", Str.ar("1", 5));
    }

    public void test_z() {
        assertEquals("1", Str.z(1, 0));
        assertEquals("1", Str.z(1, 1));
        assertEquals("01", Str.z(1, 2));
        assertEquals("00001", Str.z(1, 5));
    }

    public void test_isEmpty() {
        assertTrue(Str.isEmpty(null));
        assertTrue(Str.isEmpty(""));
        assertTrue(Str.isEmpty(" "));
        assertTrue(Str.isEmpty(" \t"));
        assertFalse(Str.isEmpty("Some text"));
    }

    public void test_mn() {
        assertEquals("", Str.mn(null));
        assertEquals("", Str.mn(""));
        assertEquals(" ", Str.mn(" "));
        assertEquals("Some text", Str.mn("Some text"));
    }

    public void test_limit() {
        assertNull(Str.limit(null, 5));
        assertEquals("123", Str.limit("123", 5));
        assertEquals("12345", Str.limit("123456", 5));
    }
}
