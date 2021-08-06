package io.coodoo.workhorse.persistence.legacy.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import io.coodoo.framework.jpa.entity.AbstractIdCreatedUpdatedAtEntity;
import io.coodoo.workhorse.core.entity.Execution;
import io.coodoo.workhorse.core.entity.ExecutionFailStatus;
import io.coodoo.workhorse.core.entity.ExecutionStatus;

/**
 * The JobExceuctionView gets Executions without the heavy payload of the log and stacktrace.
 * 
 * @author coodoo GmbH (coodoo.io)
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "jobengine_execution_view")
public class LegacyExecutionView extends AbstractIdCreatedUpdatedAtEntity {

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "priority")
    private boolean priority;

    @Column(name = "maturity")
    private LocalDateTime maturity;

    @Column(name = "summary")
    private String summary;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "chain_id")
    private Long chainId;

    @Column(name = "chain_previous_execution_id")
    private Long chainPreviousExecutionId;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "parameters")
    private String parameters;

    @Column(name = "fail_retry")
    private int failRetry;

    @Column(name = "fail_retry_execution_id")
    private Long failRetryExecutionId;

    @Column(name = "fail_message")
    private String failMessage;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public LocalDateTime getMaturity() {
        return maturity;
    }

    public void setMaturity(LocalDateTime maturity) {
        this.maturity = maturity;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getChainId() {
        return chainId;
    }

    public void setChainId(Long chainId) {
        this.chainId = chainId;
    }

    public Long getChainPreviousExecutionId() {
        return chainPreviousExecutionId;
    }

    public void setChainPreviousExecutionId(Long chainPreviousExecutionId) {
        this.chainPreviousExecutionId = chainPreviousExecutionId;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public int getFailRetry() {
        return failRetry;
    }

    public void setFailRetry(int failRetry) {
        this.failRetry = failRetry;
    }

    public Long getFailRetryExecutionId() {
        return failRetryExecutionId;
    }

    public void setFailRetryExecutionId(Long failRetryExecutionId) {
        this.failRetryExecutionId = failRetryExecutionId;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LegacyExecutionView [id=");
        builder.append(id);
        builder.append(", updatedAt=");
        builder.append(updatedAt);
        builder.append(", createdAt=");
        builder.append(createdAt);
        builder.append(", jobId=");
        builder.append(jobId);
        builder.append(", status=");
        builder.append(status);
        builder.append(", startedAt=");
        builder.append(startedAt);
        builder.append(", endedAt=");
        builder.append(endedAt);
        builder.append(", priority=");
        builder.append(priority);
        builder.append(", maturity=");
        builder.append(maturity);
        builder.append(", batchId=");
        builder.append(batchId);
        builder.append(", chainId=");
        builder.append(chainId);
        builder.append(", chainPreviousExecutionId=");
        builder.append(chainPreviousExecutionId);
        builder.append(", duration=");
        builder.append(duration);
        builder.append(", parameters=");
        builder.append(parameters);
        builder.append(", failRetry=");
        builder.append(failRetry);
        builder.append(", failRetryExecutionId=");
        builder.append(failRetryExecutionId);
        builder.append(", failMessage=");
        builder.append(failMessage);
        builder.append("]");
        return builder.toString();
    }

    public static Execution map(LegacyExecutionView jobExecution) {
        if (jobExecution == null) {
            return null;
        }
        Execution execution = new Execution();
        execution.setId(jobExecution.getId());
        execution.setJobId(jobExecution.getJobId());
        execution.setStatus(jobExecution.getStatus());
        execution.setFailStatus(ExecutionFailStatus.NONE);
        execution.setStartedAt(jobExecution.getStartedAt());
        execution.setEndedAt(jobExecution.getEndedAt());
        execution.setDuration(jobExecution.getDuration());
        execution.setSummary(jobExecution.getSummary());
        execution.setPriority(jobExecution.isPriority());
        execution.setPlannedFor(jobExecution.getMaturity());
        execution.setBatchId(jobExecution.getBatchId());
        execution.setChainId(jobExecution.getChainId());
        execution.setParameters(jobExecution.getParameters());
        execution.setFailRetry(jobExecution.getFailRetry());
        execution.setFailRetryExecutionId(jobExecution.getFailRetryExecutionId());
        execution.setCreatedAt(jobExecution.getCreatedAt());
        execution.setUpdatedAt(jobExecution.getUpdatedAt());
        return execution;
    }
}
