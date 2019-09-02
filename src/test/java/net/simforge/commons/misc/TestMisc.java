package net.simforge.commons.misc;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

public class TestMisc {
    @Test
    public void test_mn() {
        Object defaultValue = new Object();
        Object anotherValue = new Object();

        assertEquals(defaultValue, Misc.mn(null, defaultValue));
        assertEquals(anotherValue, Misc.mn(anotherValue, defaultValue));
    }

    @Test
    public void test_equals() {
        Object o1 = new Object();
        Object o2 = new Object();

        assertTrue(Misc.equal(null, null));
        assertTrue(Misc.equal(o1, o1));
        assertFalse(Misc.equal(o1, o2));
        assertFalse(Misc.equal(null, o2));
        assertFalse(Misc.equal(o1, null));
    }

    @Test
    public void test_sleep() {
        long started = System.currentTimeMillis();
        Misc.sleep(1000);
        long duration = System.currentTimeMillis() - started;
        assertTrue(1000 <= duration && duration <= 1050);
    }

    @Test
    public void test_sleep_interrupted() throws InterruptedException {
        final Object[] status = new Object[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    status[0] = "Running";
                    Misc.sleep(10000);
                    status[0] = new Exception("Test failed");
                } catch (RuntimeException e) {
                    status[0] = e;
                } catch (Exception e) {
                    status[0] = e;
                }
            }
        });
        thread.start();

        // wait till
        while (status[0] == null) {
            Thread.sleep(50);
        }

        Thread.sleep(1000);
        thread.interrupt();

        while (!(status[0] instanceof Exception)) {
            Thread.sleep(50);
        }

        Exception e = (Exception) status[0];

        assertNotNull(e.getCause());
        assertEquals(InterruptedException.class, e.getCause().getClass());
    }

    @Test
    public void test_stackTraceToString() {
        String str = Misc.stackTraceToString(Thread.currentThread().getStackTrace());
        assertTrue(str.contains("TestMisc.test_stackTraceToString"));
    }

    @Test
    public void test_random_10_20() {
        _test_random_int2int(10, 20);
    }

    @Test
    public void test_random_m10_20() {
        _test_random_int2int(-10, 20);
    }

    @Test
    public void test_random_m20_m10() {
        _test_random_int2int(-20, -10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_random_inverted() {
        Misc.random(20, 10);
    }

    private void _test_random_int2int(int from, int to) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < 1000000; i++) {
            int random = Misc.random(from, to);
            min = Math.min(min, random);
            max = Math.max(max, random);
        }

        assertEquals(from, min);
        assertEquals(to, max);
    }
}
