package io.coodoo.workhorse.persistence.legacy.control;

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
import io.coodoo.workhorse.core.control.StaticConfig;
import io.coodoo.workhorse.core.entity.ExecutionStatus;
import io.coodoo.workhorse.core.entity.Job;
import io.coodoo.workhorse.core.entity.JobExecutionCount;
import io.coodoo.workhorse.core.entity.JobExecutionStatusSummary;
import io.coodoo.workhorse.core.entity.JobStatus;
import io.coodoo.workhorse.persistence.legacy.boundary.JobEngineEntityManager;
import io.coodoo.workhorse.persistence.legacy.entity.JobExecutionCounts;
import io.coodoo.workhorse.persistence.legacy.entity.LegacyConfig;
import io.coodoo.workhorse.persistence.legacy.entity.LegacyExecution;
import io.coodoo.workhorse.persistence.legacy.entity.LegacyExecutionView;
import io.coodoo.workhorse.persistence.legacy.entity.LegacyJob;
import io.coodoo.workhorse.persistence.legacy.entity.LegacyLog;
import io.coodoo.workhorse.util.WorkhorseUtil;

/**
 * Provides basically CRUD and management functionality
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@Stateless
public class LegacyController {

    private final Logger logger = LoggerFactory.getLogger(LegacyController.class);

    @Inject
    @JobEngineEntityManager
    EntityManager entityManager;

    public LegacyConfig getConfig() {

        LegacyConfig config = LegacyConfig.getConfig(entityManager);
        if (config == null) {
            config = new LegacyConfig();
            entityManager.persist(config);

            logger.trace("Created: {}", config);
            logMessage("Initial config set: " + config, null, false);
        }
        return config;
    }

    public LegacyConfig updateConfig(String timeZone, int jobQueuePollerInterval, int jobQueueMax, int jobQueueMin, int zombieRecognitionTime,
                    ExecutionStatus zombieCureStatus, int daysUntilStatisticMinutesDeletion, int daysUntilStatisticHoursDeletion, String logChange,
                    String logTimeFormatter, String logInfoMarker, String logWarnMarker, String logErrorMarker) {

        LegacyConfig config = getConfig();
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

    public ListingResult<LegacyExecution> listExecutions(ListingParameters listingParameters) {
        return Listing.getListingResult(entityManager, LegacyExecution.class, listingParameters);
    }

    public ListingResult<LegacyExecutionView> listExecutionViews(ListingParameters listingParameters) {
        return Listing.getListingResult(entityManager, LegacyExecutionView.class, listingParameters);
    }

    public ListingResult<LegacyJob> listJobs(ListingParameters listingParameters) {
        return Listing.getListingResult(entityManager, LegacyJob.class, listingParameters);
    }

    public ListingResult<LegacyLog> listLogs(ListingParameters listingParameters) {
        return Listing.getListingResult(entityManager, LegacyLog.class, listingParameters);
    }

    public LegacyLog getLog(Long id) {
        return entityManager.find(LegacyLog.class, id);
    }

    /**
     * Logs a text message in an own {@link Transaction}
     * 
     * @param message text to log
     * @param jobId optional: belonging {@link LegacyJob}-ID
     * @param byUser <code>true</code> if author is a user, <code>false</code> if author is the system
     * @return the resulting log entry
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public LegacyLog logMessageInNewTransaction(String message, Long jobId, boolean byUser) {
        return logMessage(message, jobId, byUser);
    }

    /**
     * Logs a text message
     * 
     * @param message text to log
     * @param jobId optional: belonging {@link LegacyJob}-ID
     * @param byUser <code>true</code> if author is a user, <code>false</code> if author is the system
     * @return the resulting log entry
     */
    public LegacyLog logMessage(String message, Long jobId, boolean byUser) {

        JobStatus jobStatus = null;
        if (jobId != null) {
            LegacyJob job = getJobById(jobId);
            if (job != null) {
                jobStatus = job.getStatus();
            }
        }
        return createLog(message, jobId, jobStatus, byUser, null, null, null, null);
    }

    public LegacyLog createLog(String message, Long jobId, JobStatus jobStatus, boolean byUser, String changeParameter, String changeOld, String changeNew,
                    String stacktrace) {

        LegacyLog log = new LegacyLog();
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
        logger.trace("Created: {}", log);
        return log;
    }

    public int deleteAllLogsByJobId(Long jobId) {
        return LegacyLog.deleteAllByJobId(entityManager, jobId);
    }

    public LegacyLog deleteLogsById(Long logId) {

        LegacyLog log = getLog(logId);
        if (log != null) {
            entityManager.remove(log);
            logger.trace("Deleted: {}", log);
        }
        return log;
    }

    public List<LegacyJob> getAllJobs() {
        return LegacyJob.getAll(entityManager);
    }

    public List<LegacyJob> getAllByStatus(JobStatus status) {
        return LegacyJob.getAllByStatus(entityManager, status);
    }

    public LegacyJob getJobById(Long jobId) {
        return entityManager.find(LegacyJob.class, jobId);
    }

    public Long countAllJobs() {
        return LegacyJob.countAll(entityManager);
    }

    public Long countJobsByStatus(JobStatus jobStatus) {
        return LegacyJob.countAllByStatus(entityManager, jobStatus);
    }

    public LegacyJob getJobByClassName(String className) {
        return LegacyJob.getByWorkerClassName(entityManager, className);
    }

    public List<LegacyJob> getAllScheduledJobs() {
        return LegacyJob.getAllScheduled(entityManager);
    }

    public LegacyJob createJob(String name, String description, List<String> tags, String workerClassName, String parametersClassName, String schedule,
                    JobStatus status, int threads, Integer maxPerMinute, int failRetries, int retryDelay, int daysUntilCleanUp, boolean uniqueInQueue) {

        LegacyJob dbJob = new LegacyJob();
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

    public LegacyJob updateJob(Long jobId, String name, String description, List<String> tags, String workerClassName, String schedule, JobStatus status,
                    int threads, Integer maxPerMinute, int failRetries, int retryDelay, int daysUntilCleanUp, boolean uniqueInQueue) {

        LegacyJob dbJob = getJobById(jobId);
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

        logger.trace("Job updated: {}", dbJob);
        return dbJob;
    }

    public void deleteJob(Long jobId) {

        LegacyJob job = getJobById(jobId);

        int deletedJobExecutions = LegacyExecution.deleteAllByJobId(entityManager, jobId);
        int deletedJobLogs = deleteAllLogsByJobId(jobId);

        entityManager.remove(job);

        String logMessage = String.format("Job removed (including %d executions and %d logs): %s", deletedJobExecutions, deletedJobLogs, job.toString());
        logger.trace(logMessage);
    }

    public LegacyExecution getJobExecutionById(Long jobExecutionId) {
        return entityManager.find(LegacyExecution.class, jobExecutionId);
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
        return LegacyExecution.countBatchByStatus(entityManager, batchId, status);
    }

    public List<LegacyExecution> getJobExecutionBatch(Long batchId) {
        return LegacyExecution.getBatch(entityManager, batchId);
    }

    /**
     * Abort all executions of a batch that are in status {@link ExecutionStatus#QUEUED}
     * 
     * @param batchId the ID of the batch executions
     * @return the amount of executions of that batch that where put in status {@link ExecutionStatus#ABORTED}
     */
    public int abortBatch(Long batchId) {
        return LegacyExecution.abortBatch(entityManager, batchId);
    }

    public List<LegacyExecution> getJobExecutionChain(Long chainId) {
        return LegacyExecution.getChain(entityManager, chainId);
    }

    public void appendExecutionLog(Long jobId, Long executionId, String log) {
        log = log + System.lineSeparator();
        String query = "UPDATE jobengine_execution SET log = CONCAT(IFNULL(log, ''), :log) WHERE id = " + executionId;
        entityManager.createNativeQuery(query).setParameter("log", log).executeUpdate();
    }

    public void appendExecutionFailure(Long jobId, Long executionId, String error, String stacktrace) {
        String query = "UPDATE jobengine_execution SET fail_message = :error, fail_stacktrace = :stacktrace WHERE id = " + executionId;
        entityManager.createNativeQuery(query).setParameter("error", error).setParameter("stacktrace", stacktrace).executeUpdate();
    }

    /**
     * Abort all executions of a chain that are in status {@link ExecutionStatus#QUEUED}
     * 
     * @param chainId the ID of the chain executions
     * @return the amount of executions of that chain that where put in status {@link ExecutionStatus#ABORTED}
     */
    public int abortChain(Long chainId) {
        return LegacyExecution.abortChain(entityManager, chainId);
    }

    public List<LegacyExecution> getAllByJobIdAndStatus(Long jobId, ExecutionStatus jobExecutionStatus) {
        return LegacyExecution.getAllByJobIdAndStatus(entityManager, jobId, jobExecutionStatus);
    }

    public List<LegacyExecution> getNextCandidates(Long jobId) {
        return LegacyExecution.getNextCandidates(entityManager, jobId, StaticConfig.BUFFER_MAX);
    }

    public List<LegacyExecution> findZombies(LocalDateTime time) {
        return LegacyExecution.findZombies(entityManager, time);
    }

    public LegacyExecution createJobExecution(Long jobId, ExecutionStatus status, boolean priority, LocalDateTime maturity, Long batchId, Long chainId,
                    String parameters, Integer parametersHash, int failRetry, Long failRetryExecutionId) {

        LegacyExecution jobExecution = new LegacyExecution();
        jobExecution.setJobId(jobId);
        jobExecution.setStatus(status);
        jobExecution.setPriority(priority);
        jobExecution.setParameters(parameters);
        jobExecution.setParametersHash(parametersHash);
        jobExecution.setFailRetry(failRetry);
        jobExecution.setFailRetryExecutionId(failRetryExecutionId);
        jobExecution.setMaturity(maturity);
        jobExecution.setBatchId(batchId);
        jobExecution.setChainId(chainId);

        entityManager.persist(jobExecution);
        logger.debug("JobExecution created: {}", jobExecution);
        return jobExecution;
    }

    public LegacyExecution updateJobExecution(Long jobExecutionId, ExecutionStatus status, String parameters, boolean priority, LocalDateTime maturity,
                    int fails, Long batchId, Long chainId, Long duration, LocalDateTime startedAt, LocalDateTime endedAt, Long failRetryExecutionId) {

        LegacyExecution jobExecution = getJobExecutionById(jobExecutionId);
        jobExecution.setStatus(status);
        jobExecution.setParameters(parameters);
        jobExecution.setPriority(priority);
        jobExecution.setMaturity(maturity);
        jobExecution.setBatchId(batchId);
        jobExecution.setChainId(chainId);
        jobExecution.setDuration(duration);
        jobExecution.setStartedAt(startedAt);
        jobExecution.setFailRetryExecutionId(failRetryExecutionId);
        jobExecution.setEndedAt(endedAt);
        jobExecution.setFailRetry(fails);
        logger.trace("JobExecution updated: {}", jobExecution);
        return jobExecution;
    }

    public LegacyExecution updateJobExecutionStatus(Long jobExecutionId, ExecutionStatus status) {

        LegacyExecution jobExecution = getJobExecutionById(jobExecutionId);
        jobExecution.setStatus(status);
        logger.trace("JobExecution updated: {}", jobExecution);
        return jobExecution;
    }

    public LegacyExecution getFirstCreatedByJobIdAndParametersHash(Long jobId, Object parametersHash) {
        return LegacyExecution.getFirstCreatedByJobIdAndParametersHash(entityManager, jobId, parametersHash);
    }

    public void deleteJobExecution(Long jobExecutionId) {

        LegacyExecution jobExecution = getJobExecutionById(jobExecutionId);
        entityManager.remove(jobExecution);
        logger.trace("JobExecution removed: {}", jobExecution);
    }

    public int deleteOlderJobExecutions(Long jobId, LocalDateTime preDate) {
        return LegacyExecution.deleteOlderJobExecutions(entityManager, jobId, preDate);
    }

    public List<JobExecutionStatusSummary> getJobExecutionStatusSummaries(ExecutionStatus status, LocalDateTime since) {

        List<JobExecutionStatusSummary> executionStatusSummaries = new ArrayList<JobExecutionStatusSummary>();
        List<Long> jobIds;
        if (since == null) {
            jobIds = LegacyExecution.countDistinctJobIdByStatus(entityManager, status);
        } else {
            jobIds = LegacyExecution.countDistinctJobIdByStatusAndSince(entityManager, since, status);
        }
        for (Long jobId : jobIds) {

            LegacyJob legacyJob = getJobById(jobId);
            Job job = new Job();
            job.setId(legacyJob.getId());
            job.setName(legacyJob.getName());
            job.setDescription(legacyJob.getDescription());
            job.setWorkerClassName(legacyJob.getWorkerClassName());
            job.setParametersClassName(legacyJob.getParametersClassName());
            job.setStatus(legacyJob.getStatus());
            job.setThreads(legacyJob.getThreads());
            job.setMaxPerMinute(legacyJob.getMaxPerMinute());
            job.setFailRetries(legacyJob.getFailRetries());
            job.setRetryDelay(legacyJob.getRetryDelay());
            job.setMinutesUntilCleanUp(legacyJob.getDaysUntilCleanUp() / 24 / 60);
            job.setUniqueQueued(legacyJob.isUniqueInQueue());
            job.setSchedule(legacyJob.getSchedule());
            job.setCreatedAt(legacyJob.getCreatedAt());
            job.setUpdatedAt(legacyJob.getUpdatedAt());

            Long count;
            if (since == null) {
                count = LegacyExecution.countByJobIdAndStatus(entityManager, jobId, status);
            } else {
                count = LegacyExecution.countByJobIdAndStatusAndSince(entityManager, jobId, since, status);
            }
            JobExecutionStatusSummary summary = new JobExecutionStatusSummary(status, count, job);

            executionStatusSummaries.add(summary);
        }

        return executionStatusSummaries;
    }

    public JobExecutionCount getJobExecutionCounts(Long jobId, LocalDateTime from, LocalDateTime to) {

        JobExecutionCounts jobExecutionCounts = JobExecutionCounts.query(entityManager, jobId, from, to);

        return new JobExecutionCount(jobId, from, to, jobExecutionCounts.getTotal(), jobExecutionCounts.getPlanned(), jobExecutionCounts.getQueued(),
                        jobExecutionCounts.getRunning(), jobExecutionCounts.getFinished(), jobExecutionCounts.getFailed(), jobExecutionCounts.getAborted());
    }
}
