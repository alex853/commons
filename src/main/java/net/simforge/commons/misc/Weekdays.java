package net.simforge.commons.misc;

import java.time.DayOfWeek;

public class Weekdays {
    private boolean[] days = new boolean[7];

    private Weekdays(Weekdays src) {
        System.arraycopy(src.days, 0, days, 0, 7);
    }

    private Weekdays(boolean mon, boolean tue, boolean wen, boolean thu, boolean fri, boolean sat, boolean sun) {
        days[0] = mon;
        days[1] = tue;
        days[2] = wen;
        days[3] = thu;
        days[4] = fri;
        days[5] = sat;
        days[6] = sun;
    }

    public String toString() {
        return s(0) + s(1) + s(2) + s(3) + s(4) + s(5) + s(6);
    }

    private String s(int day) {
        return days[day] ? String.valueOf(day + 1) : "_";
    }

    public static Weekdays valueOf(String s) {
        return new Weekdays(p(s, 0), p(s, 1), p(s, 2), p(s, 3), p(s, 4), p(s, 5), p(s, 6));
    }

    public static Weekdays wholeWeek() {
        return new Weekdays(true, true, true, true, true, true, true);
    }

    private static boolean p(String s, int day) {
        char c = s.charAt(day);
        if (c == '_') {
            return false;
        }
        if (c == '0' + (day + 1)) {
            return true;
        }
        throw new IllegalArgumentException("Could not parse day " + (day + 1) + " in weekdays string '" + s + "'");
    }

    public boolean isOn(int dayOfWeek) {
        return days[dayOfWeek - 1];
    }

    public boolean isOn(DayOfWeek dayOfWeek) {
        return isOn(dayOfWeek.getValue());
    }

    public Weekdays off(int dayOfWeek) {
        Weekdays result = new Weekdays(this);
        result.days[dayOfWeek - 1] = false;
        return result;
    }

    public Weekdays off(DayOfWeek dayOfWeek) {
        return off(dayOfWeek.getValue());
    }
}
