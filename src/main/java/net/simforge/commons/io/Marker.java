package net.simforge.commons.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Marker {
    private static final Logger logger = LoggerFactory.getLogger(Marker.class.getName());

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private File file;

    public Marker(String name) {
        init("./markers/" + name);
    }

    private void init(String filename) {
        this.file = new File(filename);
        //noinspection ResultOfMethodCallIgnored
        this.file.getParentFile().mkdirs();
    }

    public String getString() {
        try {
            return IOHelper.loadFile(file);
        } catch (Exception e) {
            logger.warn("Can't load marker value from file " + file.getAbsolutePath());
            return null;
        }
    }

    public void setString(String s) {
        try {
            IOHelper.saveFile(file, s);
        } catch (IOException e) {
            String msg = "Can't save marker value to file " + file.getAbsolutePath();
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public int getInt() {
        try {
            String s = IOHelper.loadFile(file);
            return Integer.valueOf(s);
        } catch (Exception e) {
            logger.warn("Can't load marker value from file " + file.getAbsolutePath());
            return -1;
        }
    }

    public void setInt(int id) {
        try {
            IOHelper.saveFile(file, String.valueOf(id));
        } catch (IOException e) {
            String msg = "Can't save marker value to file " + file.getAbsolutePath();
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public Date getDate() {
        try {
            String s = IOHelper.loadFile(file);
            return dateFormat.parse(s);
        } catch (Exception e) {
            logger.warn("Can't load marker value from file " + file.getAbsolutePath());
            return null;
        }
    }

    public void setDate(Date date) {
        try {
            IOHelper.saveFile(file, dateFormat.format(date));
        } catch (IOException e) {
            String msg = "Can't save marker value to file " + file.getAbsolutePath();
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }
}
