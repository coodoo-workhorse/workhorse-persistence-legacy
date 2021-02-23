package io.coodoo.workhorse.persistence.mysql.legacy.control;

import java.time.LocalDateTime;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import io.coodoo.workhorse.core.control.BaseWorker;
import io.coodoo.workhorse.core.control.JobScheduler;
import io.coodoo.workhorse.core.control.Workhorse;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MySQLLegacyService;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.annotation.JobEngineEntityManager;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.Job;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.JobExecution;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.JobExecutionStatus;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.JobStatus;
import io.coodoo.workhorse.util.WorkhorseUtil;

/**
 * @author coodoo GmbH (coodoo.io)
 */
@Stateless
public class MySQLLegacyController {

    @Inject
    @JobEngineEntityManager
    EntityManager entityManager;

    @Inject
    Workhorse workhorse;

    @Inject
    JobScheduler jobScheduler;

    @Inject
    MySQLLegacyService jobEngineService;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public int deleteOlderJobExecutions(Long jobId, int minDaysOld) {
        return JobExecution.deleteOlderJobExecutions(entityManager, jobId, LocalDateTime.now().minusDays(minDaysOld));
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public synchronized JobExecution handleFailedExecution(Job job, Long jobExecutionId, Exception exception, Long duration, String jobExecutionLog,
                    BaseWorker jobWorker) {

        JobExecution failedExecution = entityManager.find(JobExecution.class, jobExecutionId);
        JobExecution retryExecution = null;

        if (failedExecution.getFailRetry() < job.getFailRetries()) {
            // retry
            retryExecution = createRetryExecution(failedExecution);
        } else if (failedExecution.getChainId() != null) {
            JobExecution.abortChain(entityManager, failedExecution.getChainId());
        }

        failedExecution.setStatus(JobExecutionStatus.FAILED);
        failedExecution.setEndedAt(WorkhorseUtil.timestamp());
        failedExecution.setDuration(duration);
        failedExecution.setLog(jobExecutionLog);
        failedExecution.setFailMessage(WorkhorseUtil.getMessagesFromException(exception));
        failedExecution.setFailStacktrace(WorkhorseUtil.stacktraceToString(exception));

        if (retryExecution == null) {
            jobWorker.onFailed(jobExecutionId);
            if (failedExecution.getChainId() != null) {
                jobWorker.onFailedChain(failedExecution.getChainId(), jobExecutionId);
            }
        } else {
            jobWorker.onRetry(jobExecutionId, retryExecution.getId());
        }
        return retryExecution;
    }

    private JobExecution createRetryExecution(JobExecution failedExecution) {

        // create a new execution to retry the work of the failed one
        JobExecution retryExecution = new JobExecution();
        retryExecution.setJobId(failedExecution.getJobId());
        retryExecution.setStatus(failedExecution.getStatus());
        retryExecution.setStartedAt(WorkhorseUtil.timestamp());
        retryExecution.setPriority(failedExecution.isPriority());
        retryExecution.setMaturity(failedExecution.getMaturity());
        retryExecution.setChainId(failedExecution.getChainId());
        retryExecution.setChainPreviousExecutionId(failedExecution.getChainPreviousExecutionId());
        retryExecution.setParameters(failedExecution.getParameters());
        retryExecution.setParametersHash(failedExecution.getParametersHash());

        // increase failure number
        retryExecution.setFailRetry(failedExecution.getFailRetry() + 1);
        if (retryExecution.getFailRetryExecutionId() == null) {
            retryExecution.setFailRetryExecutionId(failedExecution.getId());
        }

        entityManager.persist(retryExecution);
        return retryExecution;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public synchronized void setJobExecutionRunning(Long jobExecutionId) {
        JobExecution.updateStatusRunning(entityManager, WorkhorseUtil.timestamp(), jobExecutionId);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public synchronized void setJobExecutionFinished(Job job, Long jobExecutionId, Long duration, String jobExecutionLog) {
        JobExecution.updateStatusFinished(entityManager, WorkhorseUtil.timestamp(), duration, jobExecutionLog, jobExecutionId);
    }

    public synchronized JobExecution getNextInChain(Long chainId, Long currentJobExecutionId) {
        return JobExecution.getNextInChain(entityManager, chainId, currentJobExecutionId);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void setJobStatus(Long jobId, JobStatus status) {

        // TODO nicht über JPA lösen - Optimistic Lock bei Status aus mehreren Threads möglich - oder darauf reagieren.
        final Job job = jobEngineService.getJobById(jobId);
        if (job != null) {
            job.setStatus(status);
        }
    }
}
