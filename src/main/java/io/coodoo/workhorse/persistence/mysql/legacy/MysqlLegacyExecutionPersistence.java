package io.coodoo.workhorse.persistence.mysql.legacy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.coodoo.framework.listing.boundary.ListingParameters;
import io.coodoo.workhorse.core.entity.Execution;
import io.coodoo.workhorse.core.entity.ExecutionFailStatus;
import io.coodoo.workhorse.core.entity.ExecutionLog;
import io.coodoo.workhorse.core.entity.ExecutionStatus;
import io.coodoo.workhorse.persistence.interfaces.ExecutionPersistence;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MySQLLegacyConfig;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MySQLLegacyService;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.JobExecution;

/**
 * Legacy support for the MySQL Persistence of Workhorse version 1.5
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class MysqlLegacyExecutionPersistence implements ExecutionPersistence {

    @Inject
    MySQLLegacyService mySQLLegacyService;

    @Override
    public Execution getById(Long jobId, Long executionId) {
        return map(mySQLLegacyService.getJobExecutionById(executionId));
    }

    private Execution map(JobExecution jobExecution) {
        Execution execution = new Execution();
        execution.setId(jobExecution.getId());
        execution.setJobId(jobExecution.getJobId());
        execution.setStatus(jobExecution.getStatus());
        execution.setFailStatus(ExecutionFailStatus.NONE);
        execution.setStartedAt(jobExecution.getStartedAt());
        execution.setEndedAt(jobExecution.getEndedAt());
        execution.setDuration(jobExecution.getDuration());
        execution.setPriority(jobExecution.isPriority());
        execution.setPlannedFor(jobExecution.getMaturity());
        execution.setExpiresAt(null);
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

    @Override
    public List<Execution> getByJobId(Long jobId, Long limit) {

        ListingParameters listingParameters = new ListingParameters(limit.intValue());
        listingParameters.addFilterAttributes("jobId", jobId.toString());

        return mySQLLegacyService.listExecutions(listingParameters).getResults().stream().map(e -> map(e)).collect(Collectors.toList());
    }

    @Override
    public List<Execution> pollNextExecutions(Long jobId, int limit) {
        return mySQLLegacyService.getNextCandidates(jobId).stream().map(e -> map(e)).collect(Collectors.toList());
    }

    @Override
    public Long count() {
        return mySQLLegacyService.countExecutions(new ListingParameters());
    }

    @Override
    public Execution persist(Execution execution) {
        return map(mySQLLegacyService.createJobExecution(execution.getJobId(), execution.getStatus(), execution.isPriority(), execution.getPlannedFor(),
                        execution.getBatchId(), execution.getChainId(), execution.getParameters(), execution.getParametersHash()));
    }

    @Override
    public void delete(Long jobId, Long executionId) {
        mySQLLegacyService.deleteJobExecution(executionId);
    }

    @Override
    public Execution update(Execution execution) {
        return map(mySQLLegacyService.updateJobExecution(execution.getId(), execution.getStatus(), execution.getParameters(), execution.isPriority(),
                        execution.getPlannedFor(), execution.getFailRetry()));
    }

    @Override
    public Execution updateStatus(Long jobId, Long executionId, ExecutionStatus status, ExecutionFailStatus failStatus) {
        return map(mySQLLegacyService.updateJobExecutionStatus(executionId, status));
    }

    @Override
    public int deleteOlderExecutions(Long jobId, LocalDateTime preDate) {
        return mySQLLegacyService.deleteOlderJobExecutions(jobId, preDate);
    }

    @Override
    public List<Execution> getBatch(Long jobId, Long batchId) {
        return mySQLLegacyService.getJobExecutionBatch(batchId).stream().map(e -> map(e)).collect(Collectors.toList());
    }

    @Override
    public List<Execution> getChain(Long jobId, Long chainId) {
        return mySQLLegacyService.getJobExecutionChain(chainId).stream().map(e -> map(e)).collect(Collectors.toList());
    }

    @Override
    public Execution getQueuedBatchExecution(Long jobId, Long batchId) {

        JobExecution jobExecution = mySQLLegacyService.getJobExecutionById(batchId);

        if (jobExecution != null && (jobExecution.getStatus() == ExecutionStatus.QUEUED || jobExecution.getStatus() == ExecutionStatus.PLANNED)) {
            return map(jobExecution);
        }
        return null;
    }

    @Override
    public List<Execution> getFailedBatchExecutions(Long jobId, Long batchId) {

        ListingParameters listingParameters = new ListingParameters(0);
        listingParameters.addFilterAttributes("jobId", jobId.toString());
        listingParameters.addFilterAttributes("batchId", batchId.toString());
        listingParameters.addFilterAttributes("status", ExecutionStatus.FAILED.toString());

        return mySQLLegacyService.listExecutions(listingParameters).getResults().stream().map(e -> map(e)).collect(Collectors.toList());
    }

    @Override
    public Execution getFirstCreatedByJobIdAndParametersHash(Long jobId, Integer parameterHash) {
        return map(mySQLLegacyService.getFirstCreatedByJobIdAndParametersHash(jobId, parameterHash));
    }

    @Override
    public boolean isBatchFinished(Long jobId, Long batchId) {
        return mySQLLegacyService.isBatchFinished(batchId);
    }

    @Override
    public boolean abortChain(Long jobId, Long chainId) {
        return mySQLLegacyService.abortChain(chainId) > 0;
    }

    @Override
    public List<Execution> findTimeoutExecutions(LocalDateTime time) {
        return mySQLLegacyService.findZombies(time).stream().map(e -> map(e)).collect(Collectors.toList());
    }

    @Override
    public ExecutionLog getLog(Long jobId, Long executionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void log(Long jobId, Long executionId, String log) {
        // TODO Auto-generated method stub

    }

    @Override
    public void log(Long jobId, Long executionId, String error, String stacktrace) {
        // TODO Auto-generated method stub

    }

    @Override
    public void connect(Object... params) {
        // TODO ?!
    }

    @Override
    public String getPersistenceName() {
        return MySQLLegacyConfig.NAME;
    }

    @Override
    public boolean isPusherAvailable() {
        return false;
    }

}
