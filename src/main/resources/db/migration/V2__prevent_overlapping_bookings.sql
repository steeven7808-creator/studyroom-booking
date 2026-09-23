CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE bookings
    ADD CONSTRAINT bookings_no_overlap
    EXCLUDE USING gist (
        room_id WITH =,
        tsrange(start_time, end_time) WITH &&
    )
    WHERE (status <> 'CANCELLED');