package net.simforge.commons.misc;

import org.junit.Test;

import java.time.DayOfWeek;

import static org.junit.Assert.*;

public class WeekdaysTest {
    @Test
    public void valueOf_wholeWeek() {
        Weekdays weekdays = Weekdays.valueOf("1234567");

        assertTrue(weekdays.isOn(DayOfWeek.MONDAY));
        assertTrue(weekdays.isOn(1));

        assertTrue(weekdays.isOn(DayOfWeek.TUESDAY));
        assertTrue(weekdays.isOn(2));

        assertTrue(weekdays.isOn(DayOfWeek.WEDNESDAY));
        assertTrue(weekdays.isOn(3));

        assertTrue(weekdays.isOn(DayOfWeek.THURSDAY));
        assertTrue(weekdays.isOn(4));

        assertTrue(weekdays.isOn(DayOfWeek.FRIDAY));
        assertTrue(weekdays.isOn(5));

        assertTrue(weekdays.isOn(DayOfWeek.SATURDAY));
        assertTrue(weekdays.isOn(6));

        assertTrue(weekdays.isOn(DayOfWeek.SUNDAY));
        assertTrue(weekdays.isOn(7));
    }

    @Test
    public void valueOf_wednesdayAndSunday() {
        Weekdays weekdays = Weekdays.valueOf("__3___7");

        assertFalse(weekdays.isOn(DayOfWeek.MONDAY));
        assertFalse(weekdays.isOn(1));

        assertFalse(weekdays.isOn(DayOfWeek.TUESDAY));
        assertFalse(weekdays.isOn(2));

        assertTrue(weekdays.isOn(DayOfWeek.WEDNESDAY));
        assertTrue(weekdays.isOn(3));

        assertFalse(weekdays.isOn(DayOfWeek.THURSDAY));
        assertFalse(weekdays.isOn(4));

        assertFalse(weekdays.isOn(DayOfWeek.FRIDAY));
        assertFalse(weekdays.isOn(5));

        assertFalse(weekdays.isOn(DayOfWeek.SATURDAY));
        assertFalse(weekdays.isOn(6));

        assertTrue(weekdays.isOn(DayOfWeek.SUNDAY));
        assertTrue(weekdays.isOn(7));
    }

    @Test
    public void wholeWeek() {
        Weekdays weekdays = Weekdays.wholeWeek();

        assertTrue(weekdays.isOn(DayOfWeek.MONDAY));
        assertTrue(weekdays.isOn(1));

        assertTrue(weekdays.isOn(DayOfWeek.TUESDAY));
        assertTrue(weekdays.isOn(2));

        assertTrue(weekdays.isOn(DayOfWeek.WEDNESDAY));
        assertTrue(weekdays.isOn(3));

        assertTrue(weekdays.isOn(DayOfWeek.THURSDAY));
        assertTrue(weekdays.isOn(4));

        assertTrue(weekdays.isOn(DayOfWeek.FRIDAY));
        assertTrue(weekdays.isOn(5));

        assertTrue(weekdays.isOn(DayOfWeek.SATURDAY));
        assertTrue(weekdays.isOn(6));

        assertTrue(weekdays.isOn(DayOfWeek.SUNDAY));
        assertTrue(weekdays.isOn(7));
    }

    @Test
    public void immutability() {
        Weekdays weekdays1 = Weekdays.wholeWeek();
        Weekdays weekdays2 = weekdays1.off(DayOfWeek.MONDAY);

        assertTrue(weekdays1.isOn(DayOfWeek.MONDAY));
        assertTrue(weekdays1.isOn(1));

        assertFalse(weekdays2.isOn(DayOfWeek.MONDAY));
        assertFalse(weekdays2.isOn(1));
    }

    @Test
    public void offDay() {
        Weekdays weekdays = Weekdays.wholeWeek();
        weekdays = weekdays.off(DayOfWeek.MONDAY);

        assertFalse(weekdays.isOn(DayOfWeek.MONDAY));
        assertFalse(weekdays.isOn(1));

        assertTrue(weekdays.isOn(DayOfWeek.TUESDAY));
        assertTrue(weekdays.isOn(2));

        assertTrue(weekdays.isOn(DayOfWeek.WEDNESDAY));
        assertTrue(weekdays.isOn(3));

        assertTrue(weekdays.isOn(DayOfWeek.THURSDAY));
        assertTrue(weekdays.isOn(4));

        assertTrue(weekdays.isOn(DayOfWeek.FRIDAY));
        assertTrue(weekdays.isOn(5));

        assertTrue(weekdays.isOn(DayOfWeek.SATURDAY));
        assertTrue(weekdays.isOn(6));

        assertTrue(weekdays.isOn(DayOfWeek.SUNDAY));
        assertTrue(weekdays.isOn(7));
    }
}
