INSERT INTO rooms (name, building, capacity, open_time, close_time) VALUES
    ('IKB 101', 'Irving K. Barber Learning Centre', 6, '08:00', '22:00'),
    ('IKB 102', 'Irving K. Barber Learning Centre', 4, '08:00', '22:00'),
    ('Woodward 201', 'Woodward Library', 8, '09:00', '18:00')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (name, email) VALUES
    ('Steven Zhang', 'steven@example.com'),
    ('Alex Chen', 'alex@example.com')
ON CONFLICT (email) DO NOTHING;