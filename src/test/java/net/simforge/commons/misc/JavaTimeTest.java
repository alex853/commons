package net.simforge.commons.misc;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalTime;

import static net.simforge.commons.misc.JavaTime.hhmmToDuration;
import static net.simforge.commons.misc.JavaTime.toHhmm;
import static org.junit.Assert.assertEquals;

public class JavaTimeTest {
    @Test
    public void testDurationToHhmm() {
        Duration duration = Duration.ofHours(1).plusMinutes(23);
        String string = toHhmm(duration);
        assertEquals("01:23", string);
    }

    @Test
    public void testHhmmToDuration() {
        String hhmm = "01:23";
        Duration duration = hhmmToDuration(hhmm);
        assertEquals(1, duration.toHours());
        assertEquals(83, duration.toMinutes());
    }

    @Test
    public void testHhmmToLocalTime() {
        String hhmm = "01:23";
        LocalTime localTime = LocalTime.parse(hhmm);
        assertEquals(1, localTime.getHour());
        assertEquals(23, localTime.getMinute());
        assertEquals(0, localTime.getSecond());
    }

    @Test
    public void testLocalTimeToHHMM() {
        LocalTime localTime = LocalTime.of(1, 23);
        String string = toHhmm(localTime);
        assertEquals("01:23", string);
    }

    @Test
    public void testLocalTimeWithSecondsToHHMM() {
        LocalTime localTime = LocalTime.of(1, 23, 45);
        String string = toHhmm(localTime);
        assertEquals("01:23", string);
    }

    @Test
    public void testDuration2H30M29S() {
        Duration originalDuration = Duration.ofHours(2).plusMinutes(30).plusSeconds(29);
        String hhmm = JavaTime.toHhmm(originalDuration);
        assertEquals("02:30", hhmm);
        Duration duration = JavaTime.hhmmToDuration(hhmm);
        assertEquals(Duration.ofHours(2).plusMinutes(30), duration);
    }
}
