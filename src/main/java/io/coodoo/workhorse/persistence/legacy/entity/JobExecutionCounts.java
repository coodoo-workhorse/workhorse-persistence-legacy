package io.coodoo.workhorse.persistence.legacy.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.batch.runtime.JobExecution;
import javax.persistence.EntityManager;

import io.coodoo.framework.listing.boundary.Listing;
import io.coodoo.framework.listing.boundary.ListingParameters;
import io.coodoo.framework.listing.boundary.Stats;
import io.coodoo.framework.listing.boundary.Term;
import io.coodoo.framework.listing.control.ListingConfig;
import io.coodoo.workhorse.core.control.StaticConfig;
import io.coodoo.workhorse.core.entity.ExecutionStatus;

public class JobExecutionCounts {

    private Long total = 0L;
    private Long planned = 0L;
    private Long queued = 0L;
    private Long running = 0L;
    private Long finished = 0L;
    private Long failed = 0L;
    private Long aborted = 0L;
    private Long averageDuration;
    private Long minDuration;
    private Long maxDuration;
    private Long sumDuration;

    /**
     * Queries the current counts of {@link JobExecution} status for one specific or all jobs
     * 
     * @param entityManager persistence
     * @param jobId optional, <code>null</code> will get the counts for all
     * @param from only executions that were created after this timestamp are considered
     * @param to only executions that were created before this timestamp are considered
     * @return fresh counts!
     */
    public static JobExecutionCounts query(EntityManager entityManager, Long jobId, LocalDateTime from, LocalDateTime to) {

        long fromMillis = from.atZone(ZoneId.of(StaticConfig.TIME_ZONE)).toInstant().toEpochMilli();
        long toMillis = to.atZone(ZoneId.of(StaticConfig.TIME_ZONE)).toInstant().toEpochMilli();

        String timeFilter = fromMillis + ListingConfig.OPERATOR_TO + toMillis;

        ListingParameters listingParameters = new ListingParameters();
        if (jobId != null) {
            listingParameters.addFilterAttributes("jobId", jobId.toString());
        }

        listingParameters.addFilterAttributes("createdAt", timeFilter);
        listingParameters.addTermsAttributes("status", "6"); // there are only five status
        listingParameters.addStatsAttributes("duration", "all");

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
        Stats stats = Listing.getStats(entityManager, LegacyExecution.class, listingParameters).get("duration");
        counts.setTotal(stats.getCount());
        if (stats.getAvg() != null) {
            counts.setAverageDuration(stats.getAvg().longValue());
        }
        if (stats.getMin() != null) {
            counts.setMinDuration(stats.getMin().longValue());
        }
        if (stats.getMax() != null) {
            counts.setMaxDuration(stats.getMax().longValue());
        }
        if (stats.getSum() != null) {
            counts.setSumDuration(stats.getSum().longValue());
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

    public Long getAverageDuration() {
        return averageDuration;
    }

    public void setAverageDuration(Long averageDuration) {
        this.averageDuration = averageDuration;
    }

    public Long getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(Long minDuration) {
        this.minDuration = minDuration;
    }

    public Long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(Long maxDuration) {
        this.maxDuration = maxDuration;
    }

    public Long getSumDuration() {
        return sumDuration;
    }

    public void setSumDuration(Long sumDuration) {
        this.sumDuration = sumDuration;
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
        builder.append(", averageDuration=");
        builder.append(averageDuration);
        builder.append(", minDuration=");
        builder.append(minDuration);
        builder.append(", maxDuration=");
        builder.append(maxDuration);
        builder.append(", sumDuration=");
        builder.append(sumDuration);
        builder.append("]");
        return builder.toString();
    }

}
