-- SQL Script to Insert a Vehicle Parked for 67 Hours
-- This can be run directly in phpMyAdmin or MySQL command line
-- Database: parking_lot
-- Table: parked_vehicles

-- Calculate entry time: 67 hours ago from current time
-- Note: Update the entry_millis value if you run this at a different time

-- Current timestamp calculation (example for February 16, 2026)
-- If current time is 2026-02-16 12:00:00, then 67 hours ago is 2026-02-13 17:00:00

INSERT INTO parked_vehicles 
    (license_plate, vehicle_type, entry_time, parking_spot, entry_millis)
VALUES 
    (
        'ABC6700',                                          -- License plate
        'CAR',                                              -- Vehicle type
        DATE_SUB(NOW(), INTERVAL 67 HOUR),                 -- Entry time (67 hours ago)
        'F1-R-015',                                         -- Parking spot
        UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 67 HOUR)) * 1000  -- Entry millis
    )
ON DUPLICATE KEY UPDATE
    vehicle_type = VALUES(vehicle_type),
    entry_time = VALUES(entry_time),
    parking_spot = VALUES(parking_spot),
    entry_millis = VALUES(entry_millis);

-- Optional: Insert additional vehicles with different parking durations
-- Uncomment the lines below to insert more test data

-- INSERT INTO parked_vehicles 
--     (license_plate, vehicle_type, entry_time, parking_spot, entry_millis)
-- VALUES 
--     ('ABC6701', 'CAR', DATE_SUB(NOW(), INTERVAL 72 HOUR), 'F1-R-016', UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 72 HOUR)) * 1000),
--     ('XYZ6702', 'SUV', DATE_SUB(NOW(), INTERVAL 48 HOUR), 'F1-R-017', UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 48 HOUR)) * 1000),
--     ('DEF6703', 'MOTORCYCLE', DATE_SUB(NOW(), INTERVAL 80 HOUR), 'F1-C-005', UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 80 HOUR)) * 1000)
-- ON DUPLICATE KEY UPDATE
--     vehicle_type = VALUES(vehicle_type),
--     entry_time = VALUES(entry_time),
--     parking_spot = VALUES(parking_spot),
--     entry_millis = VALUES(entry_millis);

-- Verify the insertion
SELECT 
    license_plate, 
    vehicle_type, 
    entry_time, 
    parking_spot,
    TIMESTAMPDIFF(HOUR, entry_time, NOW()) AS hours_parked,
    FROM_UNIXTIME(entry_millis/1000) AS entry_timestamp_check
FROM parked_vehicles
WHERE license_plate = 'ABC6700';
