package io.coodoo.workhorse.persistence.mysql.legacy;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.coodoo.workhorse.core.entity.WorkhorseConfig;
import io.coodoo.workhorse.persistence.interfaces.ConfigPersistence;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MysqlLegacyConfig;
import io.coodoo.workhorse.persistence.mysql.legacy.control.MysqlLegacyController;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.LegacyConfig;

/**
 * Legacy support for the MySQL Persistence of Workhorse version 1.5.
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class MysqlLegacyConfigPersistence implements ConfigPersistence {

    @Inject
    MysqlLegacyController mysqlLegacyController;

    @Override
    public WorkhorseConfig get() {
        LegacyConfig config = mysqlLegacyController.getConfig();
        return mapConfig(config);
    }

    private WorkhorseConfig mapConfig(LegacyConfig config) {
        if (config == null) {
            return null;
        }
        WorkhorseConfig workhorseConfig = new MysqlLegacyConfig();
        workhorseConfig.setTimeZone(config.getTimeZone());
        workhorseConfig.setBufferMax(config.getJobQueueMax());
        workhorseConfig.setBufferMin(config.getJobQueueMin());
        workhorseConfig.setBufferPollInterval(config.getJobQueuePollerInterval());
        workhorseConfig.setBufferPushFallbackPollInterval(config.getJobQueuePollerInterval());
        workhorseConfig.setMinutesUntilCleanup(MysqlLegacyConfig.MINUTES_UNTIL_CLEANUP);
        workhorseConfig.setLogChange(config.getLogChange());
        workhorseConfig.setLogTimeFormat(config.getLogTimeFormatter());
        workhorseConfig.setLogInfoMarker(config.getLogInfoMarker());
        workhorseConfig.setLogWarnMarker(config.getLogWarnMarker());
        workhorseConfig.setLogErrorMarker(config.getLogErrorMarker());
        return workhorseConfig;
    }

    @Override
    public WorkhorseConfig update(WorkhorseConfig workhorseConfig) {

        LegacyConfig config = mysqlLegacyController.updateConfig(workhorseConfig.getTimeZone(),
                workhorseConfig.getBufferPollInterval(), workhorseConfig.getBufferMax(), workhorseConfig.getBufferMin(),
                workhorseConfig.getExecutionTimeout(), workhorseConfig.getExecutionTimeoutStatus(), 0, 0,
                workhorseConfig.getLogChange(), workhorseConfig.getLogTimeFormat(), workhorseConfig.getLogInfoMarker(),
                workhorseConfig.getLogWarnMarker(), workhorseConfig.getLogErrorMarker());

        return mapConfig(config);
    }

    @Override
    public void connect(Object... params) {
        // TODO ?!
    }

    @Override
    public String getPersistenceName() {
        return MysqlLegacyConfig.NAME;
    }

    @Override
    public String getPersistenceVersion() {
        return "MySQL_Persistence_22032020_1130";
    }

}
