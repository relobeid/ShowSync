-- Create group_media_lists table for group-level media collections
CREATE TABLE group_media_lists (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    media_id BIGINT NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    list_type VARCHAR(20) NOT NULL, -- CURRENTLY_WATCHING, COMPLETED, PLAN_TO_WATCH, DROPPED, ON_HOLD
    added_by BIGINT NOT NULL REFERENCES users(id),
    group_rating DOUBLE PRECISION, -- Aggregated rating from group members
    total_votes INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(group_id, media_id)
);

-- Create group_media_votes table for voting on what to watch next
CREATE TABLE group_media_votes (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    media_id BIGINT NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vote_type VARCHAR(20) NOT NULL, -- WATCH_NEXT, SKIP, PRIORITY_HIGH, PRIORITY_LOW
    suggested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(group_id, media_id, user_id)
);

-- Create group_activities table for activity feed
CREATE TABLE group_activities (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL, -- MEDIA_ADDED, MEDIA_RATED, MEDIA_COMPLETED, VOTE_CAST, MEMBER_JOINED, etc.
    target_media_id BIGINT REFERENCES media(id),
    target_user_id BIGINT REFERENCES users(id),
    activity_data JSONB, -- Flexible data storage for activity-specific information
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_group_media_lists_group_id ON group_media_lists(group_id);
CREATE INDEX idx_group_media_lists_media_id ON group_media_lists(media_id);
CREATE INDEX idx_group_media_lists_type ON group_media_lists(group_id, list_type);
CREATE INDEX idx_group_media_lists_added_by ON group_media_lists(added_by);

CREATE INDEX idx_group_media_votes_group_id ON group_media_votes(group_id);
CREATE INDEX idx_group_media_votes_media_id ON group_media_votes(media_id);
CREATE INDEX idx_group_media_votes_user_id ON group_media_votes(user_id);
CREATE INDEX idx_group_media_votes_type ON group_media_votes(group_id, vote_type);
CREATE INDEX idx_group_media_votes_suggested_at ON group_media_votes(group_id, suggested_at DESC);

CREATE INDEX idx_group_activities_group_id ON group_activities(group_id);
CREATE INDEX idx_group_activities_user_id ON group_activities(user_id);
CREATE INDEX idx_group_activities_type ON group_activities(activity_type);
CREATE INDEX idx_group_activities_created_at ON group_activities(group_id, created_at DESC);
CREATE INDEX idx_group_activities_target_media ON group_activities(target_media_id) WHERE target_media_id IS NOT NULL;
CREATE INDEX idx_group_activities_target_user ON group_activities(target_user_id) WHERE target_user_id IS NOT NULL; 