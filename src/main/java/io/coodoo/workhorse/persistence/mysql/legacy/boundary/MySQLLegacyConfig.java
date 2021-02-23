package io.coodoo.workhorse.persistence.mysql.legacy.boundary;

import io.coodoo.workhorse.core.entity.WorkhorseConfig;

/**
 * @author coodoo GmbH (coodoo.io)
 */
public class MySQLLegacyConfig extends WorkhorseConfig {

    public static final String NAME = "MySQLLegacy";

    @Override
    public String getPersistenceName() {
        return NAME;
    }
}
