package io.coodoo.workhorse.persistence.mysql.legacy;

import javax.enterprise.context.ApplicationScoped;

import io.coodoo.workhorse.core.entity.WorkhorseConfig;
import io.coodoo.workhorse.persistence.interfaces.ConfigPersistence;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MySQLLegacyConfig;

/**
 * Legacy support for the MySQL Persistence of Workhorse version 1.5
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class MysqlLegacyConfigPersistence implements ConfigPersistence {

    @Override
    public WorkhorseConfig get() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkhorseConfig update(WorkhorseConfig workhorseConfig) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void connect(Object... params) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPersistenceName() {
        return MySQLLegacyConfig.NAME;
    }

}
