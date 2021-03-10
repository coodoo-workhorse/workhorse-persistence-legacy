package io.coodoo.workhorse.persistence.mysql.legacy;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.coodoo.framework.listing.boundary.ListingParameters;
import io.coodoo.workhorse.core.entity.WorkhorseLog;
import io.coodoo.workhorse.persistence.interfaces.LogPersistence;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MySQLLegacyConfig;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MySQLLegacyService;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.Log;

/**
 * Legacy support for the MySQL Persistence of Workhorse version 1.5
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class MysqlLegacyLogPersistence implements LogPersistence {

    @Inject
    MySQLLegacyService mySQLLegacyService;

    @Override
    public WorkhorseLog get(Long logId) {
        return map(mySQLLegacyService.getLog(logId));
    }

    private WorkhorseLog map(Log log) {
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
    public WorkhorseLog update(Long logId, WorkhorseLog workhorseLog) {
        throw new RuntimeException("Dare you changing the log?!");
    }

    @Override
    public WorkhorseLog delete(Long logId) {
        return map(mySQLLegacyService.deleteLogsById(logId));
    }

    @Override
    public WorkhorseLog persist(WorkhorseLog workhorseLog) {
        return map(mySQLLegacyService.createLog(workhorseLog.getMessage(), workhorseLog.getJobId(), workhorseLog.getJobStatus(), workhorseLog.isByUser(),
                        workhorseLog.getChangeParameter(), workhorseLog.getChangeOld(), workhorseLog.getChangeNew(), workhorseLog.getStacktrace()));
    }

    @Override
    public List<WorkhorseLog> getAll(int limit) {
        return mySQLLegacyService.listLogs(new ListingParameters(limit)).getResults().stream().map(l -> map(l)).collect(Collectors.toList());
    }

    @Override
    public int deleteByJobId(Long jobId) {
        return mySQLLegacyService.deleteAllLogsByJobId(jobId);
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
