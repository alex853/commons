package net.simforge.commons.misc;

import junit.framework.TestCase;

public class TestMisc extends TestCase {
    public void test_mn() {
        Object defaultValue = new Object();
        Object anotherValue = new Object();

        assertEquals(defaultValue, Misc.mn(null, defaultValue));
        assertEquals(anotherValue, Misc.mn(anotherValue, defaultValue));
    }

    public void test_equals() {
        Object o1 = new Object();
        Object o2 = new Object();

        assertTrue(Misc.equal(null, null));
        assertTrue(Misc.equal(o1, o1));
        assertFalse(Misc.equal(o1, o2));
        assertFalse(Misc.equal(null, o2));
        assertFalse(Misc.equal(o1, null));
    }

    public void test_sleep() {
        long started = System.currentTimeMillis();
        Misc.sleep(1000);
        long duration = System.currentTimeMillis() - started;
        assertTrue(1000 <= duration && duration <= 1050);
    }

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

    public void test_stackTraceToString() {
        String str = Misc.stackTraceToString(Thread.currentThread().getStackTrace());
        assertTrue(str.contains("TestMisc.test_stackTraceToString"));
    }
}
