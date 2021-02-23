package io.coodoo.workhorse.persistence.mysql.legacy.entity;

/**
 * @author coodoo GmbH (coodoo.io)
 */
public enum JobStatus {

    /**
     * Job is in service
     */
    ACTIVE,

    /**
     * Job is not in service
     */
    INACTIVE,

    /**
     * Error occurred while processing the job
     */
    ERROR,

    /**
     * The {@link JobWorker} implementation is missing
     */
    NO_WORKER,

    ;

}
