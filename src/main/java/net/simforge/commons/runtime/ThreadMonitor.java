package net.simforge.commons.runtime;

import net.simforge.commons.legacy.misc.Mailer;
import net.simforge.commons.legacy.misc.Settings;
import net.simforge.commons.misc.Misc;
import net.simforge.commons.misc.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ThreadMonitor {

    private static Logger logger = LoggerFactory.getLogger(ThreadMonitor.class.getName());
    private static MonitoringThread monitoringThread;

    private static final Map<Thread, ThreadInfo> threadInfoList = new HashMap<>();

    public static void checkin() {
        ThreadInfo threadInfo = getThreadInfo();
        threadInfo.alive();

        checkMonitorRunning();
    }

    @SuppressWarnings("WeakerAccess")
    public static void alive() {
        ThreadInfo threadInfo = getThreadInfo();
        threadInfo.alive();
    }

    public static boolean isStopRequested() {
        ThreadInfo threadInfo = getThreadInfo();
        return threadInfo.isStopRequested();
    }

    public static void requestStop(long timeout) {
        logger.info("Stop is requested");

        for (ThreadInfo threadInfo : threadInfoList.values()) {
            threadInfo.requestStop();
        }

        long waitTill = System.currentTimeMillis() + timeout;
        long lastStatus = 0;
        while (true) {
            if (System.currentTimeMillis() > lastStatus + TimeUnit.MINUTES.toMillis(5)) {
                logger.info("Waiting for stop of following threads:");
                for (Thread thread : threadInfoList.keySet()) {
                    logger.info(String.format("\tThread '%s'", thread.getName()));
                }

                lastStatus = System.currentTimeMillis();
            }

            if (threadInfoList.isEmpty()) {
                logger.info("No threads are running, it seems like we have stopped");
                break;
            }

            if (System.currentTimeMillis() > waitTill) {
                logger.warn("Exiting due to timeout");
                break;
            }

            Misc.sleepBM(100);
        }
    }

    private static ThreadInfo getThreadInfo() {
        synchronized (threadInfoList) {
            Thread thread = Thread.currentThread();
            ThreadInfo threadInfo = threadInfoList.get(thread);
            if (threadInfo == null) {
                threadInfo = new ThreadInfo(thread);
                threadInfoList.put(thread, threadInfo);
            }
            return threadInfo;
        }
    }

    @Deprecated
    public static void setLog(Logger log) {
        ThreadMonitor.logger = log;
    }

    public static void sleepBM(long timeToSleep) {
        long step;
        if (timeToSleep >= 30000) {
            step = 5000;
        } else if (timeToSleep >= 10000) {
            step = 1000;
        } else if (timeToSleep >= 1000) {
            step = 100;
        } else {
            step = 10;
        }

        long sleepTill = System.currentTimeMillis() + timeToSleep;
        while (System.currentTimeMillis() < sleepTill && !isStopRequested()) {
            Misc.sleepBM(step);

            alive();
        }
    }

    private static class ThreadInfo {
        private Reference<Thread> threadRef;
        private long lastAlive;
        private long lastLog;
        private long lastEmail;
        private boolean stopRequested;

        private ThreadInfo(Thread thread) {
            this.threadRef = new WeakReference<>(thread);
            alive();
        }

        void alive() {
            lastAlive = System.currentTimeMillis();
            lastLog = 0;
            lastEmail = 0;
        }

        @SuppressWarnings({"UnusedDeclaration"})
        Thread getThread() {
            return threadRef.get();
        }

        void notifyLogged() {
            lastLog = System.currentTimeMillis();
        }

        void notifyEmailed() {
            lastEmail = System.currentTimeMillis();
        }

        boolean isAlive() {
            return System.currentTimeMillis() - lastAlive < TimeUnit.MINUTES.toMillis(2);
        }

        boolean isTimeToLog() {
            return System.currentTimeMillis() - lastLog > TimeUnit.MINUTES.toMillis(10);
        }

        boolean isTimeToEmail() {
            return System.currentTimeMillis() - lastEmail > TimeUnit.MINUTES.toMillis(60);
        }

        boolean isStopRequested() {
            return stopRequested;
        }

        void requestStop() {
            stopRequested = true;
        }
    }

    private static synchronized void checkMonitorRunning() {
        if (monitoringThread != null) {
            return;
        }

        monitoringThread = new MonitoringThread();
        monitoringThread.setDaemon(true);
        monitoringThread.start();
    }

    private static class MonitoringThread extends Thread {

        private final String email;
        private final String emailTag;

        private MonitoringThread() {
            super("MonitoringThread");
            setDaemon(true);
            email = Settings.get("ThreadMonitor.email");
            emailTag = Str.mn(Settings.get("ThreadMonitor.email.tag"));
        }

        @Override
        public void run() {
            long lastStatus = 0;
            long nextEmailStatus = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1);

            //noinspection InfiniteLoopStatement
            while (true) {
                synchronized (threadInfoList) {
                    List<Thread> threadsToRemove = new ArrayList<>();

                    for (Map.Entry<Thread, ThreadInfo> entry : threadInfoList.entrySet()) {
                        Thread thread = entry.getKey();
                        ThreadInfo threadInfo = entry.getValue();

                        State state = thread.getState();
                        if (state != State.TERMINATED) {
                            if (!threadInfo.isAlive()) {
                                if (threadInfo.isTimeToLog()) {
                                    logger.warn(String.format("Thread '%s' seems like sleeping\r\n%s",
                                            thread.getName(),
                                            Misc.stackTraceToString(thread.getStackTrace())));
                                    threadInfo.notifyLogged();
                                }

                                if (threadInfo.isTimeToEmail()) {
                                    if (email != null) {
                                        sendEmail(String.format("SLEEPING: %s thread", thread.getName()), Misc.stackTraceToString(thread.getStackTrace()));
                                        threadInfo.notifyEmailed();
                                    }
                                }
                            }
                        } else {
                            logger.error(String.format("Thread '%s' is terminated - REMOVED", thread.getName()));
                            threadsToRemove.add(thread);


                            if (!threadInfo.isStopRequested()) {
                                if (email != null) {
                                    sendEmail(String.format("REMOVE: %s thread - terminated", thread.getName()), "");
                                }
                            }
                        }
                    }

                    for (Thread thread : threadsToRemove) {
                        threadInfoList.remove(thread);
                    }

                    if (System.currentTimeMillis() > lastStatus + TimeUnit.MINUTES.toMillis(15)) {
                        logger.info("Current threads:");
                        for (Thread thread : threadInfoList.keySet()) {
                            logger.info(String.format("\tThread '%s'", thread.getName()));
                        }

                        lastStatus = System.currentTimeMillis();
                    }
                }


                if (email != null
                        && nextEmailStatus < System.currentTimeMillis()) {
                    String body = "";
                    for (Thread thread : threadInfoList.keySet()) {
                        body += thread.getName() + "\r\n";
                    }

                    sendEmail(String.format("STATUS: %s thread(s)", threadInfoList.size()), body);

                    nextEmailStatus += TimeUnit.DAYS.toMillis(7);
                }


                Misc.sleepBM(1000);
            }
        }

        private void sendEmail(String subject, String body) {
            if (email == null) {
                return;
            }

            try {
                logger.warn("Sending email with subject '{}'", subject);
                Mailer.sendPlainText(email, emailTag + " " + subject, body);
                logger.warn("Email sent");
            } catch (Throwable t) {
                logger.warn("Error on email processing", t);
            }
        }
    }
}
