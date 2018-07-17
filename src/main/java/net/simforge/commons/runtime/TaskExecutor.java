package net.simforge.commons.runtime;

import net.simforge.commons.io.IOHelper;
import net.simforge.commons.misc.Misc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TaskExecutor {
    private static Logger logger = LoggerFactory.getLogger(TaskExecutor.class.getName());

    public static void main(String[] args) throws IOException {
        String tasksConfig = "tasks";
        boolean setStopSignal = false;

        for (String arg : args) {
            if (arg.equals("k:stop")) {
                setStopSignal = true;
            } else {
                tasksConfig = arg;
            }
        }

        if (setStopSignal) {
            setStopSignal(tasksConfig);
        } else {
            runTasks(tasksConfig);
        }
    }

    private static void runTasks(String tasksConfig) throws IOException {
        if (getStopSignal(tasksConfig).exists()) {
            logger.warn("Stop Signal EXISTS! Wait till finish of previous copy or remove it manully. Exiting.");
            return;
        }

        List<TaskInfo> tasks = loadTasks(tasksConfig);

        ProcessLock processLock = null;
        try {

            processLock = ProcessLock.lock(tasksConfig);
            logger.debug("Process lock obtained");


            invokeStartupTask(tasks);


            List<Thread> threads = startThreads(tasks);


            boolean stopSignal;
            while (true) {
                stopSignal = getStopSignal(tasksConfig).exists();

                if (stopSignal) {
                    break;
                }

                Misc.sleep(1000);

                printMemoryReport();
            }


            if (stopSignal) {
                logger.info("Stop Signal RECEIVED");

                logger.info("Requesting to stop monitoring threads...");
                ThreadMonitor.requestStop(60000);

                logger.warn("Interrupting all non-stopped threads...");
                for (Thread thread : threads) {
                    logger.warn("    Interrupting thread " + thread);
                    if (thread.isAlive()) {
                        thread.interrupt();
                    }
                }

                if (getStopSignal(tasksConfig).delete()) {
                    logger.info("Stop Signal deleted");
                } else {
                    logger.warn("Unable to delete Stop Signal");
                }
            }


            logger.info("STOPPED");
        } catch (Throwable t) {
            logger.error("Error happened", t);
        } finally {
            invokeShutdownTask(tasks);

            if (processLock != null) {
                processLock.release();
                logger.debug("Process lock released");
            }
        }
    }

    private static List<TaskInfo> loadTasks(String tasksConfig) throws IOException {
        List<TaskInfo> tasks = new ArrayList<>();

        String propertiesFilename = "./" + tasksConfig + ".properties";
        String propertiesContent = IOHelper.loadFile(new File(propertiesFilename));

        String[] lines = propertiesContent.split("\n");
        for (String str : lines) {
            str = str.replace('\r', ' ').trim();
            if (str.length() == 0 || str.startsWith(";") || str.startsWith("#"))
                continue;

            int index = str.indexOf(' ');
            String classname = index != -1 ? str.substring(0, index).trim() : str;
            String params = index != -1 ? str.substring(index).trim() : "";

            logger.info("Task " + classname + " | " + params);

            TaskInfo taskInfo = new TaskInfo(classname, params);
            tasks.add(taskInfo);
        }

        return tasks;
    }

    private static List<Thread> startThreads(List<TaskInfo> tasks) throws IOException {
        List<Thread> threads = new ArrayList<>();

        for (TaskInfo task : tasks) {
            if (task.getClassName().startsWith(TaskInfo.startupPrefix) || task.getClassName().startsWith(TaskInfo.shutdownPrefix)) {
                continue;
            }

            Thread thread = task.createThread();
            if (thread == null) {
                continue;
            }
            threads.add(thread);

            thread.start();
        }

        return threads;
    }

    private static void invokeStartupTask(List<TaskInfo> tasks) {
        invokePrefixedTask(tasks, TaskInfo.startupPrefix);
    }

    private static void invokeShutdownTask(List<TaskInfo> tasks) {
        invokePrefixedTask(tasks, TaskInfo.shutdownPrefix);
    }

    private static void invokePrefixedTask(List<TaskInfo> tasks, String prefix) {
        for (TaskInfo task : tasks) {
            if (!task.getClassName().startsWith(prefix)) {
                continue;
            }

            task.runPrefixedRunnable();
        }
    }

    private static void setStopSignal(String tasksConfig) throws IOException {
        File stopSignal = getStopSignal(tasksConfig);
        IOHelper.saveFile(stopSignal, "Stop Signal");
        logger.info("Stop Signal SET");
    }

    private static long lastMemoryReportTs;

    private static void printMemoryReport() {
        if (lastMemoryReportTs + 10 * 60 * 1000 < System.currentTimeMillis()) {
            Runtime runtime = Runtime.getRuntime();
            long mm = runtime.maxMemory();
            long fm = runtime.freeMemory();
            long tm = runtime.totalMemory();
            String str = "Memory report: Used = " + toMB(tm - fm) + ", " + "Free = " + toMB(fm) + ", " + "Total = " + toMB(tm) + ", " + "Max = " + toMB(mm);
            logger.info(str);

            lastMemoryReportTs = System.currentTimeMillis();
        }
    }

    private static String toMB(long size) {
        return Long.toString(size / 0x100000L);
    }

    private static File getStopSignal(String tasksConfig) {
        return new File("./" + tasksConfig + ".stop-signal");
    }

    private static class TaskInfo {
        public static String startupPrefix = "startup:";
        public static String shutdownPrefix = "shutdown:";

        private String className;
        private String argsStr;

        TaskInfo(String className, String argsStr) {
            this.className = className;
            this.argsStr = argsStr;
        }

        public String getClassName() {
            return className;
        }

        Thread createThread() {
            Runnable taskRunnable = createRunnableWithProperties();
            if (taskRunnable == null) {
                taskRunnable = createRunnableWithDefaultConstructor();
            }

            Thread thread = new Thread(taskRunnable);

            if (taskRunnable instanceof BaseTask) {
                thread.setName(((BaseTask) taskRunnable).getTaskName());
            } else {
                try {
                    Class<?> clazz = Class.forName(className);
                    thread.setName(clazz.getSimpleName());
                } catch (ClassNotFoundException e) {
                    thread.setName(className);
                }
            }

            return thread;
        }

        private Runnable createRunnableWithDefaultConstructor() {
            try {
                Class<?> clazz = Class.forName(className);
                return (Runnable) clazz.newInstance();
            } catch (Throwable t) {
                logger.warn("Unable to start task " + className, t);
                return null;
            }
        }

        private Runnable createRunnableWithProperties() {
            final String[] args = argsStr.split(" ");
            Properties properties = new Properties();
            for (String arg : args) {
                int index = arg.indexOf(':');
                if (index == -1) {
                    continue;
                }

                String key = arg.substring(0, index);
                String value = arg.substring(index + 1);

                properties.put(key, value);
            }


            try {
                Class<?> clazz = Class.forName(className);
                Constructor<?> constructor = clazz.getDeclaredConstructor(Properties.class);
                return (Runnable) constructor.newInstance(properties);
            } catch (Throwable t) {
                return null;
            }
        }

        public void runPrefixedRunnable() {
            String realClassName;
            if (className.startsWith(startupPrefix)) {
                realClassName = className.substring(startupPrefix.length());
            } else if (className.startsWith(shutdownPrefix)) {
                realClassName = className.substring(shutdownPrefix.length());
            } else {
                throw new IllegalStateException();
            }

            try {
                Class<?> clazz = Class.forName(realClassName);
                Runnable taskRunnable = (Runnable) clazz.newInstance();

                taskRunnable.run();
            } catch (Exception e) {
                logger.error("Unable to run task " + realClassName, e);
                throw new RuntimeException("Unable to run task " + realClassName, e);
            }
        }
    }
}
