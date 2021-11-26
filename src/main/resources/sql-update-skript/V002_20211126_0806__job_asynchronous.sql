-- Add column asynchronous in jobengine_job

ALTER TABLE jobengine_job ADD COLUMN asynchronous bit(1) NOT NULL DEFAULT b'0'; 