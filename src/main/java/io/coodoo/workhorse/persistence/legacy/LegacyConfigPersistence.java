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

    private static LegacyPersistenceConfig legacyPersistenceConfig = new LegacyPersistenceConfig();

    @Inject
    LegacyController legacyController;

    @Override
    public String getPersistenceName() {
        return legacyPersistenceConfig.getPersistenceName();
    }

    @Override
    public String getPersistenceVersion() {
        return legacyPersistenceConfig.getPersistenceVersion();
    }

    @Override
    public void initialize(Object... params) {}

    @Override
    public WorkhorseConfig get() {
        LegacyConfig config = legacyController.getConfig();
        return LegacyConfig.map(config);
    }

    @Override
    public WorkhorseConfig update(WorkhorseConfig workhorseConfig) {

        LegacyConfig config = legacyController.updateConfig(workhorseConfig.getTimeZone(), workhorseConfig.getBufferPollInterval(),
                        workhorseConfig.getBufferMax(), workhorseConfig.getBufferMin(), workhorseConfig.getExecutionTimeout(),
                        workhorseConfig.getExecutionTimeoutStatus(), 0, 0, workhorseConfig.getLogChange(), workhorseConfig.getLogTimeFormat(),
                        workhorseConfig.getLogInfoMarker(), workhorseConfig.getLogWarnMarker(), workhorseConfig.getLogErrorMarker());

        return LegacyConfig.map(config);
    }

}
