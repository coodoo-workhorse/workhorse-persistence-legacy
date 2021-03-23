package io.coodoo.workhorse.persistence.legacy;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.coodoo.framework.listing.boundary.ListingParameters;
import io.coodoo.framework.listing.boundary.ListingResult;
import io.coodoo.workhorse.core.entity.Job;
import io.coodoo.workhorse.core.entity.JobStatus;
import io.coodoo.workhorse.persistence.interfaces.JobPersistence;
import io.coodoo.workhorse.persistence.legacy.boundary.LegacyPersistenceConfig;
import io.coodoo.workhorse.persistence.legacy.control.LegacyController;
import io.coodoo.workhorse.persistence.legacy.entity.LegacyJob;

/**
 * Legacy support for the Persistence of Workhorse version 1.5
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class LegacyJobPersistence implements JobPersistence {

    @Inject
    LegacyController legacyController;

    @Override
    public Job get(Long jobId) {
        return map(legacyController.getJobById(jobId));
    }

    private Job map(LegacyJob legacyJob) {
        if (legacyJob == null) {
            return null;
        }
        Job job = new Job();
        job.setId(legacyJob.getId());
        job.setName(legacyJob.getName());
        job.setDescription(legacyJob.getDescription());
        job.setWorkerClassName(legacyJob.getWorkerClassName());
        job.setParametersClassName(legacyJob.getParametersClassName());
        job.setStatus(legacyJob.getStatus());
        job.setThreads(legacyJob.getThreads());
        job.setMaxPerMinute(legacyJob.getMaxPerMinute());
        job.setFailRetries(legacyJob.getFailRetries());
        job.setRetryDelay(legacyJob.getRetryDelay());
        job.setMinutesUntilCleanUp(legacyJob.getDaysUntilCleanUp() / 24 / 60);
        job.setUniqueQueued(legacyJob.isUniqueInQueue());
        job.setSchedule(legacyJob.getSchedule());
        job.setCreatedAt(legacyJob.getCreatedAt());
        job.setUpdatedAt(legacyJob.getUpdatedAt());
        return job;
    }

    @Override
    public io.coodoo.workhorse.persistence.interfaces.listing.ListingResult<Job> getJobListing(
                    io.coodoo.workhorse.persistence.interfaces.listing.ListingParameters listingParameters) {

        ListingParameters params = new ListingParameters(listingParameters.getPage(), listingParameters.getLimit(), listingParameters.getSortAttribute());
        params.setFilterAttributes(listingParameters.getFilterAttributes());
        params.setFilter(listingParameters.getFilter());

        ListingResult<LegacyJob> result = legacyController.listJobs(params);

        List<Job> results = result.getResults().stream().map(l -> map(l)).collect(Collectors.toList());

        io.coodoo.workhorse.persistence.interfaces.listing.Metadata metadata =
                        new io.coodoo.workhorse.persistence.interfaces.listing.Metadata(result.getMetadata().getCount(), listingParameters);

        return new io.coodoo.workhorse.persistence.interfaces.listing.ListingResult<Job>(results, metadata);
    }

    @Override
    public Job getByName(String jobName) {
        return map(legacyController.getJobByClassName(jobName));
    }

    @Override
    public Job getByWorkerClassName(String jobClassName) {
        return map(legacyController.getJobByClassName(jobClassName));
    }

    @Override
    public List<Job> getAll() {
        return legacyController.getAllJobs().stream().map(j -> map(j)).collect(Collectors.toList());
    }

    @Override
    public List<Job> getAllByStatus(JobStatus jobStatus) {
        return legacyController.getAllByStatus(jobStatus).stream().map(j -> map(j)).collect(Collectors.toList());
    }

    @Override
    public List<Job> getAllScheduled() {
        return legacyController.getAllScheduledJobs().stream().map(j -> map(j)).collect(Collectors.toList());
    }

    @Override
    public Long count() {
        return legacyController.countAllJobs();
    }

    @Override
    public Long countByStatus(JobStatus jobStatus) {
        return legacyController.countJobsByStatus(jobStatus);
    }

    @Override
    public Job persist(Job job) {
        int daysuntilCleanup = job.getMinutesUntilCleanUp() * 24 * 60;
        LegacyJob createJob = legacyController.createJob(job.getName(), job.getDescription(), job.getTags(), job.getWorkerClassName(),
                        job.getParametersClassName(), job.getSchedule(), job.getStatus(), job.getThreads(), job.getMaxPerMinute(), job.getFailRetries(),
                        job.getRetryDelay(), daysuntilCleanup, job.isUniqueQueued());
        return map(createJob);
    }

    @Override
    public Job update(Job job) {
        int daysuntilCleanup = job.getMinutesUntilCleanUp() * 24 * 60;
        LegacyJob createJob = legacyController.updateJob(job.getId(), job.getName(), job.getDescription(), job.getTags(), job.getWorkerClassName(),
                        job.getSchedule(), job.getStatus(), job.getThreads(), job.getMaxPerMinute(), job.getFailRetries(), job.getRetryDelay(),
                        daysuntilCleanup, job.isUniqueQueued());
        return map(createJob);
    }

    @Override
    public void deleteJob(Long jobId) {
        legacyController.deleteJob(jobId);
    }

    @Override
    public void connect(Object... params) {
        // TODO ?!
    }

    @Override
    public String getPersistenceName() {
        return LegacyPersistenceConfig.NAME;
    }

}
