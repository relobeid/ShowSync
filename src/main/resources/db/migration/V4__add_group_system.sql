-- Create groups table
CREATE TABLE groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    privacy_setting VARCHAR(20) NOT NULL DEFAULT 'PUBLIC', -- PUBLIC, PRIVATE
    created_by BIGINT NOT NULL REFERENCES users(id),
    max_members INTEGER DEFAULT 50,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create group_memberships table (junction table for many-to-many)
CREATE TABLE group_memberships (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER', -- OWNER, ADMIN, MEMBER
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, PENDING, BANNED
    joined_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(group_id, user_id)
);

-- Create indexes for performance
CREATE INDEX idx_groups_created_by ON groups(created_by);
CREATE INDEX idx_groups_privacy_active ON groups(privacy_setting, is_active);
CREATE INDEX idx_groups_name_active ON groups(name, is_active) WHERE is_active = true;

CREATE INDEX idx_group_memberships_group ON group_memberships(group_id);
CREATE INDEX idx_group_memberships_user ON group_memberships(user_id);
CREATE INDEX idx_group_memberships_status ON group_memberships(group_id, status);
CREATE INDEX idx_group_memberships_role ON group_memberships(group_id, role);

-- Add constraint to ensure at least one owner per group
-- This will be enforced at application level initially 