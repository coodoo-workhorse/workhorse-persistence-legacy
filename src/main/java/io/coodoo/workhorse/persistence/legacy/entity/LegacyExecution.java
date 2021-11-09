package io.coodoo.workhorse.persistence.legacy.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;

import io.coodoo.framework.jpa.entity.AbstractIdCreatedUpdatedAtEntity;
import io.coodoo.workhorse.core.entity.Execution;
import io.coodoo.workhorse.core.entity.ExecutionFailStatus;
import io.coodoo.workhorse.core.entity.ExecutionStatus;
import io.coodoo.workhorse.util.WorkhorseUtil;

/**
 * <p>
 * A JobExceuction defines a single job which will be excecuted by the job engine.
 * </p>
 * <p>
 * Every needed information to do a single job is stored with this entity.
 * </p>
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@Entity
@Table(name = "jobengine_execution")
@NamedQueries({

                @NamedQuery(name = "JobExecution.getAllByJobId", query = "SELECT j FROM LegacyExecution j WHERE j.jobId = :jobId"),
                @NamedQuery(name = "JobExecution.deleteAllByJobId", query = "DELETE FROM LegacyExecution j WHERE j.jobId = :jobId"),
                @NamedQuery(name = "JobExecution.getAllByStatus", query = "SELECT j FROM LegacyExecution j WHERE j.status = :status"),
                @NamedQuery(name = "JobExecution.getAllByJobIdAndStatus",
                                query = "SELECT j FROM LegacyExecution j WHERE j.jobId = :jobId AND j.status = :status"),

                // Poller
                @NamedQuery(name = "JobExecution.getNextCandidates",
                                query = "SELECT j FROM LegacyExecution j WHERE j.jobId = :jobId AND (j.status = 'QUEUED' OR j.status = 'PLANNED') AND (j.maturity IS NULL OR j.maturity < :currentTime) AND j.chainPreviousExecutionId IS NULL ORDER BY j.priority, j.createdAt"),

                // Batch
                @NamedQuery(name = "JobExecution.getBatch", query = "SELECT j FROM LegacyExecution j WHERE j.batchId = :batchId ORDER BY j.createdAt, j.id"),
                @NamedQuery(name = "JobExecution.countBatchByStatus",
                                query = "SELECT COUNT(j) FROM LegacyExecution j WHERE j.batchId = :batchId AND j.status = :status"),
                @NamedQuery(name = "JobExecution.abortBatch",
                                query = "UPDATE LegacyExecution j SET j.status = 'ABORTED' WHERE j.batchId = :batchId AND j.status = 'QUEUED'"),

                // Chained
                @NamedQuery(name = "JobExecution.getChain", query = "SELECT j FROM LegacyExecution j WHERE j.chainId = :chainId ORDER BY j.createdAt, j.id"),
                @NamedQuery(name = "JobExecution.getNextInChain",
                                query = "SELECT j FROM LegacyExecution j WHERE j.chainId = :chainId AND j.chainPreviousExecutionId = :jobExecutionId"),
                @NamedQuery(name = "JobExecution.abortChain",
                                query = "UPDATE LegacyExecution j SET j.status = 'FAILED' WHERE j.chainId = :chainId AND j.status = 'QUEUED'"),

                // Misc
                @NamedQuery(name = "JobExecution.deleteOlderJobExecutions",
                                query = "DELETE FROM LegacyExecution j WHERE j.jobId = :jobId AND j.createdAt < :preDate AND (j.status = 'FINISHED' OR j.status = 'FAILED')"),
                @NamedQuery(name = "JobExecution.selectDuration", query = "SELECT j.duration FROM LegacyExecution j WHERE j.id = :jobExecutionId"),
                @NamedQuery(name = "JobExecution.findZombies", query = "SELECT j FROM LegacyExecution j WHERE j.startedAt < :time AND j.status = 'RUNNING'"),

                // Status
                @NamedQuery(name = "JobExecution.updateStatusRunning",
                                query = "UPDATE LegacyExecution j SET j.status = 'RUNNING', j.startedAt = :startedAt, j.updatedAt = :startedAt WHERE j.id = :jobExecutionId"),
                @NamedQuery(name = "JobExecution.updateStatusFinished",
                                query = "UPDATE LegacyExecution j SET j.status = 'FINISHED', j.endedAt = :endedAt, j.duration = :duration, j.log = :log, j.updatedAt = :endedAt WHERE j.id = :jobExecutionId"),

                // Analytic
                @NamedQuery(name = "JobExecution.getFirstCreatedByJobIdAndParametersHash",
                                query = "SELECT j FROM LegacyExecution j WHERE j.jobId = :jobId AND j.status = 'QUEUED' AND (j.parametersHash IS NULL OR j.parametersHash = :parametersHash) ORDER BY j.createdAt ASC"),
                @NamedQuery(name = "JobExecution.countQueudByJobIdAndParamters",
                                query = "SELECT COUNT(j) FROM LegacyExecution j WHERE j.jobId = :jobId AND j.status = 'QUEUED' and (j.parameters IS NULL or j.parameters = :parameters)"),
                @NamedQuery(name = "JobExecution.countByJobIdAndStatus",
                                query = "SELECT COUNT(j) FROM LegacyExecution j WHERE j.jobId = :jobId AND j.status = :status"),
                @NamedQuery(name = "JobExecution.countDistinctJobIdByStatus",
                                query = "SELECT DISTINCT j.jobId FROM LegacyExecution j WHERE j.status = :status"),
                @NamedQuery(name = "JobExecution.countDistinctJobIdByStatusAndSince",
                                query = "SELECT DISTINCT j.jobId FROM LegacyExecution j WHERE j.status = :status AND j.createdAt > :since"),
                @NamedQuery(name = "JobExecution.countByJobIdAndStatusAndSince",
                                query = "SELECT count(j) FROM LegacyExecution j WHERE j.jobId = :jobId AND j.status = :status AND j.createdAt > :since"),

})

public class LegacyExecution extends AbstractIdCreatedUpdatedAtEntity {

    private static final long serialVersionUID = 1L;

    /**
     * The reference to the job
     */
    @Column(name = "job_id")
    private Long jobId;

    /**
     * The job excecution status e.g. QUEUED or FINISHED.
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration")
    private Long duration;

    /**
     * If a job exectution has the priority set to <code>true</code> it will be executed before all jobs with priority <code>false</code>.
     */
    @Column(name = "priority")
    private boolean priority;

    /**
     * If a maturity is given, the job execution will not be executed before this this time.
     */
    @Column(name = "maturity")
    private LocalDateTime maturity;

    /**
     * A short message to summarize an execution.
     */
    @Column(name = "summary")
    private String summary;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "chain_id")
    private Long chainId;

    @Column(name = "chain_previous_execution_id")
    private Long chainPreviousExecutionId;

    @Column(name = "parameters")
    private String parameters;

    @Column(name = "parameters_hash")
    private Integer parametersHash;

    @Column(name = "log")
    private String log;

    @Column(name = "fail_retry")
    private int failRetry;

    @Column(name = "fail_retry_execution_id")
    private Long failRetryExecutionId;

    /**
     * The exception stacktrace, if the job execution ends in an exception.
     */
    @Column(name = "fail_stacktrace")
    private String failStacktrace;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public LocalDateTime getMaturity() {
        return maturity;
    }

    public void setMaturity(LocalDateTime maturity) {
        this.maturity = maturity;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getChainId() {
        return chainId;
    }

    public void setChainId(Long chainId) {
        this.chainId = chainId;
    }

    public Long getChainPreviousExecutionId() {
        return chainPreviousExecutionId;
    }

    public void setChainPreviousExecutionId(Long chainPreviousExecutionId) {
        this.chainPreviousExecutionId = chainPreviousExecutionId;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Integer getParametersHash() {
        return parametersHash;
    }

    public void setParametersHash(Integer parametersHash) {
        this.parametersHash = parametersHash;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public int getFailRetry() {
        return failRetry;
    }

    public void setFailRetry(int failRetry) {
        this.failRetry = failRetry;
    }

    public Long getFailRetryExecutionId() {
        return failRetryExecutionId;
    }

    public void setFailRetryExecutionId(Long failRetryExecutionId) {
        this.failRetryExecutionId = failRetryExecutionId;
    }

    public String getFailStacktrace() {
        return failStacktrace;
    }

    public void setFailStacktrace(String failStacktrace) {
        this.failStacktrace = failStacktrace;
    }

    @Override
    public String toString() {
        return "JobExecution [id=" + id + ", jobId=" + jobId + ", status=" + status + ", startedAt=" + startedAt + ", endedAt=" + endedAt + ", duration="
                        + duration + ", priority=" + priority + ", maturity=" + maturity + ", batchId=" + batchId + ", chainId=" + chainId
                        + ", chainPreviousExecutionId=" + chainPreviousExecutionId + ", parameters=" + parameters + ", parametersHash=" + parametersHash
                        + ", failRetry=" + failRetry + ", failRetryExecutionId=" + failRetryExecutionId + "]";
    }

    public static Execution map(LegacyExecution jobExecution) {
        if (jobExecution == null) {
            return null;
        }
        Execution execution = new Execution();
        execution.setId(jobExecution.getId());
        execution.setJobId(jobExecution.getJobId());
        execution.setStatus(jobExecution.getStatus());
        execution.setSummary(jobExecution.getSummary());
        execution.setFailStatus(ExecutionFailStatus.NONE);
        execution.setStartedAt(jobExecution.getStartedAt());
        execution.setEndedAt(jobExecution.getEndedAt());
        execution.setDuration(jobExecution.getDuration());
        execution.setPriority(jobExecution.isPriority());
        execution.setPlannedFor(jobExecution.getMaturity());
        execution.setBatchId(jobExecution.getBatchId());
        execution.setChainId(jobExecution.getChainId());
        execution.setParameters(jobExecution.getParameters());
        execution.setParametersHash(jobExecution.getParametersHash());
        execution.setFailRetry(jobExecution.getFailRetry());
        execution.setFailRetryExecutionId(jobExecution.getFailRetryExecutionId());
        execution.setCreatedAt(jobExecution.getCreatedAt());
        execution.setUpdatedAt(jobExecution.getUpdatedAt());
        return execution;
    }

    /**
     * Executes the query 'JobExecution.getAllByJobId' returning a list of result objects.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @return List of result objects
     */
    @SuppressWarnings("unchecked")
    public static List<LegacyExecution> getAllByJobId(EntityManager entityManager, Long jobId) {
        Query query = entityManager.createNamedQuery("JobExecution.getAllByJobId");
        query = query.setParameter("jobId", jobId);
        return query.getResultList();
    }

    /**
     * Executes the query 'JobExecution.deleteAllByJobId' returning the number of affected rows.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @return Number of deleted objects
     */
    public static int deleteAllByJobId(EntityManager entityManager, Long jobId) {
        Query query = entityManager.createNamedQuery("JobExecution.deleteAllByJobId");
        query = query.setParameter("jobId", jobId);
        return query.executeUpdate();
    }

    /**
     * Executes the query 'JobExecution.getAllByStatus' returning a list of result objects.
     *
     * @param entityManager the entityManager
     * @param status the status
     * @return List of result objects
     */
    @SuppressWarnings("unchecked")
    public static List<LegacyExecution> getAllByStatus(EntityManager entityManager, ExecutionStatus status) {
        Query query = entityManager.createNamedQuery("JobExecution.getAllByStatus");
        query = query.setParameter("status", status);
        return query.getResultList();
    }

    /**
     * Executes the query 'JobExecution.getAllByJobIdAndStatus' returning a list of result objects.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @param status the status
     * @return List of result objects
     */
    @SuppressWarnings("unchecked")
    public static List<LegacyExecution> getAllByJobIdAndStatus(EntityManager entityManager, Long jobId, ExecutionStatus status) {
        Query query = entityManager.createNamedQuery("JobExecution.getAllByJobIdAndStatus");
        query = query.setParameter("jobId", jobId);
        query = query.setParameter("status", status);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public static List<LegacyExecution> getNextCandidates(EntityManager entityManager, Long jobId, int maxResults) {
        Query query = entityManager.createNamedQuery("JobExecution.getNextCandidates");
        query = query.setParameter("jobId", jobId);
        query = query.setParameter("currentTime", WorkhorseUtil.timestamp());
        query = query.setMaxResults(maxResults);
        return query.getResultList();
    }

    /**
     * Executes the query 'JobExecution.getBatch' returning a list of result objects.
     *
     * @param entityManager the entityManager
     * @param batchId the batchId
     * @return List of result objects
     */
    @SuppressWarnings("unchecked")
    public static List<LegacyExecution> getBatch(EntityManager entityManager, Long batchId) {
        Query query = entityManager.createNamedQuery("JobExecution.getBatch");
        query = query.setParameter("batchId", batchId);
        return query.getResultList();
    }

    /**
     * Executes the query 'JobExecution.countBatchByStatus' returning one/the first object or null if nothing has been found.
     *
     * @param entityManager the entityManager
     * @param batchId the batchId
     * @param status the status
     * @return the result
     */
    public static Long countBatchByStatus(EntityManager entityManager, Long batchId, ExecutionStatus status) {
        Query query = entityManager.createNamedQuery("JobExecution.countBatchByStatus");
        query = query.setParameter("batchId", batchId);
        query = query.setParameter("status", status);
        query = query.setMaxResults(1);
        @SuppressWarnings("rawtypes")
        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return (Long) results.get(0);
    }

    /**
     * Executes the query 'JobExecution.abortBatch' returning the number of affected rows.
     *
     * @param entityManager the entityManager
     * @param batchId the batchId
     * @return Number of updated objects
     */
    public static int abortBatch(EntityManager entityManager, Object batchId) {
        Query query = entityManager.createNamedQuery("JobExecution.abortBatch");
        query = query.setParameter("batchId", batchId);
        return query.executeUpdate();
    }

    /**
     * Executes the query 'JobExecution.getChain' returning a list of result objects.
     *
     * @param entityManager the entityManager
     * @param chainId the chainId
     * @return List of result objects
     */
    @SuppressWarnings("unchecked")
    public static List<LegacyExecution> getChain(EntityManager entityManager, Long chainId) {
        Query query = entityManager.createNamedQuery("JobExecution.getChain");
        query = query.setParameter("chainId", chainId);
        return query.getResultList();
    }

    /**
     * Executes the query 'JobExecution.getNextInChain' returning one/the first object or null if nothing has been found.
     *
     * @param entityManager the entityManager
     * @param chainId the chainId
     * @param jobExecutionId the jobExecutionId
     * @return the result
     */
    public static LegacyExecution getNextInChain(EntityManager entityManager, Long chainId, Long jobExecutionId) {
        Query query = entityManager.createNamedQuery("JobExecution.getNextInChain");
        query = query.setParameter("chainId", chainId);
        query = query.setParameter("jobExecutionId", jobExecutionId);
        query = query.setMaxResults(1);
        @SuppressWarnings("rawtypes")
        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return (LegacyExecution) results.get(0);
    }

    /**
     * Executes the query 'JobExecution.abortChain' returning the number of affected rows.
     *
     * @param entityManager the entityManager
     * @param chainId the chainId
     * @return Number of updated objects
     */
    public static int abortChain(EntityManager entityManager, Long chainId) {
        Query query = entityManager.createNamedQuery("JobExecution.abortChain");
        query = query.setParameter("chainId", chainId);
        return query.executeUpdate();
    }

    /**
     * Executes the query 'JobExecution.deleteOlderJobExecutions' returning the number of affected rows.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @param preDate the preDate
     * @return Number of deleted objects
     */
    public static int deleteOlderJobExecutions(EntityManager entityManager, Long jobId, LocalDateTime preDate) {
        Query query = entityManager.createNamedQuery("JobExecution.deleteOlderJobExecutions");
        query = query.setParameter("jobId", jobId);
        query = query.setParameter("preDate", preDate);
        return query.executeUpdate();
    }

    /**
     * Executes the query 'JobExecution.selectDuration' returning one/the first object or null if nothing has been found.
     *
     * @param entityManager the entityManager
     * @param jobExecutionId the jobExecutionId
     * @return the result
     */
    public static Long selectDuration(EntityManager entityManager, Long jobExecutionId) {
        Query query = entityManager.createNamedQuery("JobExecution.selectDuration");
        query = query.setParameter("jobExecutionId", jobExecutionId);
        query = query.setMaxResults(1);
        @SuppressWarnings("rawtypes")
        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return (Long) results.get(0);
    }

    /**
     * Executes the query 'JobExecution.findZombies' returning a list of result objects.
     *
     * @param entityManager the entityManager
     * @param time the time
     * @return List of result objects
     */
    @SuppressWarnings("unchecked")
    public static List<LegacyExecution> findZombies(EntityManager entityManager, LocalDateTime time) {
        Query query = entityManager.createNamedQuery("JobExecution.findZombies");
        query = query.setParameter("time", time);
        return query.getResultList();
    }

    /**
     * Executes the query 'JobExecution.updateStatusRunning' returning the number of affected rows.
     *
     * @param entityManager the entityManager
     * @param startedAt the startedAt
     * @param jobExecutionId the jobExecutionId
     * @return Number of updated objects
     */
    public static int updateStatusRunning(EntityManager entityManager, LocalDateTime startedAt, Long jobExecutionId) {
        Query query = entityManager.createNamedQuery("JobExecution.updateStatusRunning");
        query = query.setParameter("startedAt", startedAt);
        query = query.setParameter("jobExecutionId", jobExecutionId);
        return query.executeUpdate();
    }

    /**
     * Executes the query 'JobExecution.updateStatusFinished' returning the number of affected rows.
     *
     * @param entityManager the entityManager
     * @param endedAt the endedAt
     * @param duration the duration
     * @param log the log
     * @param jobExecutionId the jobExecutionId
     * @return Number of updated objects
     */
    public static int updateStatusFinished(EntityManager entityManager, LocalDateTime endedAt, Long duration, String log, Long jobExecutionId) {
        Query query = entityManager.createNamedQuery("JobExecution.updateStatusFinished");
        query = query.setParameter("endedAt", endedAt);
        query = query.setParameter("duration", duration);
        query = query.setParameter("log", log);
        query = query.setParameter("jobExecutionId", jobExecutionId);
        return query.executeUpdate();
    }

    /**
     * Executes the query 'JobExecution.getFirstCreatedByJobIdAndParameterHash' returning a list of result objects.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @param parametersHash the parameterHash
     * @return List of result objects
     */
    @SuppressWarnings("unchecked")
    public static LegacyExecution getFirstCreatedByJobIdAndParametersHash(EntityManager entityManager, Long jobId, Object parametersHash) {
        Query query = entityManager.createNamedQuery("JobExecution.getFirstCreatedByJobIdAndParametersHash");
        query = query.setParameter("jobId", jobId);
        query = query.setParameter("parametersHash", parametersHash);
        query = query.setMaxResults(1);
        List<LegacyExecution> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return null;
        } else {
            return resultList.get(0);
        }
    }

    /**
     * Executes the query 'JobExecution.countQueudByJobIdAndParamters' returning one/the first object or null if nothing has been found.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @param parameters the parameters
     * @return the result
     */
    public static Long countQueudByJobIdAndParamters(EntityManager entityManager, Long jobId, String parameters) {
        Query query = entityManager.createNamedQuery("JobExecution.countQueudByJobIdAndParamters");
        query = query.setParameter("jobId", jobId);
        query = query.setParameter("parameters", parameters);
        query = query.setMaxResults(1);
        @SuppressWarnings("rawtypes")
        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return (Long) results.get(0);
    }

    /**
     * Executes the query 'JobExecution.countDistinctJobIdByStatus' returning a list of result objects.
     *
     * @param entityManager the entityManager
     * @param status the status
     * @return List of result objects
     */
    @SuppressWarnings("unchecked")
    public static List<Long> countDistinctJobIdByStatus(EntityManager entityManager, ExecutionStatus status) {
        Query query = entityManager.createNamedQuery("JobExecution.countDistinctJobIdByStatus");
        query = query.setParameter("status", status);
        return query.getResultList();
    }

    /**
     * Executes the query 'JobExecution.countDistinctJobIdByStatusAndSince' returning a list of result objects.
     *
     * @param entityManager the entityManager
     * @param since the since
     * @param status the status
     * @return List of result objects
     */
    @SuppressWarnings("unchecked")
    public static List<Long> countDistinctJobIdByStatusAndSince(EntityManager entityManager, LocalDateTime since, ExecutionStatus status) {
        Query query = entityManager.createNamedQuery("JobExecution.countDistinctJobIdByStatusAndSince");
        query = query.setParameter("since", since);
        query = query.setParameter("status", status);
        return query.getResultList();
    }

    /**
     * Executes the query 'JobExecution.countByJobIdAndStatus' returning one/the first object or null if nothing has been found.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @param status the status
     * @return the result
     */
    public static Long countByJobIdAndStatus(EntityManager entityManager, Long jobId, ExecutionStatus status) {
        Query query = entityManager.createNamedQuery("JobExecution.countByJobIdAndStatus");
        query = query.setParameter("jobId", jobId);
        query = query.setParameter("status", status);
        query = query.setMaxResults(1);
        @SuppressWarnings("rawtypes")
        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return (Long) results.get(0);
    }

    /**
     * Executes the query 'JobExecution.countByJobIdAndStatusAndSince' returning one/the first object or null if nothing has been found.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @param since the since
     * @param status the status
     * @return the result
     */
    public static Long countByJobIdAndStatusAndSince(EntityManager entityManager, Long jobId, LocalDateTime since, ExecutionStatus status) {
        Query query = entityManager.createNamedQuery("JobExecution.countByJobIdAndStatusAndSince");
        query = query.setParameter("jobId", jobId);
        query = query.setParameter("since", since);
        query = query.setParameter("status", status);
        query = query.setMaxResults(1);
        @SuppressWarnings("rawtypes")
        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return (Long) results.get(0);
    }

}
