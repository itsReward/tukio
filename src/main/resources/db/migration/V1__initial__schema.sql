-- Path: src/main/resources/db/migration/V1__initial_schema.sql

-- This is an initial migration that will establish Flyway's control over the schema
-- We won't make any changes if tables already exist (since you have data in your schema)

-- Venues table
CREATE TABLE IF NOT EXISTS venues (
                                      id BIGSERIAL PRIMARY KEY,
                                      name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    availability_status BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Venue Amenities table
CREATE TABLE IF NOT EXISTS venue_amenities (
                                               id BIGSERIAL PRIMARY KEY,
                                               name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
    );

-- Join table for venues and amenities
CREATE TABLE IF NOT EXISTS venue_amenity_mapping (
                                                     venue_id BIGINT NOT NULL REFERENCES venues(id),
    amenity_id BIGINT NOT NULL REFERENCES venue_amenities(id),
    PRIMARY KEY (venue_id, amenity_id)
    );

-- Venue Bookings table
CREATE TABLE IF NOT EXISTS venue_bookings (
                                              id BIGSERIAL PRIMARY KEY,
                                              venue_id BIGINT NOT NULL REFERENCES venues(id),
    event_id BIGINT NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    attendee_count INT NOT NULL,
    booking_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_venue_bookings_venue_id ON venue_bookings(venue_id);
CREATE INDEX IF NOT EXISTS idx_venue_bookings_event_id ON venue_bookings(event_id);
CREATE INDEX IF NOT EXISTS idx_venue_bookings_time_range ON venue_bookings(start_time, end_time);