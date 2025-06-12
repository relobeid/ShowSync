-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    profile_picture_url VARCHAR(255),
    bio VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create media table
CREATE TABLE media (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    original_title VARCHAR(255),
    description TEXT,
    release_date TIMESTAMP,
    poster_url VARCHAR(255),
    backdrop_url VARCHAR(255),
    external_id VARCHAR(100),
    external_source VARCHAR(50),
    average_rating DOUBLE PRECISION,
    rating_count INTEGER,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create user_media_interactions table
CREATE TABLE user_media_interactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    media_id BIGINT NOT NULL REFERENCES media(id),
    rating INTEGER CHECK (rating >= 1 AND rating <= 10),
    status VARCHAR(20) NOT NULL,
    review TEXT,
    progress INTEGER,
    is_favorite BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(user_id, media_id)
);

-- Create indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_media_type ON media(type);
CREATE INDEX idx_media_title ON media(title);
CREATE INDEX idx_media_external ON media(external_id, external_source);
CREATE INDEX idx_user_media_user ON user_media_interactions(user_id);
CREATE INDEX idx_user_media_media ON user_media_interactions(media_id);
CREATE INDEX idx_user_media_status ON user_media_interactions(status); 