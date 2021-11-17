package net.simforge.commons.legacy;

import net.simforge.commons.legacy.misc.Settings;
import net.simforge.commons.misc.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.TreeMap;
import java.text.DecimalFormat;

public class BM {
    private static final boolean bmEnabled = Boolean.parseBoolean(Settings.get("BMEnabled"));

    private static final ThreadLocal<BMContext> bmContext = new ThreadLocal<>();

    public static void init() {
        if (!bmEnabled) return;
        init("common");
    }

    public static void init(String loggerName) {
        if (!bmEnabled) return;
        BMContext bmc = getBMContext();
        if (bmc.root == null) {
            bmc.root = Sample.createRoot(point(getStackTrace()));
            bmc.loggerName = loggerName;
            bmc.curr = bmc.root;
            bmc.root.start();
        }
    }

    public static void init(String loggerName, long loggingPeriod) {
        init(loggerName);
        setLoggingPeriod(loggingPeriod);
    }

    @SuppressWarnings("WeakerAccess")
    public static void setLoggingPeriod(long loggingPeriod) {
        if (!bmEnabled) return;
        BMContext bmc = getBMContext();
        bmc.loggingPeriod = loggingPeriod;
    }

    public static void start() {
        if (!bmEnabled) return;
        start(point(getStackTrace()));
    }

    public static void start(String point) {
        if (!bmEnabled) return;

        BMContext bmc = getBMContext();
        if (bmc.root == null) {
            return;
        }

        bmc.curr = bmc.curr.getChild(point);
        if (!bmc.curr.isStarted()) {
            bmc.curr.start();
        } else {
            logError(bmc, "Curr is started. curr = " + bmc.curr.point + ", point = " + point);
        }
    }

    public static void stop() {
        if (!bmEnabled) return;
        BMContext bmc = getBMContext();
        if (bmc.root == null) {
            return; // it seems BM is not initialized
        }

        if (bmc.curr.isStarted() && !bmc.curr.isStopped()) {
            bmc.curr.stop();
            if (bmc.curr != bmc.root) {
                bmc.curr = bmc.curr.getParent();
            }
        } else {
            logError(bmc, "Curr is in wrong state: curr = " + bmc.curr.point + " started = " + bmc.curr.isStarted() + " stopped = " + bmc.curr.isStopped());
        }
    }

    private static void logError(BMContext bmc, String msg) {
        Logger logger = getLogger(bmc);
        //noinspection ThrowableInstanceNeverThrown
        logger.error(msg, new Exception());
    }

    private static StackTraceElement getStackTrace() {
        StackTraceElement[] all = Thread.currentThread().getStackTrace();
        boolean bmFound = false;
        for (StackTraceElement element : all) {
            boolean isBm = element.getClassName().equals(BM.class.getName());

            if (!bmFound) {
                if (isBm) {
                    bmFound = true;
                }
            } else if (!isBm) {
                return element;
            }
        }

        throw new IllegalStateException();
    }

    private static String point(StackTraceElement trace) {
        return trace.getClassName() + "#" + trace.getMethodName();
    }

    @SuppressWarnings("WeakerAccess")
    public synchronized static void logNow() {
        if (!bmEnabled) return;
        BMContext bmc = getBMContext();
        Logger logger = getLogger(bmc);
        logger.info("=== " + bmc.loggerName + " ===================================================================================================================================");
        log(logger, 0, bmc.root, bmc.root);
        logger.info("--- end ---------");
    }

    private static Logger getLogger(BMContext bmc) {
        return LoggerFactory.getLogger(bmc.loggerName + "/BM");
    }

    public static void logPeriodically(boolean reset) {
        if (!bmEnabled) return;
        BMContext bmc = getBMContext();
        if (System.currentTimeMillis() - bmc.lastLogTS >= bmc.loggingPeriod) {
            logNow();
            if (reset) {
                reset();
            }
            bmc.lastLogTS = System.currentTimeMillis();
        }
    }

