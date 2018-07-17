package net.simforge.commons.legacy.logging;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;

/**
 * File handler that supports different kind of rolling than java.util.logging.FileHandler.
 * Supported rolling methods are: by date (day).
 * <p/>
 * <p/>
 * Example of entries in the logging file (system property "java.util.logging.config.file"):
 * <p/>
 * <p/>
 * logging.RollingFileHandler.level = FINEST
 * logging.RollingFileHandler.prefix = MyApp_
 * logging.RollingFileHandler.dateFormat = yyyyMMdd
 * logging.RollingFileHandler.suffix = .log
 * logging.RollingFileHanlder.cycle=day
 * logging.RollingFileHandler.formatter = java.util.logging.SimpleFormatter
 *
 * @author $Author:$
 * @version $Revision:$ ($Date:$)
 */
class RollingFileHandler extends StreamHandler {

    /**
     * Date format to use in file name.
     */
    private static String dateFormat = "yyyy-MM-dd";

    /**
     * File pattern.
     */
    private String pattern;

    /**
     * Time cycle (for file roling)
     */
    private String cycle;

    /**
     * Time in milliseconds for the next cycle
     */
    private long nextCycle = 0;


    /**
     * Constructor.
     */
    public RollingFileHandler(String pattern, String cycle) {
        super();

        this.pattern = pattern;

        if (cycle != null) {
            if (cycle.equalsIgnoreCase("day")
                    || cycle.equalsIgnoreCase("week")
                    || cycle.equalsIgnoreCase("month")
                    || cycle.equalsIgnoreCase("year")) {
                this.cycle = cycle;
            } else {
                this.cycle = "day";
            }
        }

        openFile();
    }

    /**
     * Open existing or create new log file.
     */
    private synchronized void openFile() {
        //create file name:
        String dateString = dateFormat; //default (to note error in file name)
        Date currentDate = new Date();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
            dateString = sdf.format(currentDate);
        } catch (IllegalArgumentException iae) {
            /* ignore wrong date format */
        }

        //compute next cycle:
        Date nextDate;
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(currentDate);
        if (cycle.equalsIgnoreCase("week")) {
            gc.add(Calendar.WEEK_OF_YEAR, 1);
            nextDate = gc.getTime();
        } else if (cycle.equalsIgnoreCase("month")) {
            gc.add(Calendar.MONTH, 1);
            int month = gc.get(Calendar.MONTH);
            int year = gc.get(Calendar.YEAR);
            GregorianCalendar gc2 = new GregorianCalendar(year, month, 1);
            nextDate = gc2.getTime();
        } else if (cycle.equalsIgnoreCase("year")) {
            gc.add(Calendar.YEAR, 1);
            int year = gc.get(Calendar.YEAR);
            GregorianCalendar gc2 = new GregorianCalendar(year, 0, 1);
            nextDate = gc2.getTime();
        } else { //day by default
            gc.add(Calendar.DAY_OF_MONTH, 1);
            nextDate = gc.getTime();
        }

        //to zero time:
        gc = new GregorianCalendar();
        gc.setTime(nextDate);
        gc.set(Calendar.HOUR, 0);
        gc.set(Calendar.HOUR_OF_DAY, 0);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        nextDate = gc.getTime();

        nextCycle = nextDate.getTime();

        //create new file:
        String fileName = pattern.replace("%d", dateString);
        File file = new File(fileName);
        File parent = file.getParentFile();
        parent.mkdirs();

        //create file:
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
            }
        }

        //set log file as OutputStream:
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            setOutputStream(fos);
        } catch (FileNotFoundException fnfe) {
            reportError(null, fnfe, ErrorManager.OPEN_FAILURE);
            fnfe.printStackTrace(System.err);
            setOutputStream(System.out); //fallback stream
        }
    }

    /**
     * Overwrites super.
     */
    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        super.publish(record);
        flush();

        //check if we need to rotate
        if (System.currentTimeMillis() >= nextCycle) { //next cycle?
            role();
        }
    }

    /**
     * Role file. Close current file and possibly create new file.
     */
    private synchronized void role() {
        Level oldLevel = getLevel();
        setLevel(Level.OFF);
        super.close();

        openFile();

        setLevel(oldLevel);
    }
}
