package io.coodoo.workhorse.persistence.mysql.legacy;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.coodoo.workhorse.core.entity.WorkhorseConfig;
import io.coodoo.workhorse.persistence.interfaces.ConfigPersistence;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MySQLLegacyConfig;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MySQLLegacyService;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.Config;

/**
 * Legacy support for the MySQL Persistence of Workhorse version 1.5
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class MysqlLegacyConfigPersistence implements ConfigPersistence {

    @Inject
    MySQLLegacyService mySQLLegacyService;

    @Override
    public WorkhorseConfig get() {
        Config config = mySQLLegacyService.getConfig();
        return mapConfig(config);
    }

    private WorkhorseConfig mapConfig(Config config) {
        WorkhorseConfig workhorseConfig = new MySQLLegacyConfig();
        workhorseConfig.setTimeZone(config.getTimeZone());
        workhorseConfig.setBufferMax(new Long(config.getJobQueueMax()));
        workhorseConfig.setBufferMin(config.getJobQueueMin());
        workhorseConfig.setBufferPollInterval(config.getJobQueuePollerInterval());
        workhorseConfig.setBufferPushFallbackPollInterval(config.getJobQueuePollerInterval());
        workhorseConfig.setMinutesUntilCleanup(MySQLLegacyConfig.MINUTES_UNTIL_CLEANUP);
        workhorseConfig.setLogChange(config.getLogChange());
        workhorseConfig.setLogTimeFormat(config.getLogTimeFormatter());
        workhorseConfig.setLogInfoMarker(config.getLogInfoMarker());
        workhorseConfig.setLogWarnMarker(config.getLogWarnMarker());
        workhorseConfig.setLogErrorMarker(config.getLogErrorMarker());
        return workhorseConfig;
    }

    @Override
    public WorkhorseConfig update(WorkhorseConfig workhorseConfig) {

        if (workhorseConfig.getMinutesUntilCleanup() != MySQLLegacyConfig.MINUTES_UNTIL_CLEANUP) {
            throw new RuntimeException("The " + MySQLLegacyConfig.NAME + " persistence can not change the minutes until cleanup default value of 30 days ("
                            + MySQLLegacyConfig.MINUTES_UNTIL_CLEANUP + " minutes)");
        }

        Config config = mySQLLegacyService.updateConfig(workhorseConfig.getTimeZone(), workhorseConfig.getBufferPollInterval(),
                        workhorseConfig.getBufferMax().intValue(), workhorseConfig.getBufferMin(), workhorseConfig.getExecutionTimeout(),
                        workhorseConfig.getExecutionTimeoutStatus(), 0, 0, workhorseConfig.getLogChange(), workhorseConfig.getLogTimeFormat(),
                        workhorseConfig.getLogInfoMarker(), workhorseConfig.getLogWarnMarker(), workhorseConfig.getLogErrorMarker());

        return mapConfig(config);
    }

    @Override
    public void connect(Object... params) {
        // TODO ?!
    }

    @Override
    public String getPersistenceName() {
        return MySQLLegacyConfig.NAME;
    }

}
