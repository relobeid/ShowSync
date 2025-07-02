-- Fix JSONB compatibility issue for H2 database support
-- Convert activity_data column from JSONB to TEXT to ensure compatibility
-- with both H2 (development/testing) and PostgreSQL (production)

-- Alter the activity_data column type from JSONB to TEXT
-- This ensures compatibility across H2 and PostgreSQL databases
ALTER TABLE group_activities ALTER COLUMN activity_data TYPE TEXT;

-- Add comment to document the change
COMMENT ON COLUMN group_activities.activity_data IS 'Flexible JSON data storage as TEXT for cross-database compatibility (H2/PostgreSQL)'; 