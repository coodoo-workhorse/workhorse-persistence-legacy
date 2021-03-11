package io.coodoo.workhorse.persistence.mysql.legacy.boundary;

import io.coodoo.workhorse.core.entity.WorkhorseConfigBuilder;

/**
 * A class to build an object of type {@link MysqlLegacyConfig}
 * 
 * @author coodoo GmbH (coodoo.io)
 */
public class MysqlLegacyConfigBuilder extends WorkhorseConfigBuilder {

    private MysqlLegacyConfig config = new MysqlLegacyConfig();

    public MysqlLegacyConfigBuilder() {
        this.workhorseConfig = config;
    }

    @Override
    public MysqlLegacyConfig build() {
        return this.config;
    }

}
