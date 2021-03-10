package io.coodoo.workhorse.persistence.mysql.legacy;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.coodoo.framework.listing.boundary.ListingParameters;
import io.coodoo.workhorse.core.entity.WorkhorseLog;
import io.coodoo.workhorse.persistence.interfaces.LogPersistence;
import io.coodoo.workhorse.persistence.interfaces.listing.ListingResult;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MySQLLegacyConfig;
import io.coodoo.workhorse.persistence.mysql.legacy.control.MySQLLegacyController;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.LegacyLog;

/**
 * Legacy support for the MySQL Persistence of Workhorse version 1.5
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class MysqlLegacyLogPersistence implements LogPersistence {

    @Inject
    MySQLLegacyController mySQLLegacyController;

    @Override
    public WorkhorseLog get(Long logId) {
        return map(mySQLLegacyController.getLog(logId));
    }

    private WorkhorseLog map(LegacyLog log) {
        WorkhorseLog workhorseLog = new WorkhorseLog();
        workhorseLog.setId(log.getId());
        workhorseLog.setMessage(log.getMessage());
        workhorseLog.setJobId(log.getJobId());
        workhorseLog.setJobStatus(log.getJobStatus());
        workhorseLog.setByUser(log.isByUser());
        workhorseLog.setChangeParameter(log.getChangeParameter());
        workhorseLog.setChangeOld(log.getChangeOld());
        workhorseLog.setChangeNew(log.getChangeNew());
        workhorseLog.setHostName(log.getHostName());
        workhorseLog.setStacktrace(log.getStacktrace());
        workhorseLog.setCreatedAt(log.getCreatedAt());
        workhorseLog.setUpdatedAt(log.getUpdatedAt());
        return workhorseLog;
    }

    @Override
    public ListingResult<WorkhorseLog> getWorkhorseLogListing(io.coodoo.workhorse.persistence.interfaces.listing.ListingParameters listingParameters) {

        ListingParameters params = new ListingParameters(listingParameters.getPage(), listingParameters.getLimit(), listingParameters.getSortAttribute());
        params.setFilterAttributes(listingParameters.getFilterAttributes());
        params.setFilter(listingParameters.getFilter());

        io.coodoo.framework.listing.boundary.ListingResult<LegacyLog> result = mySQLLegacyController.listLogs(params);
        List<WorkhorseLog> results = result.getResults().stream().map(l -> map(l)).collect(Collectors.toList());

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
        return map(mySQLLegacyController.deleteLogsById(logId));
    }

    @Override
    public WorkhorseLog persist(WorkhorseLog workhorseLog) {
        return map(mySQLLegacyController.createLog(workhorseLog.getMessage(), workhorseLog.getJobId(), workhorseLog.getJobStatus(), workhorseLog.isByUser(),
                        workhorseLog.getChangeParameter(), workhorseLog.getChangeOld(), workhorseLog.getChangeNew(), workhorseLog.getStacktrace()));
    }

    @Override
    public List<WorkhorseLog> getAll(int limit) {
        return mySQLLegacyController.listLogs(new ListingParameters(limit)).getResults().stream().map(l -> map(l)).collect(Collectors.toList());
    }

    @Override
    public int deleteByJobId(Long jobId) {
        return mySQLLegacyController.deleteAllLogsByJobId(jobId);
    }

    @Override
    public String getPersistenceName() {
        return MySQLLegacyConfig.NAME;
    }

    @Override
    public void connect(Object... params) {
        // TODO ?!
    }

}
