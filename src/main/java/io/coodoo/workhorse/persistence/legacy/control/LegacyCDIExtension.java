package io.coodoo.workhorse.persistence.legacy.control;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;

import io.coodoo.workhorse.persistence.legacy.LegacyConfigPersistence;
import io.coodoo.workhorse.persistence.legacy.LegacyExecutionPersistence;
import io.coodoo.workhorse.persistence.legacy.LegacyJobPersistence;
import io.coodoo.workhorse.persistence.legacy.LegacyLogPersistence;

/**
 * @author coodoo GmbH (coodoo.io)
 */
public class LegacyCDIExtension {

    public void register(@Observes BeforeBeanDiscovery bbdEvent) {
        bbdEvent.addAnnotatedType(LegacyConfigPersistence.class, LegacyConfigPersistence.class.getName());
        bbdEvent.addAnnotatedType(LegacyJobPersistence.class, LegacyJobPersistence.class.getName());
        bbdEvent.addAnnotatedType(LegacyExecutionPersistence.class, LegacyExecutionPersistence.class.getName());
        bbdEvent.addAnnotatedType(LegacyLogPersistence.class, LegacyLogPersistence.class.getName());
    }
}
