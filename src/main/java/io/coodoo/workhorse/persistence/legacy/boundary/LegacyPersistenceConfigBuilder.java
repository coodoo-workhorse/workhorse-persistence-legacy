package io.coodoo.workhorse.persistence.legacy.boundary;

import io.coodoo.workhorse.core.entity.WorkhorseConfigBuilder;

/**
 * A class to build an object of type {@link LegacyPersistenceConfig}
 * 
 * @author coodoo GmbH (coodoo.io)
 */
public class LegacyPersistenceConfigBuilder extends WorkhorseConfigBuilder {

    private LegacyPersistenceConfig config = new LegacyPersistenceConfig();

    public LegacyPersistenceConfigBuilder() {
        this.workhorseConfig = config;
    }

    @Override
    public LegacyPersistenceConfig build() {
        return this.config;
    }

}
