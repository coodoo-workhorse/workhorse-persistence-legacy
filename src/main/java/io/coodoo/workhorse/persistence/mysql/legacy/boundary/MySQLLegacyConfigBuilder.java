package io.coodoo.workhorse.persistence.mysql.legacy.boundary;

import io.coodoo.workhorse.core.entity.WorkhorseConfigBuilder;

/**
 * A class to build an object of type {@link MySQLLegacyConfig}
 * 
 * @author coodoo GmbH (coodoo.io)
 */
public class MySQLLegacyConfigBuilder extends WorkhorseConfigBuilder {

    private MySQLLegacyConfig config = new MySQLLegacyConfig();

    public MySQLLegacyConfigBuilder() {
        this.workhorseConfig = config;
    }

    @Override
    public MySQLLegacyConfig build() {
        return this.config;
    }

}
