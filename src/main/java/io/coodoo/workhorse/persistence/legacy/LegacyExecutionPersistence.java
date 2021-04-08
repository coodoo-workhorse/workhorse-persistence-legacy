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
import io.coodoo.workhorse.core.entity.JobExecutionCount;
import io.coodoo.workhorse.core.entity.JobExecutionStatusSummary;
import io.coodoo.workhorse.persistence.interfaces.ExecutionPersistence;
import io.coodoo.workhorse.persistence.interfaces.listing.ListingResult;
import io.coodoo.workhorse.persistence.legacy.boundary.LegacyPersistenceConfig;
import io.coodoo.workhorse.persistence.legacy.control.LegacyController;
import io.coodoo.workhorse.persistence.legacy.entity.LegacyExecution;
import io.coodoo.workhorse.persistence.legacy.entity.LegacyExecutionView;

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
        return LegacyExecution.map(legacyController.getJobExecutionById(executionId));
    }

    @Override
    public List<Execution> getByJobId(Long jobId, Long limit) {

        ListingParameters listingParameters = new ListingParameters(limit.intValue());
        listingParameters.addFilterAttributes("jobId", jobId.toString());

        return legacyController.listExecutions(listingParameters).getResults().stream().map(e -> LegacyExecution.map(e)).collect(Collectors.toList());
    }

    @Override
    public io.coodoo.workhorse.persistence.interfaces.listing.ListingResult<Execution> getExecutionListing(Long jobId,
                    io.coodoo.workhorse.persistence.interfaces.listing.ListingParameters listingParameters) {

        ListingParameters params = new ListingParameters(listingParameters.getPage(), listingParameters.getLimit(), listingParameters.getSortAttribute());
        params.setFilterAttributes(listingParameters.getFilterAttributes());
        params.addFilterAttributes("jobId", jobId.toString());
        params.setFilter(listingParameters.getFilter());

        io.coodoo.framework.listing.boundary.ListingResult<LegacyExecutionView> result = legacyController.listExecutionViews(params);

        List<Execution> results = result.getResults().stream().map(l -> LegacyExecutionView.map(l)).collect(Collectors.toList());

        io.coodoo.workhorse.persistence.interfaces.listing.Metadata metadata =
                        new io.coodoo.workhorse.persistence.interfaces.listing.Metadata(result.getMetadata().getCount(), listingParameters);

        return new ListingResult<Execution>(results, metadata);
    }

    @Override
    public List<Execution> pollNextExecutions(Long jobId, int limit) {
        return legacyController.getNextCandidates(jobId).stream().map(e -> LegacyExecution.map(e)).collect(Collectors.toList());
    }

    @Override
    public Execution persist(Execution execution) {
        return LegacyExecution.map(legacyController.createJobExecution(execution.getJobId(), execution.getStatus(), execution.isPriority(),
                        execution.getPlannedFor(), execution.getBatchId(), execution.getChainId(), execution.getParameters(), execution.getParametersHash(),
                        execution.getFailRetry(), execution.getFailRetryExecutionId()));
    }

    @Override
    public void delete(Long jobId, Long executionId) {
        legacyController.deleteJobExecution(executionId);
    }

    @Override
    public Execution update(Execution execution) {
        return LegacyExecution.map(legacyController.updateJobExecution(execution.getId(), execution.getStatus(), execution.getParameters(),
                        execution.isPriority(), execution.getPlannedFor(), execution.getFailRetry(), execution.getBatchId(), execution.getChainId(),
                        execution.getDuration(), execution.getStartedAt(), execution.getEndedAt(), execution.getFailRetryExecutionId()));
    }

    @Override
    public Execution updateStatus(Long jobId, Long executionId, ExecutionStatus status, ExecutionFailStatus failStatus) {
        return LegacyExecution.map(legacyController.updateJobExecutionStatus(executionId, status));
    }

    @Override
    public int deleteOlderExecutions(Long jobId, LocalDateTime preDate) {
        return legacyController.deleteOlderJobExecutions(jobId, preDate);
    }

    @Override
    public List<Execution> getBatch(Long jobId, Long batchId) {
        return legacyController.getJobExecutionBatch(batchId).stream().map(e -> LegacyExecution.map(e)).collect(Collectors.toList());
    }

    @Override
    public List<Execution> getChain(Long jobId, Long chainId) {
        return legacyController.getJobExecutionChain(chainId).stream().map(e -> LegacyExecution.map(e)).collect(Collectors.toList());
    }

    @Override
    public Execution getFirstCreatedByJobIdAndParametersHash(Long jobId, Integer parameterHash) {
        return LegacyExecution.map(legacyController.getFirstCreatedByJobIdAndParametersHash(jobId, parameterHash));
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
        return legacyController.findZombies(time).stream().map(e -> LegacyExecution.map(e)).collect(Collectors.toList());
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

    @Override
    public JobExecutionCount getJobExecutionCount(Long jobId, LocalDateTime from, LocalDateTime to) {
        return legacyController.getJobExecutionCounts(jobId, from, to);
    }

}
