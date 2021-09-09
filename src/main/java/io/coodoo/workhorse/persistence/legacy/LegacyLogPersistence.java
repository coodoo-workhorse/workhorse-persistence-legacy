package io.coodoo.workhorse.persistence.legacy;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.coodoo.framework.listing.boundary.ListingParameters;
import io.coodoo.workhorse.core.entity.WorkhorseLog;
import io.coodoo.workhorse.persistence.interfaces.LogPersistence;
import io.coodoo.workhorse.persistence.interfaces.listing.ListingResult;
import io.coodoo.workhorse.persistence.legacy.boundary.LegacyPersistenceConfig;
import io.coodoo.workhorse.persistence.legacy.control.LegacyController;
import io.coodoo.workhorse.persistence.legacy.entity.LegacyLog;

/**
 * Legacy support for the Persistence of Workhorse version 1.5
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class LegacyLogPersistence implements LogPersistence {

    @Inject
    LegacyController legacyController;

    @Override
    public String getPersistenceName() {
        return LegacyPersistenceConfig.NAME;
    }

    @Override
    public WorkhorseLog get(Long logId) {
        return LegacyLog.map(legacyController.getLog(logId));
    }

    @Override
    public ListingResult<WorkhorseLog> getWorkhorseLogListing(io.coodoo.workhorse.persistence.interfaces.listing.ListingParameters listingParameters) {

        ListingParameters params = new ListingParameters(listingParameters.getPage(), listingParameters.getLimit(), listingParameters.getSortAttribute());
        params.setFilterAttributes(listingParameters.getFilterAttributes());
        params.setFilter(listingParameters.getFilter());

        io.coodoo.framework.listing.boundary.ListingResult<LegacyLog> result = legacyController.listLogs(params);
        List<WorkhorseLog> results = result.getResults().stream().map(l -> LegacyLog.map(l)).collect(Collectors.toList());

        io.coodoo.workhorse.persistence.interfaces.listing.Metadata metadata =
                        new io.coodoo.workhorse.persistence.interfaces.listing.Metadata(result.getMetadata().getCount(), listingParameters);

        return new ListingResult<WorkhorseLog>(results, metadata);
    }

    @Override
    public WorkhorseLog update(Long logId, WorkhorseLog workhorseLog) {
        throw new RuntimeException("Dare you changing the log?!");
    }

    @Override
    public WorkhorseLog delete(Long logId) {
        return LegacyLog.map(legacyController.deleteLogsById(logId));
    }

    @Override
    public WorkhorseLog persist(WorkhorseLog workhorseLog) {
        return LegacyLog.map(legacyController.createLog(workhorseLog.getMessage(), workhorseLog.getJobId(), workhorseLog.getJobStatus(),
                        workhorseLog.isByUser(), workhorseLog.getChangeParameter(), workhorseLog.getChangeOld(), workhorseLog.getChangeNew(),
                        workhorseLog.getStacktrace()));
    }

    @Override
    public List<WorkhorseLog> getAll(int limit) {
        return legacyController.listLogs(new ListingParameters(limit)).getResults().stream().map(l -> LegacyLog.map(l)).collect(Collectors.toList());
    }

    @Override
    public int deleteByJobId(Long jobId) {
        return legacyController.deleteAllLogsByJobId(jobId);
    }

}
