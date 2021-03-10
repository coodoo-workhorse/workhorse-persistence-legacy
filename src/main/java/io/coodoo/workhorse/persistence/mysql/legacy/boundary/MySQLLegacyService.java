package io.coodoo.workhorse.persistence.mysql.legacy.boundary;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.coodoo.framework.listing.boundary.Listing;
import io.coodoo.framework.listing.boundary.ListingParameters;
import io.coodoo.framework.listing.boundary.ListingResult;
import io.coodoo.workhorse.core.entity.ExecutionStatus;
import io.coodoo.workhorse.core.entity.JobStatus;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.annotation.JobEngineEntityManager;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.Config;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.DbJob;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.GroupInfo;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.JobExecution;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.JobExecutionInfo;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.Log;
import io.coodoo.workhorse.util.CronExpression;
import io.coodoo.workhorse.util.WorkhorseUtil;

/**
 * Provides basically CRUD and management functionality
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@Stateless
public class MySQLLegacyService {

    private final Logger logger = LoggerFactory.getLogger(MySQLLegacyService.class);

    @Inject
    @JobEngineEntityManager
    EntityManager entityManager;

    public Config getConfig() {

        Config config = Config.getConfig(entityManager);

        if (config == null) {

            config = new Config();
            entityManager.persist(config);

            logger.info("Created: {}", config);
            logMessage("Initial config set: " + config, null, false);
        }
        return config;
    }

    public Config updateConfig(String timeZone, int jobQueuePollerInterval, int jobQueueMax, int jobQueueMin, int zombieRecognitionTime,
                    ExecutionStatus zombieCureStatus, int daysUntilStatisticMinutesDeletion, int daysUntilStatisticHoursDeletion, String logChange,
                    String logTimeFormatter, String logInfoMarker, String logWarnMarker, String logErrorMarker) {

        Config config = getConfig();
        config.setTimeZone(timeZone);
        config.setJobQueuePollerInterval(jobQueuePollerInterval);
        config.setJobQueueMax(jobQueueMax);
        config.setJobQueueMin(jobQueueMin);
        config.setZombieRecognitionTime(zombieRecognitionTime);
        config.setZombieCureStatus(zombieCureStatus);
        config.setDaysUntilStatisticMinutesDeletion(daysUntilStatisticMinutesDeletion);
        config.setDaysUntilStatisticHoursDeletion(daysUntilStatisticHoursDeletion);
        config.setLogChange(logChange);
        config.setLogTimeFormatter(logTimeFormatter);
        config.setLogInfoMarker(logInfoMarker);
        config.setLogWarnMarker(logWarnMarker);
        config.setLogErrorMarker(logErrorMarker);
        return config;
    }

    public ListingResult<Log> listLogs(ListingParameters listingParameters) {
        return Listing.getListingResult(entityManager, Log.class, listingParameters);
    }

    public Log getLog(Long id) {
        return entityManager.find(Log.class, id);
    }

    /**
     * Logs a text message in an own {@link Transaction}
     * 
     * @param message text to log
     * @param jobId optional: belonging {@link DbJob}-ID
     * @param byUser <code>true</code> if author is a user, <code>false</code> if author is the system
     * @return the resulting log entry
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Log logMessageInNewTransaction(String message, Long jobId, boolean byUser) {
        return logMessage(message, jobId, byUser);
    }

    /**
     * Logs a text message
     * 
     * @param message text to log
     * @param jobId optional: belonging {@link DbJob}-ID
     * @param byUser <code>true</code> if author is a user, <code>false</code> if author is the system
     * @return the resulting log entry
     */
    public Log logMessage(String message, Long jobId, boolean byUser) {

        JobStatus jobStatus = null;
        if (jobId != null) {
            DbJob job = getJobById(jobId);
            if (job != null) {
                jobStatus = job.getStatus();
            }
        }
        return createLog(message, jobId, jobStatus, byUser, null, null, null, null);
    }

    public Log createLog(String message, Long jobId, JobStatus jobStatus, boolean byUser, String changeParameter, String changeOld, String changeNew,
                    String stacktrace) {

        Log log = new Log();
        log.setMessage(message);
        log.setJobId(jobId);
        log.setJobStatus(jobStatus);
        log.setByUser(byUser);
        log.setChangeParameter(changeParameter);
        log.setChangeOld(changeOld);
        log.setChangeNew(changeNew);
        log.setHostName(WorkhorseUtil.getHostName());
        log.setStacktrace(stacktrace);

        entityManager.persist(log);
        logger.info("Created: {}", log);
        return log;
    }

    public int deleteAllLogsByJobId(Long jobId) {
        return Log.deleteAllByJobId(entityManager, jobId);
    }

    public Log deleteLogsById(Long logId) {

        Log log = getLog(logId);
        if (log != null) {
            entityManager.remove(log);
            logger.info("Deleted: {}", log);
        }
        return log;
    }

    public void activateJob(Long jobId) {

        DbJob job = getJobById(jobId);
        logger.info("Activate job {}", job.getName());
        updateJobStatus(job.getId(), JobStatus.ACTIVE);
    }

    public void deactivateJob(Long jobId) {

        DbJob job = getJobById(jobId);
        logger.info("Deactivate job {}", job.getName());
        updateJobStatus(job.getId(), JobStatus.INACTIVE);
    }

    private void updateJobStatus(Long jobId, JobStatus status) {

        DbJob job = getJobById(jobId);
        job.setStatus(status);
        logger.info("Job status updated to: {}", status);
    }

    public List<DbJob> getAllJobs() {
        return DbJob.getAll(entityManager);
    }

    public List<DbJob> getAllByStatus(JobStatus status) {
        return DbJob.getAllByStatus(entityManager, status);
    }

    public DbJob getJobById(Long jobId) {
        return entityManager.find(DbJob.class, jobId);
    }

    public Long countAllJobs() {
        return DbJob.countAll(entityManager);
    }

    public Long countJobsByStatus(JobStatus jobStatus) {
        return DbJob.countAllByStatus(entityManager, jobStatus);
    }

    public DbJob getJobByClassName(String className) {
        return DbJob.getByWorkerClassName(entityManager, className);
    }

    public List<DbJob> getAllScheduledJobs() {
        return DbJob.getAllScheduled(entityManager);
    }

    public DbJob createJob(String name, String description, List<String> tags, String workerClassName, String parametersClassName, String schedule,
                    JobStatus status, int threads, Integer maxPerMinute, int failRetries, int retryDelay, int daysUntilCleanUp, boolean uniqueInQueue) {

        DbJob dbJob = new DbJob();
        dbJob.setName(name);
        dbJob.setDescription(description);
        dbJob.setTags(tags);
        dbJob.setWorkerClassName(workerClassName);
        dbJob.setParametersClassName(parametersClassName);
        dbJob.setSchedule(schedule);
        dbJob.setStatus(status);
        dbJob.setThreads(threads);
        dbJob.setMaxPerMinute(maxPerMinute);
        dbJob.setFailRetries(failRetries);
        dbJob.setRetryDelay(retryDelay);
        dbJob.setDaysUntilCleanUp(daysUntilCleanUp);
        dbJob.setUniqueInQueue(uniqueInQueue);

        entityManager.persist(dbJob);

        return dbJob;
    }

    public DbJob updateJob(Long jobId, String name, String description, List<String> tags, String workerClassName, String schedule, JobStatus status,
                    int threads, Integer maxPerMinute, int failRetries, int retryDelay, int daysUntilCleanUp, boolean uniqueInQueue) {

        DbJob dbJob = getJobById(jobId);
        dbJob.setName(name);
        dbJob.setDescription(description);
        dbJob.setTags(tags);
        dbJob.setWorkerClassName(workerClassName);
        dbJob.setSchedule(schedule);
        dbJob.setStatus(status);
        dbJob.setThreads(threads);
        dbJob.setMaxPerMinute(maxPerMinute);
        dbJob.setFailRetries(failRetries);
        dbJob.setRetryDelay(retryDelay);
        dbJob.setDaysUntilCleanUp(daysUntilCleanUp);
        dbJob.setUniqueInQueue(uniqueInQueue);

        logger.info("Job updated: {}", dbJob);
        return dbJob;
    }

    public void deleteJob(Long jobId) {

        DbJob job = getJobById(jobId);

        int deletedJobExecutions = JobExecution.deleteAllByJobId(entityManager, jobId);
        int deletedJobLogs = deleteAllLogsByJobId(jobId);

        entityManager.remove(job);

        String logMessage = String.format("Job removed (including %d executions and %d logs): %s", deletedJobExecutions, deletedJobLogs, job.toString());
        logger.info(logMessage);
        logMessage(logMessage, null, true);
    }

    public JobExecution getJobExecutionById(Long jobExecutionId) {
        return entityManager.find(JobExecution.class, jobExecutionId);
    }

    public GroupInfo getJobExecutionBatchInfo(Long batchId) {

        List<JobExecutionInfo> batchInfo = JobExecution.getBatchInfo(entityManager, batchId);
        return new GroupInfo(batchId, batchInfo);
    }

    /**
     * Check whether all executions of a batch job are finished.
     * 
     * @param batchId the ID of the batch executions
     * @return <code>true</code> if no execution of this batch job is either queued or running.
     */
    public boolean isBatchFinished(Long batchId) {
        Long queuedExecutions = countBatchExecutions(batchId, ExecutionStatus.QUEUED);
        if (queuedExecutions.equals(0l)) {
            Long runningExecutions = countBatchExecutions(batchId, ExecutionStatus.RUNNING);
            if (runningExecutions.equals(0l)) {
                return true;
            }
        }
        return false;
    }

    public Long countBatchExecutions(Long batchId, ExecutionStatus status) {
        return JobExecution.countBatchByStatus(entityManager, batchId, status);
    }

    public List<JobExecution> getJobExecutionBatch(Long batchId) {
        return JobExecution.getBatch(entityManager, batchId);
    }

    /**
     * Abort all executions of a batch that are in status {@link ExecutionStatus#QUEUED}
     * 
     * @param batchId the ID of the batch executions
     * @return the amount of executions of that batch that where put in status {@link ExecutionStatus#ABORTED}
     */
    public int abortBatch(Long batchId) {
        return JobExecution.abortBatch(entityManager, batchId);
    }

    public GroupInfo getJobExecutionChainInfo(Long chainId) {

        List<JobExecutionInfo> batchInfo = JobExecution.getChainInfo(entityManager, chainId);
        return new GroupInfo(chainId, batchInfo);
    }

    public List<JobExecution> getJobExecutionChain(Long chainId) {
        return JobExecution.getChain(entityManager, chainId);
    }

    /**
     * Abort all executions of a chain that are in status {@link ExecutionStatus#QUEUED}
     * 
     * @param chainId the ID of the chain executions
     * @return the amount of executions of that chain that where put in status {@link ExecutionStatus#ABORTED}
     */
    public int abortChain(Long chainId) {
        return JobExecution.abortChain(entityManager, chainId);
    }

    public List<JobExecution> getAllByStatus(ExecutionStatus jobExecutionStatus) {
        return JobExecution.getAllByStatus(entityManager, jobExecutionStatus);
    }

    public List<JobExecution> getAllByJobIdAndStatus(Long jobId, ExecutionStatus jobExecutionStatus) {
        return JobExecution.getAllByJobIdAndStatus(entityManager, jobId, jobExecutionStatus);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public JobExecution createJobExecution(Long jobId, String parameters, Boolean priority, LocalDateTime maturity, Long batchId, Long chainId,
                    Long previousJobExecutionId, boolean uniqueInQueue) {

        Integer parametersHash = null;
        if (parameters != null) {
            parametersHash = parameters.hashCode();
            if (parameters.trim().isEmpty() || parameters.isEmpty()) {
                parameters = null;
                parametersHash = null;
            }
        }

        if (uniqueInQueue) {
            // Prüfen ob es bereits eine Job Excecution mit diesn Parametern existiert und im Status QUEUED ist. Wenn ja diese zurückgeben.
            JobExecution equalQueuedJobExcecution = JobExecution.getFirstCreatedByJobIdAndParametersHash(entityManager, jobId, parametersHash);
            if (equalQueuedJobExcecution != null) {
                return equalQueuedJobExcecution;
            }
        }

        JobExecution jobExecution = new JobExecution();
        jobExecution.setJobId(jobId);
        jobExecution.setStatus(ExecutionStatus.QUEUED);
        jobExecution.setParameters(parameters);
        jobExecution.setParametersHash(parametersHash);
        jobExecution.setFailRetry(0);
        jobExecution.setPriority(priority != null ? priority : false);
        jobExecution.setMaturity(maturity);
        jobExecution.setBatchId(batchId);
        jobExecution.setChainId(chainId);
        jobExecution.setChainPreviousExecutionId(previousJobExecutionId);

        entityManager.persist(jobExecution);
        logger.debug("JobExecution created: {}", jobExecution);
        return jobExecution;
    }

    public JobExecution updateJobExecution(Long jobExecutionId, ExecutionStatus status, String parameters, boolean priority, LocalDateTime maturity,
                    int fails) {

        JobExecution jobExecution = getJobExecutionById(jobExecutionId);
        jobExecution.setStatus(status);
        jobExecution.setParameters(parameters);
        jobExecution.setPriority(priority);
        jobExecution.setMaturity(maturity);
        jobExecution.setFailRetry(fails);
        logger.info("JobExecution updated: {}", jobExecution);
        return jobExecution;
    }

    public void deleteJobExecution(Long jobExecutionId) {

        JobExecution jobExecution = getJobExecutionById(jobExecutionId);
        entityManager.remove(jobExecution);
        logger.info("JobExecution removed: {}", jobExecution);
    }

    /**
     * You can redo an {@link JobExecution} in status {@link ExecutionStatus#FINISHED}, {@link ExecutionStatus#FAILED} and {@link ExecutionStatus#ABORTED}, but
     * all meta data like timestamps and logs of this execution will be gone!
     * 
     * @param jobExecutionId ID of the {@link JobExecution} you wish to redo
     * @return cleared out {@link JobExecution} in status {@link ExecutionStatus#QUEUED}
     */
    public JobExecution redoJobExecution(Long jobExecutionId) {

        JobExecution jobExecution = getJobExecutionById(jobExecutionId);

        if (ExecutionStatus.QUEUED == jobExecution.getStatus() || ExecutionStatus.RUNNING == jobExecution.getStatus()) {
            logger.warn("Can't redo JobExecution in status {}: {}", jobExecution.getStatus(), jobExecution);
            return jobExecution;
        }

        logger.info("Redo {} {}", jobExecution.getStatus(), jobExecution);

        jobExecution.setMaturity(WorkhorseUtil.timestamp());
        jobExecution.setStatus(ExecutionStatus.QUEUED);
        jobExecution.setStartedAt(null);
        jobExecution.setEndedAt(null);
        jobExecution.setDuration(null);
        jobExecution.setLog(null);
        jobExecution.setFailMessage(null);
        jobExecution.setFailRetry(0);
        jobExecution.setFailRetryExecutionId(null);
        jobExecution.setFailStacktrace(null);
        return jobExecution;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public long currentJobExecutions(Long jobId, ExecutionStatus jobExecutionStatus) {
        return JobExecution.countByJobIdAndStatus(entityManager, jobId, jobExecutionStatus);
    }

    /**
     * Get the next execution times of a scheduled job
     * 
     * @param jobId ID of the scheduled job
     * @param times amount of future execution times
     * @param startTime start time for this request (if <tt>null</tt> then current time is used)
     * @return List of {@link LocalDateTime} representing the next execution times of a scheduled job
     */
    public List<LocalDateTime> getNextScheduledTimes(Long jobId, int times, LocalDateTime startTime) {

        DbJob job = getJobById(jobId);
        return getNextScheduledTimes(job.getSchedule(), times, startTime);
    }

    /**
     * Get the next execution times defined by {@link DbJob#getSchedule()}
     * 
     * @param schedule CRON Expression
     * @param times amount of future execution times
     * @param startTime start time for this request (if <tt>null</tt> then current time is used)
     * @return List of {@link LocalDateTime} representing the next execution times of a scheduled job
     */
    public List<LocalDateTime> getNextScheduledTimes(String schedule, int times, LocalDateTime startTime) {

        List<LocalDateTime> nextScheduledTimes = new ArrayList<>();
        if (schedule == null) {
            return nextScheduledTimes;
        }

        CronExpression cronExpression = new CronExpression(schedule);
        LocalDateTime nextScheduledTime = startTime != null ? startTime : WorkhorseUtil.timestamp();

        for (int i = 0; i < times; i++) {
            nextScheduledTime = cronExpression.nextTimeAfter(nextScheduledTime);
            nextScheduledTimes.add(nextScheduledTime);
        }
        return nextScheduledTimes;
    }

    /**
     * Get the execution times of a scheduled job
     * 
     * @param jobId ID of the scheduled job
     * @param startTime start time for this request (if <tt>null</tt> then current time is used)
     * @param endTime end time for this request (if <tt>null</tt> then current time plus 1 day is used)
     * @return List of {@link LocalDateTime} representing the execution times of a scheduled job between the <tt>startTime</tt> and <tt>endTime</tt>
     */
    public List<LocalDateTime> getScheduledTimes(Long jobId, LocalDateTime startTime, LocalDateTime endTime) {

        DbJob job = getJobById(jobId);
        return getScheduledTimes(job.getSchedule(), startTime, endTime);
    }

    /**
     * Get the execution times defined by {@link DbJob#getSchedule()}
     * 
     * @param schedule CRON Expression
     * @param startTime start time for this request (if <tt>null</tt> then current time is used)
     * @param endTime end time for this request (if <tt>null</tt> then current time plus 1 day is used)
     * @return List of {@link LocalDateTime} representing the execution times of a scheduled job between the <tt>startTime</tt> and <tt>endTime</tt>
     */
    public List<LocalDateTime> getScheduledTimes(String schedule, LocalDateTime startTime, LocalDateTime endTime) {

        List<LocalDateTime> scheduledTimes = new ArrayList<>();
        if (schedule == null) {
            return scheduledTimes;
        }

        CronExpression cronExpression = new CronExpression(schedule);
        LocalDateTime scheduledTime = startTime != null ? startTime : WorkhorseUtil.timestamp();
        LocalDateTime endOfTimes = endTime != null ? endTime : scheduledTime.plusDays(1);

        while (scheduledTime.isBefore(endOfTimes)) {
            scheduledTime = cronExpression.nextTimeAfter(scheduledTime);
            scheduledTimes.add(scheduledTime);
        }
        return scheduledTimes;
    }

}
