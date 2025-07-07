-- Add authentication fields to users table
-- Split into separate statements for H2 compatibility
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP;

-- Create index on role for authorization queries
CREATE INDEX idx_users_role ON users(role);

-- Create index on email_verified for user management
CREATE INDEX idx_users_email_verified ON users(email_verified); 