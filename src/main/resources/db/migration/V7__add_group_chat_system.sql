-- V7: Add Group Chat System
-- Real-time chat messaging for groups with user presence tracking

-- Chat Messages Table
CREATE TABLE group_chat_messages (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message_content TEXT NOT NULL,
    message_type VARCHAR(50) NOT NULL DEFAULT 'TEXT',
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    reply_to_message_id BIGINT REFERENCES group_chat_messages(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- User Presence in Groups (for online status)
CREATE TABLE group_user_presence (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    last_seen_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(group_id, user_id)
);

-- Chat Message Reactions (future extensibility)
CREATE TABLE group_chat_reactions (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES group_chat_messages(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reaction_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(message_id, user_id, reaction_type)
);

-- Indexes for performance
CREATE INDEX idx_group_chat_messages_group_id_created_at ON group_chat_messages(group_id, created_at DESC);
CREATE INDEX idx_group_chat_messages_user_id ON group_chat_messages(user_id);
CREATE INDEX idx_group_chat_messages_reply_to ON group_chat_messages(reply_to_message_id) WHERE reply_to_message_id IS NOT NULL;
CREATE INDEX idx_group_user_presence_group_id ON group_user_presence(group_id);
CREATE INDEX idx_group_user_presence_online ON group_user_presence(group_id, is_online) WHERE is_online = TRUE;
CREATE INDEX idx_group_chat_reactions_message_id ON group_chat_reactions(message_id);

-- Comments for documentation
COMMENT ON TABLE group_chat_messages IS 'Real-time chat messages for group discussions';
COMMENT ON TABLE group_user_presence IS 'User online presence and activity tracking for groups';
COMMENT ON TABLE group_chat_reactions IS 'Message reactions (likes, etc.) for enhanced engagement';

COMMENT ON COLUMN group_chat_messages.message_type IS 'TEXT, SYSTEM, MEDIA_SHARE, EMOJI, etc.';
COMMENT ON COLUMN group_chat_messages.is_edited IS 'Track if message was edited for transparency';
COMMENT ON COLUMN group_chat_messages.is_deleted IS 'Soft delete - preserve message structure';
COMMENT ON COLUMN group_user_presence.last_seen_at IS 'Last time user was seen online in any group';
COMMENT ON COLUMN group_user_presence.last_active_at IS 'Last activity timestamp in this specific group'; 