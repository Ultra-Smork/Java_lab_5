-- V3: Create command_history table for per-session history
-- Created: 2026-03-31

-- Create command_history table
CREATE TABLE IF NOT EXISTS command_history (
    id SERIAL PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL,
    command VARCHAR(500) NOT NULL,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_command_history_session ON command_history(session_id);
CREATE INDEX IF NOT EXISTS idx_command_history_date ON command_history(executed_at DESC);

-- Add new indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_music_bands_genre_id ON music_bands(genre_id);
CREATE INDEX IF NOT EXISTS idx_music_bands_album_sales ON music_bands(album_sales);
CREATE INDEX IF NOT EXISTS idx_music_bands_number_of_participants ON music_bands(number_of_participants);