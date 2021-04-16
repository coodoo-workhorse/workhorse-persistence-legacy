package io.coodoo.workhorse.persistence.legacy.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.batch.runtime.JobExecution;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.coodoo.framework.listing.boundary.Listing;
import io.coodoo.framework.listing.boundary.ListingParameters;
import io.coodoo.framework.listing.boundary.Term;
import io.coodoo.framework.listing.control.ListingConfig;
import io.coodoo.workhorse.core.entity.ExecutionStatus;

public class JobExecutionCounts {

    private final static Logger logger = LoggerFactory.getLogger(JobExecutionCounts.class);

    private Long total = 0L;
    private Long planned = 0L;
    private Long queued = 0L;
    private Long running = 0L;
    private Long finished = 0L;
    private Long failed = 0L;
    private Long aborted = 0L;

    /**
     * Queries the current counts of {@link JobExecution} status for one specific or all jobs
     * 
     * @param entityManager persistence
     * @param jobId optional, <code>null</code> will get the counts for all
     * @param from only executions that were created after this time stamp are considered
     * @param to only executions that were created before this time stamp are considered
     * @return fresh counts!
     */
    public static JobExecutionCounts query(EntityManager entityManager, Long jobId, LocalDateTime from, LocalDateTime to) {

        ListingParameters listingParameters = new ListingParameters();
        if (jobId != null) {
            listingParameters.addFilterAttributes("jobId", jobId.toString());
        }

        // The ZoneId to use in this case is UTC. So the method toEpochMilli() don't
        // have to convert the given time in the used TimeZone.
        // InstInstead, if the local time zone is used, the convertion will be proceed.
        // But the listing framework has to make the convertion itself.
        // Something that do not happen. It is the reason, why no convertion in local
        // time zone have to be proceeded here.

        long fromMillis = from.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
        long toMillis = to.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();

        String timeFilter = fromMillis + ListingConfig.OPERATOR_TO + toMillis;

        listingParameters.addFilterAttributes("createdAt", timeFilter);
        listingParameters.addTermsAttributes("status", "6"); // there are only six status

        JobExecutionCounts counts = new JobExecutionCounts();
        for (Term term : Listing.getTerms(entityManager, LegacyExecution.class, listingParameters).get("status")) {
            switch ((ExecutionStatus) term.getValue()) {
                case PLANNED:
                    counts.setPlanned(term.getCount());
                    break;
                case QUEUED:
                    counts.setQueued(term.getCount());
                    break;
                case RUNNING:
                    counts.setRunning(term.getCount());
                    break;
                case FINISHED:
                    counts.setFinished(term.getCount());
                    break;
                case FAILED:
                    counts.setFailed(term.getCount());
                    break;
                case ABORTED:
                    counts.setAborted(term.getCount());
                    break;
            }
        }

        return counts;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getPlanned() {
        return planned;
    }

    public void setPlanned(Long planned) {
        this.planned = planned;
    }

    public Long getQueued() {
        return queued;
    }

    public void setQueued(Long queued) {
        this.queued = queued;
    }

    public Long getRunning() {
        return running;
    }

    public void setRunning(Long running) {
        this.running = running;
    }

    public Long getFinished() {
        return finished;
    }

    public void setFinished(Long finished) {
        this.finished = finished;
    }

    public Long getFailed() {
        return failed;
    }

    public void setFailed(Long failed) {
        this.failed = failed;
    }

    public Long getAborted() {
        return aborted;
    }

    public void setAborted(Long aborted) {
        this.aborted = aborted;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("JobExecutionCounts [total=");
        builder.append(total);
        builder.append(", planned=");
        builder.append(planned);
        builder.append(", queued=");
        builder.append(queued);
        builder.append(", running=");
        builder.append(running);
        builder.append(", finished=");
        builder.append(finished);
        builder.append(", failed=");
        builder.append(failed);
        builder.append(", aborted=");
        builder.append(aborted);
        builder.append("]");
        return builder.toString();
    }

}
