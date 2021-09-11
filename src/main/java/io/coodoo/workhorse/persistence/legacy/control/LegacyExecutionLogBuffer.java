package io.coodoo.workhorse.persistence.legacy.control;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import io.coodoo.workhorse.core.entity.ExecutionStatus;
import io.coodoo.workhorse.persistence.legacy.boundary.JobEngineEntityManager;
import io.coodoo.workhorse.persistence.legacy.entity.LegacyExecution;

/**
 * To avoid heavy duty on the database by performing an UPDATE statement for every line of log the execution logs gets buffered here.
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class LegacyExecutionLogBuffer {

    /**
     * Interval to write the logs in seconds
     */
    private static final int SECONDS_INTERVAL = 3;

    @Inject
    @JobEngineEntityManager
    EntityManager entityManager;

    private Map<Long, StringBuffer> logs = new ConcurrentHashMap<>();

    /**
     * Appends a line to the execution log
     */
    public void appendLog(Long executionId, String log) {

        StringBuffer stringBuffer = logs.get(executionId);
        if (stringBuffer == null) {
            stringBuffer = new StringBuffer(log);
            logs.put(executionId, stringBuffer);
        }
        stringBuffer.append(System.lineSeparator());
        stringBuffer.append(log);
    }

    /**
     * This method is called when an execution is put in status {@link ExecutionStatus#FINISHED}, {@link ExecutionStatus#FAILED} or
     * {@link ExecutionStatus#ABORTED} to finally write the log
     * 
     * @see LegacyController#updateJobExecutionStatus(Long, ExecutionStatus)
     */
    public void finalizeLog(Long executionId) {
        writeBufferdLogs(executionId);
    }

    /**
     * A schedule writes all bufferd logs into the database every {@link #SECONDS_INTERVAL} seconds
     */
    @Schedule(second = "*/" + SECONDS_INTERVAL, minute = "*", hour = "*")
    public void writeBufferdLogsBySchedule() {
        for (Long executionId : logs.keySet()) {
            writeBufferdLogs(executionId);
        }
    }

    /**
     * The attribute {@link LegacyExecution#getLog()} is used to store the log. If it is <code>null</code>, a log is created, otherwise the existing log gets
     * extended
     */
    private void writeBufferdLogs(Long executionId) {

        StringBuffer stringBuffer = logs.remove(executionId);
        if (stringBuffer == null) {
            // nothing to log here
            return;
        }
        entityManager.createNativeQuery("UPDATE jobengine_execution SET log = CONCAT(IFNULL(log, ''), :log) WHERE id = :executionId") //
                        .setParameter("log", stringBuffer.toString()) //
                        .setParameter("executionId", executionId) //
                        .executeUpdate();
    }

}
