package net.simforge.commons.legacy.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

public class LogHelper {
    private static Map<String, Logger> loggers = new HashMap<String, Logger>();
    private static ThreadLocal<Logger> threadLocalLog = new ThreadLocal<Logger>();

    public static Logger getLogger(String loggerName) {
        Logger logger = loggers.get(loggerName);
        if (logger != null)
            return logger;

        logger = Logger.getLogger(loggerName);
        loggers.put(loggerName, logger);

        String pattern = "logs/%d " + loggerName + ".log";

        Formatter formatter = new LogMessageFormatter();

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);

        RollingFileHandler rollingFileHandler = new RollingFileHandler(pattern, "day");
        rollingFileHandler.setFormatter(formatter);

        Handler handlers[] = logger.getHandlers();

        for (Handler handler : handlers) {
            logger.removeHandler(handler);
        }

        logger.addHandler(consoleHandler);
        logger.addHandler(rollingFileHandler);
        logger.setUseParentHandlers(false);

        return logger;
    }

    public static void initThreadLog(String loggerName) {
        threadLocalLog.set(getLogger(loggerName));
    }
}
