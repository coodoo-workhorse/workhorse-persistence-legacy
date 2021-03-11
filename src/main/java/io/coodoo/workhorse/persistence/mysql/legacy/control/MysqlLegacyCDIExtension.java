package io.coodoo.workhorse.persistence.mysql.legacy.control;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;

import io.coodoo.workhorse.persistence.mysql.legacy.MysqlLegacyConfigPersistence;
import io.coodoo.workhorse.persistence.mysql.legacy.MysqlLegacyExecutionPersistence;
import io.coodoo.workhorse.persistence.mysql.legacy.MysqlLegacyJobPersistence;
import io.coodoo.workhorse.persistence.mysql.legacy.MysqlLegacyLogPersistence;

/**
 * @author coodoo GmbH (coodoo.io)
 */
public class MysqlLegacyCDIExtension {

    public void register(@Observes BeforeBeanDiscovery bbdEvent) {
        bbdEvent.addAnnotatedType(MysqlLegacyConfigPersistence.class, MysqlLegacyConfigPersistence.class.getName());
        bbdEvent.addAnnotatedType(MysqlLegacyJobPersistence.class, MysqlLegacyJobPersistence.class.getName());
        bbdEvent.addAnnotatedType(MysqlLegacyExecutionPersistence.class, MysqlLegacyExecutionPersistence.class.getName());
        bbdEvent.addAnnotatedType(MysqlLegacyLogPersistence.class, MysqlLegacyLogPersistence.class.getName());
    }
}
