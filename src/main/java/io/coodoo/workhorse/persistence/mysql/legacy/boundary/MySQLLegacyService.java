package io.coodoo.workhorse.persistence.mysql.legacy.boundary;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.ejb.Asynchronous;
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
import io.coodoo.workhorse.core.control.StaticConfig;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.annotation.JobEngineEntityManager;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.Config;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.GroupInfo;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.Job;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.JobExecution;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.JobExecutionInfo;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.JobExecutionStatus;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.JobStatus;
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

    public ListingResult<Log> listLogs(ListingParameters listingParameters) {
        return Listing.getListingResult(entityManager, Log.class, listingParameters);
    }

    public Log getLog(Long id) {
        return entityManager.find(Log.class, id);
    }

    public Log logChange(Long jobId, JobStatus jobStatus, String changeParameter, Object changeOld, Object changeNew, String message) {

        String co = changeOld == null ? "" : changeOld.toString();
        String cn = changeNew == null ? "" : changeNew.toString();

        if (message == null) {
            message = String.format(StaticConfig.LOG_CHANGE, changeParameter, co, cn);
        }
        return createLog(message, jobId, jobStatus, true, changeParameter, co, cn, null);
    }

    @Asynchronous
    public void logException(Exception exception, String message, Long jobId, JobStatus jobStatus) {
        createLog(message != null ? message : WorkhorseUtil.getMessagesFromException(exception), jobId, jobStatus, false, null, null, null,
                        WorkhorseUtil.stacktraceToString(exception));
    }

    /**
     * Logs a text message in an own {@link Transaction}
     * 
     * @param message text to log
     * @param jobId optional: belonging {@link Job}-ID
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
     * @param jobId optional: belonging {@link Job}-ID
     * @param byUser <code>true</code> if author is a user, <code>false</code> if author is the system
     * @return the resulting log entry
     */
    public Log logMessage(String message, Long jobId, boolean byUser) {

        JobStatus jobStatus = null;
        if (jobId != null) {
            Job job = getJobById(jobId);
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

    public int deleteAllByJobId(Long jobId) {
        return Log.deleteAllByJobId(entityManager, jobId);
    }

    public void activateJob(Long jobId) {

        Job job = getJobById(jobId);
        logger.info("Activate job {}", job.getName());
        updateJobStatus(job.getId(), JobStatus.ACTIVE);
    }

    public void deactivateJob(Long jobId) {

        Job job = getJobById(jobId);
        logger.info("Deactivate job {}", job.getName());
        updateJobStatus(job.getId(), JobStatus.INACTIVE);
    }

    private void updateJobStatus(Long jobId, JobStatus status) {

        Job job = getJobById(jobId);
        logChange(jobId, status, "Status", job.getStatus(), status, null);
        job.setStatus(status);
        logger.info("Job status updated to: {}", status);
    }

    public List<Job> getAllJobs() {
        return Job.getAll(entityManager);
    }

    public Job getJobById(Long jobId) {
        return entityManager.find(Job.class, jobId);
    }

    public Long countJobsByStatus(JobStatus jobStatus) {
        return Job.countAllByStatus(entityManager, jobStatus);
    }

    public Job getJobByClassName(String className) {
        return Job.getByWorkerClassName(entityManager, className);
    }

    public List<Job> getAllScheduledJobs() {
        return Job.getAllScheduled(entityManager);
    }

    public Job updateJob(Long jobId, String name, String description, List<String> tags, String workerClassName, String schedule, JobStatus status, int threads,
                    Integer maxPerMinute, int failRetries, int retryDelay, int daysUntilCleanUp, boolean uniqueInQueue) {

        Job job = getJobById(jobId);

        if (!Objects.equals(job.getName(), name)) {
            logChange(jobId, status, "Name", job.getName(), name, null);
            job.setName(name);
        }
        if (!Objects.equals(job.getDescription(), description)) {
            logChange(jobId, status, "Description", job.getDescription(), description, null);
            job.setDescription(description);
        }
        if (!Objects.equals(job.getTags(), tags)) {
            logChange(jobId, status, "Tags", job.getTags(), tags, null);
            job.setTags(tags);
        }
        if (!Objects.equals(job.getWorkerClassName(), workerClassName)) {
            logChange(jobId, status, "JobWorker class name", job.getWorkerClassName(), workerClassName, null);
            job.setWorkerClassName(workerClassName);
        }
        if (!Objects.equals(job.getSchedule(), schedule)) {
            logChange(jobId, status, "Schedule", job.getSchedule(), schedule, null);
            job.setSchedule(schedule);
        }
        if (!Objects.equals(job.getStatus(), status)) {
            logChange(jobId, status, "Status", job.getStatus(), status.name(), null);
            job.setStatus(status);
        }
        if (!Objects.equals(job.getThreads(), threads)) {
            logChange(jobId, status, "Threads", job.getThreads(), threads, null);
            job.setThreads(threads);
        }
        if (!Objects.equals(job.getMaxPerMinute(), maxPerMinute)) {
            logChange(jobId, status, "Max executions per minute", job.getMaxPerMinute(), maxPerMinute, null);
            job.setMaxPerMinute(maxPerMinute);
        }
        if (!Objects.equals(job.getFailRetries(), failRetries)) {
            logChange(jobId, status, "Fail retries", job.getFailRetries(), failRetries, null);
            job.setFailRetries(failRetries);
        }
        if (!Objects.equals(job.getRetryDelay(), retryDelay)) {
            logChange(jobId, status, "Retry delay", job.getRetryDelay(), retryDelay, null);
            job.setRetryDelay(retryDelay);
        }
        if (!Objects.equals(job.getDaysUntilCleanUp(), daysUntilCleanUp)) {
            logChange(jobId, status, "Days until cleanup", job.getDaysUntilCleanUp(), daysUntilCleanUp, null);
            job.setDaysUntilCleanUp(daysUntilCleanUp);
        }
        if (!Objects.equals(job.isUniqueInQueue(), uniqueInQueue)) {
            logChange(jobId, status, "Unique in queue", job.isUniqueInQueue(), uniqueInQueue, null);
            job.setUniqueInQueue(uniqueInQueue);
        }

        logger.info("Job updated: {}", job);
        return job;
    }

    public void deleteJob(Long jobId) {

        Job job = getJobById(jobId);

        int deletedJobExecutions = JobExecution.deleteAllByJobId(entityManager, jobId);
        int deletedJobLogs = deleteAllByJobId(jobId);

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
        Long queuedExecutions = countBatchExecutions(batchId, JobExecutionStatus.QUEUED);
        if (queuedExecutions.equals(0l)) {
            Long runningExecutions = countBatchExecutions(batchId, JobExecutionStatus.RUNNING);
            if (runningExecutions.equals(0l)) {
                return true;
            }
        }
        return false;
    }

    public Long countBatchExecutions(Long batchId, JobExecutionStatus status) {
        return JobExecution.countBatchByStatus(entityManager, batchId, status);
    }

    public List<JobExecution> getJobExecutionBatch(Long batchId) {
        return JobExecution.getBatch(entityManager, batchId);
    }

    /**
     * Abort all executions of a batch that are in status {@link JobExecutionStatus#QUEUED}
     * 
     * @param batchId the ID of the batch executions
     * @return the amount of executions of that batch that where put in status {@link JobExecutionStatus#ABORTED}
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
     * Abort all executions of a chain that are in status {@link JobExecutionStatus#QUEUED}
     * 
     * @param chainId the ID of the chain executions
     * @return the amount of executions of that chain that where put in status {@link JobExecutionStatus#ABORTED}
     */
    public int abortChain(Long chainId) {
        return JobExecution.abortChain(entityManager, chainId);
    }

    public List<JobExecution> getAllByStatus(JobExecutionStatus jobExecutionStatus) {
        return JobExecution.getAllByStatus(entityManager, jobExecutionStatus);
    }

    public List<JobExecution> getAllByJobIdAndStatus(Long jobId, JobExecutionStatus jobExecutionStatus) {
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
        jobExecution.setStatus(JobExecutionStatus.QUEUED);
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

    public JobExecution updateJobExecution(Long jobExecutionId, JobExecutionStatus status, String parameters, boolean priority, LocalDateTime maturity,
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
     * You can redo an {@link JobExecution} in status {@link JobExecutionStatus#FINISHED}, {@link JobExecutionStatus#FAILED} and
     * {@link JobExecutionStatus#ABORTED}, but all meta data like timestamps and logs of this execution will be gone!
     * 
     * @param jobExecutionId ID of the {@link JobExecution} you wish to redo
     * @return cleared out {@link JobExecution} in status {@link JobExecutionStatus#QUEUED}
     */
    public JobExecution redoJobExecution(Long jobExecutionId) {

        JobExecution jobExecution = getJobExecutionById(jobExecutionId);

        if (JobExecutionStatus.QUEUED == jobExecution.getStatus() || JobExecutionStatus.RUNNING == jobExecution.getStatus()) {
            logger.warn("Can't redo JobExecution in status {}: {}", jobExecution.getStatus(), jobExecution);
            return jobExecution;
        }

        logger.info("Redo {} {}", jobExecution.getStatus(), jobExecution);

        jobExecution.setMaturity(WorkhorseUtil.timestamp());
        jobExecution.setStatus(JobExecutionStatus.QUEUED);
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
    public long currentJobExecutions(Long jobId, JobExecutionStatus jobExecutionStatus) {
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

        Job job = getJobById(jobId);
        return getNextScheduledTimes(job.getSchedule(), times, startTime);
    }

    /**
     * Get the next execution times defined by {@link Job#getSchedule()}
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

        Job job = getJobById(jobId);
        return getScheduledTimes(job.getSchedule(), startTime, endTime);
    }

    /**
     * Get the execution times defined by {@link Job#getSchedule()}
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
