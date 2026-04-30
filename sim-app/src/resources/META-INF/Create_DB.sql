-- create the database if it does not already exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'chat') THEN
        PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE chat');
    END IF;
END;
$$ LANGUAGE plpgsql;