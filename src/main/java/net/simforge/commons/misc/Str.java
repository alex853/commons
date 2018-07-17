package net.simforge.commons.misc;

public class Str {

    /**
     * Returns string which is filled in with spaces. The method is used for alignments in tables drawn in console.
     *
     * @param length count of spaces
     * @return resulted string of spaces
     */
    public static String s(int length) {
        String s = "";
        while (s.length() < length) {
            s = " " + s;
        }
        return s;
    }

    public static String al(String s, int length) {
        while (s.length() < length) {
            s = s + " ";
        }
        return s;
    }

    public static String ar(String s, int length) {
        while (s.length() < length) {
            s = " " + s;
        }
        return s;
    }

    public static String z(int v, int length) {
        String s = String.valueOf(v);
        while (s.length() < length) {
            s = "0" + s;
        }
        return s;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    /**
     * Mask null
     *
     * @return empty string if v is null, else - v
     */
    public static String mn(String v) {
        return Misc.mn(v, "");
    }

    public static String limit(String s, int maxLength) {
        if (s == null) {
            return null;
        }

        if (s.length() <= maxLength) {
            return s;
        }

        return s.substring(0, maxLength);
    }
}
