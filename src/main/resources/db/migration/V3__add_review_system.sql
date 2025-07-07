-- Create reviews table
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_id BIGINT NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    title VARCHAR(255),
    content TEXT NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 10),
    helpful_votes INTEGER NOT NULL DEFAULT 0,
    total_votes INTEGER NOT NULL DEFAULT 0,
    is_spoiler BOOLEAN NOT NULL DEFAULT false,
    is_moderated BOOLEAN NOT NULL DEFAULT false,
    moderation_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure user can only have one review per media item
    UNIQUE(user_id, media_id)
);

-- Create review_votes table
CREATE TABLE review_votes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    review_id BIGINT NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    is_helpful BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure user can only vote once per review
    UNIQUE(user_id, review_id)
);

-- Create indexes for performance
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_media_id ON reviews(media_id);
CREATE INDEX idx_reviews_created_at ON reviews(created_at DESC);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_helpful_votes ON reviews(helpful_votes DESC);
-- H2-compatible index (no WHERE clause)
CREATE INDEX idx_reviews_moderated ON reviews(is_moderated);

CREATE INDEX idx_review_votes_user_id ON review_votes(user_id);
CREATE INDEX idx_review_votes_review_id ON review_votes(review_id);
CREATE INDEX idx_review_votes_helpful ON review_votes(is_helpful);

-- Create indexes for trending calculations
-- H2-compatible index (no WHERE clause)
CREATE INDEX idx_reviews_recent_helpful ON reviews(created_at DESC, helpful_votes DESC);

-- Add check constraints for data integrity
ALTER TABLE reviews ADD CONSTRAINT chk_reviews_votes_positive 
    CHECK (helpful_votes >= 0 AND total_votes >= 0);
    
ALTER TABLE reviews ADD CONSTRAINT chk_reviews_votes_logic 
    CHECK (helpful_votes <= total_votes);

-- Add comments for documentation
COMMENT ON TABLE reviews IS 'User reviews for media items with voting and moderation support';
COMMENT ON TABLE review_votes IS 'Votes on reviews (helpful/not helpful) with one vote per user per review';

COMMENT ON COLUMN reviews.helpful_votes IS 'Number of users who voted this review as helpful';
COMMENT ON COLUMN reviews.total_votes IS 'Total number of votes (helpful + not helpful)';
COMMENT ON COLUMN reviews.is_spoiler IS 'Whether this review contains spoilers';
COMMENT ON COLUMN reviews.is_moderated IS 'Whether this review has been moderated/hidden';
COMMENT ON COLUMN review_votes.is_helpful IS 'True if vote is helpful, false if not helpful'; 