
-- Add column summary in jobengine_execution and jobengine_execution_view

ALTER TABLE jobengine_execution ADD COLUMN summary TEXT NULL DEFAULT NULL AFTER maturity;

DROP VIEW IF EXISTS jobengine_execution_view;
CREATE VIEW jobengine_execution_view AS
SELECT ex.id,
  ex.job_id,
  ex.status,
  ex.started_at,
  ex.ended_at,
  ex.priority,
  ex.maturity,
  ex.batch_id,
  ex.chain_id,
  ex.chain_previous_execution_id,
  ex.duration,
  ex.summary,
  ex.parameters,
  ex.fail_retry,
  ex.fail_retry_execution_id,
  ex.fail_message,
  ex.updated_at,
  ex.created_at
FROM jobengine_execution ex;