package io.coodoo.workhorse.persistence.legacy.boundary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import io.coodoo.workhorse.core.entity.ExecutionStatus;
import io.coodoo.workhorse.core.entity.WorkhorseConfig;

/**
 * @author coodoo GmbH (coodoo.io)
 */
public class LegacyPersistenceConfig extends WorkhorseConfig {

    public static final String NAME = "Legacy Persistence";

    /**
     * ZoneId Object time zone for LocalDateTime instance creation. Default is UTC
     */
    public static ZoneId TIME_ZONE = ZoneId.of("Europe/Berlin");

    /**
     * Job queue poller interval in seconds
     */
    public static int JOB_QUEUE_POLLER_INTERVAL = 5;

    /**
     * Max amount of executions to load into the memory queue per job
     */
    public static int JOB_QUEUE_MAX = 1000;

    /**
     * Min amount of executions in memory queue before the poller gets to add more
     */
    public static int JOB_QUEUE_MIN = 100;

    /**
     * A zombie is an execution that is stuck in status {@link ExecutionStatus#RUNNING} for this amount of minutes (if set to 0 the hunt is off)
     */
    public static int ZOMBIE_RECOGNITION_TIME = 120;

    /**
     * If an execution is stuck in status {@link ExecutionStatus#RUNNING} and doesn't change, it has became a zombie! Once found we have a cure!
     */
    public static ExecutionStatus ZOMBIE_CURE_STATUS = ExecutionStatus.ABORTED;

    /**
     * Days until by minute statistic records gets deleted (0 to keep all)
     */
    public static int DAYS_UNTIL_STATISTIC_MINUTES_DELETION = 10;

    /**
     * Days until hourly statistic records gets deleted (0 to keep all)
     */
    public static int DAYS_UNTIL_STATISTIC_HOURS_DELETION = 30;

    /**
     * Log change pattern. Placeholder <code>%s</code> for changeParameter, changeOld and changeNew in this order <br>
     * Default is <code>Changed %s from '%s' to '%s'</code>
     */
    public static String LOG_CHANGE = "%s changed from '%s' to '%s'";

    /**
     * Execution log timestamp pattern. Default is <code>[HH:mm:ss.SSS]</code>
     */
    public static String LOG_TIME_FORMAT = "'['HH:mm:ss.SSS']'";
    public static DateTimeFormatter LOG_TIME_FORMATTER = DateTimeFormatter.ofPattern(LOG_TIME_FORMAT);

    /**
     * Execution log info marker. Default is none
     */
    public static String LOG_INFO_MARKER = null;

    /**
     * Execution log warn marker. Default is <code>[WARN]</code>
     */
    public static String LOG_WARN_MARKER = "[WARN]";

    /**
     * Execution log error marker. Default is <code>[ERROR]</code>
     */
    public static String LOG_ERROR_MARKER = "[ERROR]";

    public static final Long MINUTES_UNTIL_CLEANUP = 30l * 24l * 60l;

    private static String version = null;
    {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("workhorse-persistence-legacy.txt");
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            version = reader.readLine();
            if (version == null) {
                version = "Unknown";
            } else {
                if (version.endsWith("SNAPSHOT")) {
                    String timestamp = reader.readLine();
                    if (timestamp != null) {
                        version += " (" + timestamp + ")";
                    }
                }
            }
        } catch (IOException e) {
            version = "Unknown (" + e.getMessage() + ")";
        }
    }

    public LegacyPersistenceConfig() {

        timeZone = "Europe/Berlin";
        bufferMax = JOB_QUEUE_MAX;
        bufferMin = JOB_QUEUE_MIN;
        bufferPollInterval = JOB_QUEUE_POLLER_INTERVAL;
        bufferPushFallbackPollInterval = JOB_QUEUE_POLLER_INTERVAL;
        minutesUntilCleanup = MINUTES_UNTIL_CLEANUP;
        executionTimeout = ZOMBIE_RECOGNITION_TIME * 60;
        executionTimeoutStatus = ZOMBIE_CURE_STATUS;
        logChange = LOG_CHANGE;
        logTimeFormat = LOG_TIME_FORMAT;
        logInfoMarker = LOG_INFO_MARKER;
        logWarnMarker = LOG_WARN_MARKER;
        logErrorMarker = LOG_ERROR_MARKER;
    }

    @Override
    public String getPersistenceName() {
        return NAME;
    }

    @Override
    public String getPersistenceVersion() {
        return version;
    }

}
