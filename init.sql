-- Initialize Famistry Database
-- This script runs when the PostgreSQL container starts for the first time

-- Create database if it doesn't exist
-- (Note: The database is already created by POSTGRES_DB environment variable)

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set default encoding
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE famistry TO famistry;

-- Create schema if needed (optional)
-- CREATE SCHEMA IF NOT EXISTS famistry_schema;
-- SET search_path TO famistry_schema, public;

-- Log initialization
\echo 'Famistry database initialized successfully!'
