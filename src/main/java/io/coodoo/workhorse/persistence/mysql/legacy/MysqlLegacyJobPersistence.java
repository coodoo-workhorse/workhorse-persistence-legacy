package io.coodoo.workhorse.persistence.mysql.legacy;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.coodoo.workhorse.core.entity.Job;
import io.coodoo.workhorse.core.entity.JobStatus;
import io.coodoo.workhorse.persistence.interfaces.JobPersistence;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MySQLLegacyConfig;
import io.coodoo.workhorse.persistence.mysql.legacy.control.MySQLLegacyController;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.LegacyJob;

/**
 * Legacy support for the MySQL Persistence of Workhorse version 1.5
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class MysqlLegacyJobPersistence implements JobPersistence {

    @Inject
    MySQLLegacyController mySQLLegacyController;

    @Override
    public Job get(Long jobId) {
        return mapJob(mySQLLegacyController.getJobById(jobId));
    }

    private Job mapJob(LegacyJob dbJob) {
        Job job = new Job();
        job.setId(dbJob.getId());
        job.setName(dbJob.getName());
        job.setDescription(dbJob.getDescription());
        job.setWorkerClassName(dbJob.getWorkerClassName());
        job.setParametersClassName(dbJob.getParametersClassName());
        job.setStatus(dbJob.getStatus());
        job.setThreads(dbJob.getThreads());
        job.setMaxPerMinute(dbJob.getMaxPerMinute());
        job.setFailRetries(dbJob.getFailRetries());
        job.setRetryDelay(dbJob.getRetryDelay());
        job.setMinutesUntilCleanUp(dbJob.getDaysUntilCleanUp() / 24 / 60);
        job.setUniqueQueued(dbJob.isUniqueInQueue());
        job.setSchedule(dbJob.getSchedule());
        job.setCreatedAt(dbJob.getCreatedAt());
        job.setUpdatedAt(dbJob.getUpdatedAt());
        return job;
    }

    @Override
    public Job getByName(String jobName) {
        return mapJob(mySQLLegacyController.getJobByClassName(jobName));
    }

    @Override
    public Job getByWorkerClassName(String jobClassName) {
        return mapJob(mySQLLegacyController.getJobByClassName(jobClassName));
    }

    @Override
    public List<Job> getAll() {
        return mySQLLegacyController.getAllJobs().stream().map(j -> mapJob(j)).collect(Collectors.toList());
    }

    @Override
    public List<Job> getAllByStatus(JobStatus jobStatus) {
        return mySQLLegacyController.getAllByStatus(jobStatus).stream().map(j -> mapJob(j)).collect(Collectors.toList());
    }

    @Override
    public List<Job> getAllScheduled() {
        return mySQLLegacyController.getAllScheduledJobs().stream().map(j -> mapJob(j)).collect(Collectors.toList());
    }

    @Override
    public Long count() {
        return mySQLLegacyController.countAllJobs();
    }

    @Override
    public Long countByStatus(JobStatus jobStatus) {
        return mySQLLegacyController.countJobsByStatus(jobStatus);
    }

    @Override
    public Job persist(Job job) {
        int daysuntilCleanup = job.getMinutesUntilCleanUp() * 24 * 60;
        LegacyJob createJob = mySQLLegacyController.createJob(job.getName(), job.getDescription(), job.getTags(), job.getWorkerClassName(),
                        job.getParametersClassName(), job.getSchedule(), job.getStatus(), job.getThreads(), job.getMaxPerMinute(), job.getFailRetries(),
                        job.getRetryDelay(), daysuntilCleanup, job.isUniqueQueued());
        return mapJob(createJob);
    }

    @Override
    public Job update(Job job) {
        int daysuntilCleanup = job.getMinutesUntilCleanUp() * 24 * 60;
        LegacyJob createJob = mySQLLegacyController.updateJob(job.getId(), job.getName(), job.getDescription(), job.getTags(), job.getWorkerClassName(),
                        job.getSchedule(), job.getStatus(), job.getThreads(), job.getMaxPerMinute(), job.getFailRetries(), job.getRetryDelay(),
                        daysuntilCleanup, job.isUniqueQueued());
        return mapJob(createJob);
    }

    @Override
    public void connect(Object... params) {
        // TODO ?!
    }

    @Override
    public String getPersistenceName() {
        return MySQLLegacyConfig.NAME;
    }

}
