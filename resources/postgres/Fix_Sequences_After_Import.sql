-- Realign all sequence-backed columns in PUBLIC schema after a CSV import.
-- Safe to run multiple times.
DO $$
DECLARE
    rec RECORD;
    max_id BIGINT;
BEGIN
    FOR rec IN
        SELECT
            c.table_schema,
            c.table_name,
            c.column_name,
            pg_get_serial_sequence(format('%I.%I', c.table_schema, c.table_name), c.column_name) AS seq_name
        FROM information_schema.columns c
        WHERE c.table_schema = 'public'
    LOOP
        IF rec.seq_name IS NULL THEN
            CONTINUE;
        END IF;

        EXECUTE format(
            'SELECT COALESCE(MAX(%I), 0) FROM %I.%I',
            rec.column_name,
            rec.table_schema,
            rec.table_name
        ) INTO max_id;

        EXECUTE format(
            'SELECT setval(%L::regclass, %s, false)',
            rec.seq_name,
            max_id + 1
        );
    END LOOP;
END $$;

-- Optional verification for the current known issue:
-- SELECT pg_get_serial_sequence('dashboard_daily_summary', 'id') AS seq_name;
-- SELECT MAX(id) AS max_id FROM dashboard_daily_summary;
