package io.coodoo.workhorse.persistence.legacy;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.coodoo.workhorse.core.entity.WorkhorseConfig;
import io.coodoo.workhorse.persistence.interfaces.ConfigPersistence;
import io.coodoo.workhorse.persistence.legacy.boundary.LegacyPersistenceConfig;
import io.coodoo.workhorse.persistence.legacy.control.LegacyController;
import io.coodoo.workhorse.persistence.legacy.entity.LegacyConfig;

/**
 * Legacy support for the Persistence of Workhorse version 1.5.
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class LegacyConfigPersistence implements ConfigPersistence {

    @Inject
    LegacyController legacyController;

    @Override
    public WorkhorseConfig get() {
        LegacyConfig config = legacyController.getConfig();
        return mapConfig(config);
    }

    private WorkhorseConfig mapConfig(LegacyConfig config) {
        if (config == null) {
            return null;
        }
        WorkhorseConfig workhorseConfig = new LegacyPersistenceConfig();
        workhorseConfig.setTimeZone(config.getTimeZone());
        workhorseConfig.setBufferMax(config.getJobQueueMax());
        workhorseConfig.setBufferMin(config.getJobQueueMin());
        workhorseConfig.setBufferPollInterval(config.getJobQueuePollerInterval());
        workhorseConfig.setBufferPushFallbackPollInterval(config.getJobQueuePollerInterval());
        workhorseConfig.setMinutesUntilCleanup(LegacyPersistenceConfig.MINUTES_UNTIL_CLEANUP);
        workhorseConfig.setLogChange(config.getLogChange());
        workhorseConfig.setLogTimeFormat(config.getLogTimeFormatter());
        workhorseConfig.setLogInfoMarker(config.getLogInfoMarker());
        workhorseConfig.setLogWarnMarker(config.getLogWarnMarker());
        workhorseConfig.setLogErrorMarker(config.getLogErrorMarker());
        return workhorseConfig;
    }

    @Override
    public WorkhorseConfig update(WorkhorseConfig workhorseConfig) {

        LegacyConfig config = legacyController.updateConfig(workhorseConfig.getTimeZone(), workhorseConfig.getBufferPollInterval(),
                        workhorseConfig.getBufferMax(), workhorseConfig.getBufferMin(), workhorseConfig.getExecutionTimeout(),
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
        return LegacyPersistenceConfig.NAME;
    }

    @Override
    public String getPersistenceVersion() {
        return LegacyPersistenceConfig.VERSION + "-210325-2055-execution-summary";
    }

}