    private static void log(Logger logger, int level, Sample root, Sample sample) {
        if (sample.getInvocations() == 0 && sample.parent != null) {
            return;
        }

        int avg = (int) (sample.getElapsed() / (double) sample.getInvocations());
        int avg2 = (int) (sample.getElapsed() / (double) root.getInvocations());
        int percent = (int) Math.round((sample.getElapsed() / (double) root.getElapsed()) * 100);
        logger.info(
                Str.s(level * 4) +
                        Str.al(removePath(sample.point), 90 - level * 4) +
                        Str.ar(duration(sample.getElapsed()), 9) +
                        Str.ar(percent + "%", 6) +
                        Str.ar(String.valueOf(sample.getInvocations()), 8) +
                        Str.ar(duration(avg), 9) +
                        Str.ar(duration(avg2), 9) +
                        Str.ar(duration(sample.max), 9));
        for (Sample eachChild : sample.children.values()) {
            log(logger, level + 1, root, eachChild);
        }
    }

    private static String removePath(String s) {
//        if (s.startsWith("net.simforge.")) {
//            return s.substring("net.simforge.".length());
//        }
        return s;
    }

    private static String duration(long time) {
        if (time < 500) {
            return time + "ms";
        } else if (time < 10000) {
            return new DecimalFormat("0.0").format(time / 1000.0) + "s";
        } else {
            int m = (int) (time / 60000);
            int s = (int) Math.round((time - m * 60000) / 1000.0);
            if (m == 0) {
                return s + "s";
            } else {
                return m + "m " + s + "s";
            }
        }
    }

    private static BMContext getBMContext() {
        BMContext bmc = bmContext.get();
        if (bmc == null) {
            bmc = new BMContext();
            bmContext.set(bmc);
        }
        return bmc;
    }

    private static void reset() {
        long now = System.currentTimeMillis();
        BMContext bmc = getBMContext();
        reset(bmc.root, now);
    }

    private static void reset(Sample each, long now) {
        if (each.isStarted() && !each.isStopped()) {
            each.invocations = 0;
            each.elapsed = 0;
            each.started = now;
            each.stopped = 0;
            each.max = 0;
        } else {
            each.invocations = 0;
            each.elapsed = 0;
            each.started = 0;
            each.stopped = 0;
            each.max = 0;
        }
        for (Sample eachChild : each.children.values()) {
            reset(eachChild, now);
        }
    }

    private static class BMContext {
        Sample root;
        Sample curr;
        String loggerName;
        long lastLogTS;
        long loggingPeriod = TimeUnit.MINUTES.toMillis(1);
    }

    private static class Sample {
        private String point;

        private int invocations;
        private long elapsed;
        private long max;

        private long started;
        private long stopped;

        private Sample parent;
        private Map<String, Sample> children = new TreeMap<>();

        Sample(String point, Sample parent) {
            this.point = point;
            this.parent = parent;
        }

        void start() {
            started = System.currentTimeMillis();
        }

        void stop() {
            stopped = System.currentTimeMillis();

            long eachElapsed = stopped - started;
            invocations++;
            elapsed += eachElapsed;
            if (max < eachElapsed) {
                max = eachElapsed;
            }
            stopped = 0;
            started = 0;
        }

        boolean isStarted() {
            return started != 0;
        }

        boolean isStopped() {
            return stopped != 0;
        }

        Sample getParent() {
            return parent;
        }

        static Sample createRoot(String point) {
            return new Sample(point, null);
        }

        Sample getChild(String point) {
            Sample child = children.get(point);
            if (child == null) {
                child = new Sample(point, this);
                children.put(point, child);
            }
            return child;
        }

        long getElapsed() {
            if (isStarted() && !isStopped()) {
                return elapsed + (System.currentTimeMillis() - started);
            } else {
                return elapsed;
            }
        }

        int getInvocations() {
            if (isStarted() && !isStopped()) {
                return invocations + 1;
            } else {
                return invocations;
            }
        }
    }
}
