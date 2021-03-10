package io.coodoo.workhorse.persistence.mysql.legacy;

import java.time.LocalDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import io.coodoo.workhorse.core.entity.Execution;
import io.coodoo.workhorse.core.entity.ExecutionFailStatus;
import io.coodoo.workhorse.core.entity.ExecutionLog;
import io.coodoo.workhorse.core.entity.ExecutionStatus;
import io.coodoo.workhorse.persistence.interfaces.ExecutionPersistence;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MySQLLegacyConfig;

/**
 * Legacy support for the MySQL Persistence of Workhorse version 1.5
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class MysqlLegacyExecutionPersistence implements ExecutionPersistence {

    @Override
    public Execution getById(Long jobId, Long executionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Execution> getByJobId(Long jobId, Long limit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Execution> pollNextExecutions(Long jobId, Long limit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long count() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Execution persist(Execution execution) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(Long jobId, Long executionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public Execution update(Execution execution) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Execution updateStatus(Long jobId, Long executionId, ExecutionStatus status, ExecutionFailStatus failStatus) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int deleteOlderExecutions(Long jobId, LocalDateTime preDate) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Execution> getBatch(Long jobId, Long batchId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Execution> getChain(Long jobId, Long chainId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Execution getQueuedBatchExecution(Long jobId, Long batchId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Execution> getFailedBatchExecutions(Long jobId, Long batchId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Execution getFirstCreatedByJobIdAndParametersHash(Long jobId, Integer parameterHash) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isBatchFinished(Long jobId, Long batchId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean abortChain(Long jobId, Long chainId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Execution> findTimeoutExecutions(LocalDateTime time) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExecutionLog getLog(Long jobId, Long executionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void log(Long jobId, Long executionId, String log) {
        // TODO Auto-generated method stub

    }

    @Override
    public void log(Long jobId, Long executionId, String error, String stacktrace) {
        // TODO Auto-generated method stub

    }

    @Override
    public void connect(Object... params) {
        // TODO ?!
    }

    @Override
    public String getPersistenceName() {
        return MySQLLegacyConfig.NAME;
    }

    @Override
    public boolean isPusherAvailable() {
        return false;
    }

}
