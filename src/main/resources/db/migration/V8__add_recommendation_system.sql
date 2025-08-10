-- V8: Add AI Recommendation System
-- User preference analysis and recommendation tracking

-- User Preference Profiles (calculated taste profiles)
CREATE TABLE user_preference_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Genre preferences (JSON object with genre -> score mapping)
    genre_preferences TEXT NOT NULL DEFAULT '{}',
    
    -- Platform preferences (Netflix, Hulu, etc.)
    platform_preferences TEXT NOT NULL DEFAULT '{}',
    
    -- Time period preferences (80s, 90s, 2000s, etc.)
    era_preferences TEXT NOT NULL DEFAULT '{}',
    
    -- Content length preferences
    preferred_content_length VARCHAR(50) DEFAULT 'MEDIUM', -- SHORT, MEDIUM, LONG
    
    -- Rating patterns
    average_user_rating DECIMAL(3,2) DEFAULT 7.0,
    rating_variance DECIMAL(3,2) DEFAULT 1.5, -- How spread out their ratings are
    
    -- Activity patterns  
    total_interactions INTEGER DEFAULT 0,
    total_completed INTEGER DEFAULT 0,
    completion_rate DECIMAL(3,2) DEFAULT 0.0,
    
    -- Calculated personality type
    viewing_personality VARCHAR(50), -- CASUAL, CRITIC, BINGE_WATCHER, EXPLORER, etc.
    
    -- Preference strength (how confident we are in this profile)
    confidence_score DECIMAL(3,2) DEFAULT 0.0,
    
    -- Timestamps
    last_calculated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(user_id)
);

-- Group Recommendation Cache (pre-calculated group suggestions)
CREATE TABLE group_recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recommended_group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    
    -- Recommendation scoring
    compatibility_score DECIMAL(3,2) NOT NULL, -- 0.0 to 1.0
    reason_code VARCHAR(100), -- GENRE_MATCH, ACTIVITY_LEVEL, SIMILAR_USERS, etc.
    explanation TEXT, -- Human-readable explanation
    
    -- Metadata
    is_viewed BOOLEAN DEFAULT FALSE,
    is_dismissed BOOLEAN DEFAULT FALSE,
    is_joined BOOLEAN DEFAULT FALSE,
    
    -- Timestamps
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '7 days'),
    
    UNIQUE(user_id, recommended_group_id)
);

-- Content Recommendations (what to watch next)
CREATE TABLE content_recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    group_id BIGINT REFERENCES groups(id) ON DELETE CASCADE, -- NULL for personal recommendations
    recommended_media_id BIGINT NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    
    -- Recommendation scoring
    relevance_score DECIMAL(3,2) NOT NULL, -- 0.0 to 1.0
    reason_code VARCHAR(100), -- GENRE_MATCH, TRENDING, GROUP_ACTIVITY, SIMILAR_USERS, etc.
    explanation TEXT,
    
    -- Context
    recommendation_type VARCHAR(50) NOT NULL DEFAULT 'PERSONAL', -- PERSONAL, GROUP, TRENDING
    source_media_id BIGINT REFERENCES media(id), -- "Because you liked X"
    source_group_id BIGINT REFERENCES groups(id), -- "Popular in group Y"
    
    -- User interaction
    is_viewed BOOLEAN DEFAULT FALSE,
    is_dismissed BOOLEAN DEFAULT FALSE,
    is_added_to_library BOOLEAN DEFAULT FALSE,
    user_feedback INTEGER, -- 1-5 star feedback on recommendation quality
    
    -- Timestamps
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '14 days'),
    
    INDEX(user_id, created_at DESC),
    INDEX(group_id, created_at DESC) WHERE group_id IS NOT NULL
);

-- Recommendation Feedback (learning system)
CREATE TABLE recommendation_feedback (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recommendation_type VARCHAR(50) NOT NULL, -- GROUP, CONTENT
    recommendation_id BIGINT NOT NULL, -- Points to group_recommendations or content_recommendations
    
    -- Feedback data
    feedback_type VARCHAR(50) NOT NULL, -- POSITIVE, NEGATIVE, NEUTRAL
    feedback_reason VARCHAR(100), -- NOT_INTERESTED, ALREADY_SEEN, GOOD_MATCH, etc.
    feedback_score INTEGER, -- 1-5 rating
    feedback_text TEXT,
    
    -- Context
    action_taken VARCHAR(50), -- JOINED_GROUP, ADDED_TO_LIBRARY, DISMISSED, etc.
    
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_user_preference_profiles_user_id ON user_preference_profiles(user_id);
CREATE INDEX idx_user_preference_profiles_updated ON user_preference_profiles(last_calculated_at);
CREATE INDEX idx_group_recommendations_user_score ON group_recommendations(user_id, compatibility_score DESC);
CREATE INDEX idx_group_recommendations_expires ON group_recommendations(expires_at) WHERE is_dismissed = FALSE;
CREATE INDEX idx_content_recommendations_user_score ON content_recommendations(user_id, relevance_score DESC);
CREATE INDEX idx_content_recommendations_group ON content_recommendations(group_id, created_at DESC) WHERE group_id IS NOT NULL;
CREATE INDEX idx_recommendation_feedback_user ON recommendation_feedback(user_id, created_at DESC);

-- Comments for documentation
COMMENT ON TABLE user_preference_profiles IS 'Calculated user taste profiles for AI recommendations';
COMMENT ON TABLE group_recommendations IS 'Pre-calculated group suggestions for users';
COMMENT ON TABLE content_recommendations IS 'Media recommendations for users and groups';
COMMENT ON TABLE recommendation_feedback IS 'User feedback on recommendation quality for ML learning';

COMMENT ON COLUMN user_preference_profiles.genre_preferences IS 'JSON: {"action": 0.8, "comedy": 0.6, "drama": 0.3}';
COMMENT ON COLUMN user_preference_profiles.viewing_personality IS 'Algorithmic personality classification';
COMMENT ON COLUMN user_preference_profiles.confidence_score IS 'How confident we are in this profile (0.0-1.0)';
COMMENT ON COLUMN group_recommendations.compatibility_score IS 'Algorithm-calculated compatibility (0.0-1.0)';
COMMENT ON COLUMN content_recommendations.relevance_score IS 'Algorithm-calculated relevance (0.0-1.0)'; 