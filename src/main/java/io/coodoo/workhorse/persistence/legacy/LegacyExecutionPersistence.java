package io.coodoo.workhorse.persistence.legacy;

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
import io.coodoo.workhorse.core.entity.JobExecutionStatusSummary;
import io.coodoo.workhorse.persistence.interfaces.ExecutionPersistence;
import io.coodoo.workhorse.persistence.interfaces.listing.ListingResult;
import io.coodoo.workhorse.persistence.legacy.boundary.LegacyPersistenceConfig;
import io.coodoo.workhorse.persistence.legacy.control.LegacyController;
import io.coodoo.workhorse.persistence.legacy.entity.LegacyExecution;

/**
 * Legacy support for the Persistence of Workhorse version 1.5
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class LegacyExecutionPersistence implements ExecutionPersistence {

    @Inject
    LegacyController legacyController;

    @Override
    public Execution getById(Long jobId, Long executionId) {
        return map(legacyController.getJobExecutionById(executionId));
    }

    private Execution map(LegacyExecution jobExecution) {
        if (jobExecution == null) {
            return null;
        }
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

        return legacyController.listExecutions(listingParameters).getResults().stream().map(e -> map(e)).collect(Collectors.toList());
    }

    @Override
    public io.coodoo.workhorse.persistence.interfaces.listing.ListingResult<Execution> getExecutionListing(Long jobId,
                    io.coodoo.workhorse.persistence.interfaces.listing.ListingParameters listingParameters) {

        ListingParameters params = new ListingParameters(listingParameters.getPage(), listingParameters.getLimit(), listingParameters.getSortAttribute());
        params.setFilterAttributes(listingParameters.getFilterAttributes());
        params.addFilterAttributes("jobId", jobId.toString());
        params.setFilter(listingParameters.getFilter());

        io.coodoo.framework.listing.boundary.ListingResult<LegacyExecution> result = legacyController.listExecutions(params);
        List<Execution> results = result.getResults().stream().map(l -> map(l)).collect(Collectors.toList());

        io.coodoo.workhorse.persistence.interfaces.listing.Metadata metadata =
                        new io.coodoo.workhorse.persistence.interfaces.listing.Metadata(result.getMetadata().getCount(), listingParameters);

        return new ListingResult<Execution>(results, metadata);
    }

    @Override
    public List<Execution> pollNextExecutions(Long jobId, int limit) {
        return legacyController.getNextCandidates(jobId).stream().map(e -> map(e)).collect(Collectors.toList());
    }

    @Override
    public Execution persist(Execution execution) {
        return map(legacyController.createJobExecution(execution.getJobId(), execution.getStatus(), execution.isPriority(), execution.getPlannedFor(),
                        execution.getBatchId(), execution.getChainId(), execution.getParameters(), execution.getParametersHash(), execution.getFailRetry(),
                        execution.getFailRetryExecutionId()));
    }

    @Override
    public void delete(Long jobId, Long executionId) {
        legacyController.deleteJobExecution(executionId);
    }

    @Override
    public Execution update(Execution execution) {
        return map(legacyController.updateJobExecution(execution.getId(), execution.getStatus(), execution.getParameters(), execution.isPriority(),
                        execution.getPlannedFor(), execution.getFailRetry(), execution.getBatchId(), execution.getChainId(), execution.getDuration(),
                        execution.getStartedAt(), execution.getEndedAt(), execution.getFailRetryExecutionId()));
    }

    @Override
    public Execution updateStatus(Long jobId, Long executionId, ExecutionStatus status, ExecutionFailStatus failStatus) {
        return map(legacyController.updateJobExecutionStatus(executionId, status));
    }

    @Override
    public int deleteOlderExecutions(Long jobId, LocalDateTime preDate) {
        return legacyController.deleteOlderJobExecutions(jobId, preDate);
    }

    @Override
    public List<Execution> getBatch(Long jobId, Long batchId) {
        return legacyController.getJobExecutionBatch(batchId).stream().map(e -> map(e)).collect(Collectors.toList());
    }

    @Override
    public List<Execution> getChain(Long jobId, Long chainId) {
        return legacyController.getJobExecutionChain(chainId).stream().map(e -> map(e)).collect(Collectors.toList());
    }

    @Override
    public Execution getFirstCreatedByJobIdAndParametersHash(Long jobId, Integer parameterHash) {
        return map(legacyController.getFirstCreatedByJobIdAndParametersHash(jobId, parameterHash));
    }

    @Override
    public boolean isBatchFinished(Long jobId, Long batchId) {
        return legacyController.isBatchFinished(batchId);
    }

    @Override
    public boolean abortChain(Long jobId, Long chainId) {
        return legacyController.abortChain(chainId) > 0;
    }

    @Override
    public List<Execution> findTimeoutExecutions(LocalDateTime time) {
        return legacyController.findZombies(time).stream().map(e -> map(e)).collect(Collectors.toList());
    }

    @Override
    public ExecutionLog getLog(Long jobId, Long executionId) {

        LegacyExecution jobExecution = legacyController.getJobExecutionById(executionId);
        if (jobExecution == null) {
            return null;
        }
        ExecutionLog executionLog = new ExecutionLog();
        executionLog.setId(jobExecution.getId());
        executionLog.setExecutionId(jobExecution.getId());
        executionLog.setLog(jobExecution.getLog());
        executionLog.setError(jobExecution.getFailMessage());
        executionLog.setStacktrace(jobExecution.getFailStacktrace());
        executionLog.setCreatedAt(jobExecution.getCreatedAt());
        executionLog.setUpdatedAt(jobExecution.getUpdatedAt());

        return executionLog;
    }

    @Override
    public void log(Long jobId, Long executionId, String log) {
        legacyController.appendExecutionLog(jobId, executionId, log);
    }

    @Override
    public void log(Long jobId, Long executionId, String error, String stacktrace) {
        legacyController.appendExecutionFailure(jobId, executionId, error, stacktrace);
    }

    @Override
    public void connect(Object... params) {
        // TODO ?!
    }

    @Override
    public String getPersistenceName() {
        return LegacyPersistenceConfig.NAME;
    }

    @Override
    public boolean isPusherAvailable() {
        return false;
    }

    @Override
    public List<JobExecutionStatusSummary> getJobExecutionStatusSummaries(ExecutionStatus status, LocalDateTime since) {
    	return legacyController.getJobExecutionStatusSummaries(status, since);
    }

}