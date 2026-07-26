-- Create databases for each microservice to enforce Database per Service isolation
CREATE DATABASE auth_db;
CREATE DATABASE user_db;
CREATE DATABASE event_db;
CREATE DATABASE seat_db;
CREATE DATABASE booking_db;
CREATE DATABASE payment_db;
CREATE DATABASE analytics_db;

-- Grant all privileges on databases to the default postgres user
GRANT ALL PRIVILEGES ON DATABASE auth_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE user_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE event_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE seat_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE booking_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE payment_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE analytics_db TO postgres;
