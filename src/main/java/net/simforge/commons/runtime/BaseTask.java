package net.simforge.commons.runtime;

import net.simforge.commons.legacy.BM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTask implements Runnable {
    protected final Logger logger;
    private String taskName;

    private boolean singleRun = false;
    private long baseSleepTime = 60000; // 60 secs
    private long nextSleepTime = NEXT_SLEEP_TIME_IS_UNDEFINED;

    private static final int NEXT_SLEEP_TIME_IS_UNDEFINED = -1;

    protected BaseTask(String taskName) {
        this.taskName = taskName;
        this.logger = LoggerFactory.getLogger(taskName);
    }

    @Override
    public void run() {
        logger.info("Starting");

        ThreadMonitor.checkin();
        BM.init(taskName);

        startup();

        logger.info("Started");

        while (true) {
            if (ThreadMonitor.isStopRequested()) {
                logger.info("Stop requested");
                break;
            }

            logger.trace("Cycle");

            ThreadMonitor.alive();

            try {
                process();
            } catch (Exception e) {
                logger.error("Error during processing", e);
            }

            if (singleRun) {
                break;
            }

            if (ThreadMonitor.isStopRequested()) {
                logger.info("Stop requested");
                break;
            }

            long sleepTime = nextSleepTime == NEXT_SLEEP_TIME_IS_UNDEFINED
                    ? baseSleepTime
                    : nextSleepTime;
            nextSleepTime = NEXT_SLEEP_TIME_IS_UNDEFINED;

            ThreadMonitor.alive();

            if (ThreadMonitor.isStopRequested()) {
                logger.info("Stop requested");
                break;
            }

            BM.logPeriodically(true);

            logger.trace("Sleep for " + sleepTime + " ms");
            ThreadMonitor.sleepBM(sleepTime);
        }

        BM.logNow();

        logger.info("Shutting down");

        shutdown();

        logger.info("Shutdown");
    }

    protected void startup() {
        // nothing to do
    }

    protected abstract void process();

    protected void shutdown() {
        // nothing to do
    }

    public String getTaskName() {
        return taskName;
    }

    public boolean isSingleRun() {
        return singleRun;
    }

    public void setSingleRun(boolean singleRun) {
        this.singleRun = singleRun;
    }

    public long getBaseSleepTime() {
        return baseSleepTime;
    }

    public void setBaseSleepTime(long baseSleepTime) {
        this.baseSleepTime = baseSleepTime;
    }

    public long getNextSleepTime() {
        return nextSleepTime;
    }

    public void setNextSleepTime(long nextSleepTime) {
        this.nextSleepTime = nextSleepTime;
    }
}
