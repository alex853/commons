package net.simforge.commons.legacy.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.Date;
import java.text.MessageFormat;
import java.io.StringWriter;
import java.io.PrintWriter;

class LogMessageFormatter extends Formatter {

    Date dat = new Date();
    private final static String format = "{0,date,yyyy-MM-dd HH:mm:ss.SSS}";
    private MessageFormat formatter;

    private Object args[] = new Object[1];

    // Line separator string.  This is the value of the line.separator
    // property at the moment that the SimpleFormatter was created.
    private String lineSeparator = System.getProperty("line.separator");

    /**
     * Format the given LogRecord.
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        // Minimize memory allocations here.
        dat.setTime(record.getMillis());
        args[0] = dat;
        StringBuffer text = new StringBuffer();
        if (formatter == null) {
            formatter = new MessageFormat(LogMessageFormatter.format);
        }
        formatter.format(args, text, null);
        sb.append(text);
        sb.append(" ");
        sb.append(record.getLevel().getLocalizedName());
        sb.append(" [").append(record.getLoggerName()).append("] ");
        String message = formatMessage(record);
        sb.append(message);
        sb.append(lineSeparator);
        //noinspection ThrowableResultOfMethodCallIgnored
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                //noinspection ThrowableResultOfMethodCallIgnored
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
                // no op
            }
        }
        return sb.toString();
    }
}
