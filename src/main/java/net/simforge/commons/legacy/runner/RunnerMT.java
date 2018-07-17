package net.simforge.commons.legacy.runner;

import net.simforge.commons.io.IOHelper;
import net.simforge.commons.legacy.logging.LogHelper;
import net.simforge.commons.runtime.RunningMarker;
import net.simforge.commons.runtime.ThreadMonitor;
import net.simforge.commons.misc.Misc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class RunnerMT {
    private static Logger log;

    public static void main(String[] args) throws IOException {
        String runnerName = "runner-mt";
        boolean setStopSignal = false;

        for (String arg : args) {
            if (arg.equals("k:stop")) {
                setStopSignal = true;
            } else {
                runnerName = arg;
            }
        }

        log = LoggerFactory.getLogger(runnerName);

        if (getStopSignal(runnerName).exists()) {
            log.info("Stop Signal EXISTS! Wait till finish of previous copy or remove it manully. Exiting.");
            return;
        }

        LogHelper.initThreadLog(runnerName);
        ThreadMonitor.setLog(log);

        if (setStopSignal) {
            setStopSignal(runnerName);
            log.info("Stop Signal SET");
        } else {
            run(runnerName);
        }
    }

    private static void setStopSignal(String runnerName) throws IOException {
        File stopSignal = getStopSignal(runnerName);
        IOHelper.saveFile(stopSignal, "Stop Signal");
    }

    private static void run(String runnerName) throws IOException {
        RunningMarker.lock(runnerName);

        log.info("Starting...");

        List<Thread> threads;
        try {
            threads = makeTaskThreads(runnerName);
        } catch (Exception e) {
            log.error("Unable to make task threads", e);
            throw new RuntimeException(e);
        }

        while (true) {
            boolean stopSignal = getStopSignal(runnerName).exists();

            if (stopSignal) {
                log.info("Stop Signal RECEIVED");

                log.info("Requesting to stop monitoring threads...");
                ThreadMonitor.requestStop(60000);

                log.info("Interrupting all non-stopped threads...");
                for (Thread thread : threads) {
                    if (thread.isAlive()) {
                        thread.interrupt();
                    }
                }
                break;
            }

            Misc.sleep(1000);
        }

        if (getStopSignal(runnerName).delete()) {
            log.info("Stop Signal deleted");
        } else {
            log.warn("Unable to delete Stop Signal");
        }

        log.info("STOPPED");
    }

    private static List<Thread> makeTaskThreads(String runnerName) throws IOException {
        List<Thread> threads = new ArrayList<Thread>();
        String propertiesFilename = "./" + runnerName + ".properties";
        String propertiesString = IOHelper.loadFile(new File(propertiesFilename));
        String[] strs = propertiesString.split("\n");
        for (String str : strs) {
            str = str.replace('\r', ' ').trim();
            if (str.length() == 0 || str.startsWith(";") || str.startsWith("#"))
                continue;

            int index = str.indexOf(' ');
            String classname = index != -1 ? str.substring(0, index).trim() : str;
            String params = index != -1 ? str.substring(index).trim() : "";

            log.info("Task " + classname + " | " + params);

            TargetInfo targetInfo = new TargetInfo(classname, params);
            Thread targetThread = targetInfo.createThread();
            if (targetThread == null) {
                continue;
            }
            threads.add(targetThread);
            targetThread.start();
        }
        return threads;
    }

    private static File getStopSignal(String runnerName) {
        return new File("./" + runnerName + ".stop-signal");
    }

    private static class TargetInfo {
        private String className;
        private String argsStr;

        public TargetInfo(String className, String argsStr) {
            this.className = className;
            this.argsStr = argsStr;
        }

        public Thread createThread() {
            final String[] args = argsStr.split(" ");
            try {
                final Class<?> clazz = Class.forName(className);
                final Object clazzInstance = clazz.newInstance();

                @SuppressWarnings({"InstantiatingObjectToGetClassObject"})
                final Method mainMethod = clazz.getMethod("main", new String[]{}.getClass());

                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            mainMethod.invoke(clazzInstance, new Object[]{args});
                        } catch (Exception e) {
                            log.error("Unable to invoke task " + className, e);
                            throw new RuntimeException(e);
                        }
                    }
                };

                Thread thread = new Thread(runnable);
                thread.setName(className);
                return thread;
            } catch (Exception e) {
                log.error("Unable to start task " + className, e);
                return null;
            }
        }
    }
}
