package io.coodoo.workhorse.persistence.mysql.legacy;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.coodoo.workhorse.core.entity.Job;
import io.coodoo.workhorse.core.entity.JobStatus;
import io.coodoo.workhorse.persistence.interfaces.JobPersistence;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MySQLLegacyConfig;
import io.coodoo.workhorse.persistence.mysql.legacy.boundary.MySQLLegacyService;
import io.coodoo.workhorse.persistence.mysql.legacy.entity.DbJob;

/**
 * Legacy support for the MySQL Persistence of Workhorse version 1.5
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class MysqlLegacyJobPersistence implements JobPersistence {

    @Inject
    MySQLLegacyService mySQLLegacyService;

    @Override
    public Job get(Long jobId) {
        return mapJob(mySQLLegacyService.getJobById(jobId));
    }

    private Job mapJob(DbJob dbJob) {
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
        return mapJob(mySQLLegacyService.getJobByClassName(jobName));
    }

    @Override
    public Job getByWorkerClassName(String jobClassName) {
        return mapJob(mySQLLegacyService.getJobByClassName(jobClassName));
    }

    @Override
    public List<Job> getAll() {
        return mySQLLegacyService.getAllJobs().stream().map(j -> mapJob(j)).collect(Collectors.toList());
    }

    @Override
    public List<Job> getAllByStatus(JobStatus jobStatus) {
        return mySQLLegacyService.getAllByStatus(jobStatus).stream().map(j -> mapJob(j)).collect(Collectors.toList());
    }

    @Override
    public List<Job> getAllScheduled() {
        return mySQLLegacyService.getAllScheduledJobs().stream().map(j -> mapJob(j)).collect(Collectors.toList());
    }

    @Override
    public Long count() {
        return mySQLLegacyService.countAllJobs();
    }

    @Override
    public Long countByStatus(JobStatus jobStatus) {
        return mySQLLegacyService.countJobsByStatus(jobStatus);
    }

    @Override
    public Job persist(Job job) {
        int daysuntilCleanup = job.getMinutesUntilCleanUp() * 24 * 60;
        DbJob createJob = mySQLLegacyService.createJob(job.getName(), job.getDescription(), job.getTags(), job.getWorkerClassName(),
                        job.getParametersClassName(), job.getSchedule(), job.getStatus(), job.getThreads(), job.getMaxPerMinute(), job.getFailRetries(),
                        job.getRetryDelay(), daysuntilCleanup, job.isUniqueQueued());
        return mapJob(createJob);
    }

    @Override
    public Job update(Job job) {
        int daysuntilCleanup = job.getMinutesUntilCleanUp() * 24 * 60;
        DbJob createJob = mySQLLegacyService.updateJob(job.getId(), job.getName(), job.getDescription(), job.getTags(), job.getWorkerClassName(),
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
