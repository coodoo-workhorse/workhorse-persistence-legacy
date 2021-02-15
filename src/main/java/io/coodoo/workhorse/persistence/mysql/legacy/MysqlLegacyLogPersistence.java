package io.coodoo.workhorse.persistence.mysql.legacy;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import io.coodoo.workhorse.core.entity.WorkhorseLog;
import io.coodoo.workhorse.persistence.interfaces.LogPersistence;

/**
 * Legacy support for the MySQL Persistence of Workhorse version 1.5
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class MysqlLegacyLogPersistence implements LogPersistence {

    @Override
    public WorkhorseLog get(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkhorseLog update(Long id, WorkhorseLog workhorseLog) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkhorseLog delete(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkhorseLog persist(WorkhorseLog workhorseLog) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<WorkhorseLog> getAll(int limit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int deleteByJobId(Long jobId) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getPersistenceName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void connect(Object... params) {
        // TODO Auto-generated method stub

    }

}
