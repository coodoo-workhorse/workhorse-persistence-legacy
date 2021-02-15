package io.coodoo.workhorse.persistence.mysql.legacy;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import io.coodoo.workhorse.core.entity.Job;
import io.coodoo.workhorse.core.entity.JobStatus;
import io.coodoo.workhorse.persistence.interfaces.JobPersistence;

/**
 * Legacy support for the MySQL Persistence of Workhorse version 1.5
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class MysqlLegacyJobPersistence implements JobPersistence {

    @Override
    public Job get(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Job getByName(String jobName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Job getByWorkerClassName(String jobClassName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Job> getAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Job> getAllByStatus(JobStatus jobStatus) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Job> getAllScheduled() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long count() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long countByStatus(JobStatus jobStatus) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void persist(Job job) {
        // TODO Auto-generated method stub

    }

    @Override
    public void update(Long id, Job job) {
        // TODO Auto-generated method stub

    }

    @Override
    public void connect(Object... params) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPersistenceName() {
        // TODO Auto-generated method stub
        return null;
    }

}
