package net.simforge.commons.misc;

import net.simforge.commons.legacy.BM;

public class Misc {

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted during sleep", e);
        }
    }

    public static void sleepBM(long ms) {
        BM.start("Misc.sleepBM");
        try {
            sleep(ms);
        } finally {
            BM.stop();
        }
    }

    public static String stackTraceToString(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement eachStackTrace : stackTrace) {
            sb.append("\t").append(eachStackTrace).append("\r\n");
        }
        return sb.toString();
    }

    public static String messagesBr(Throwable t) {
        String result = "";
        while (t != null) {
            String message = t.getMessage();
            if (!Str.isEmpty(message)) {
                result += message + "<br/>";
            }
            t = t.getCause();
        }
        return result;
    }

    public static <T> T mn(T value, T defaultValue) {
        if (value == null)
            return defaultValue;
        return value;
    }

    public static boolean equal(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (o1 == null) {
            return false;
        }
        return o1.equals(o2);
    }

    public static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int random(int from, int to) {
        if (from > to) {
            throw new IllegalArgumentException("found illegal range for int-to-int random");
        }
        return from + (int) ((to - from + 1) * Math.random());
    }

}
