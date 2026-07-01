CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    position VARCHAR(20),
    is_active BOOLEAN
);

CREATE TABLE IF NOT EXISTS equipment (
    equipment_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN
);

CREATE TABLE IF NOT EXISTS slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    equipment_id INT,
    date DATE,
    time_start TIME,
    CONSTRAINT fk_slots_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id)
);

-- Note: equipment_id is intentionally denormalized here (also stored
-- in slots.equipment_id) to avoid a join when querying booking details.
-- This trades 3NF compliance for query simplicity; referential
-- integrity between bookings.equipment_id and equipment.equipment_id
-- is enforced by the FK constraint below.
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    slot_id INT,
    equipment_id INT,
    status VARCHAR(20),
    time_created TIMESTAMP,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_bookings_slot FOREIGN KEY (slot_id) REFERENCES slots(slot_id),
    CONSTRAINT fk_bookings_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id)
);

CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    booking_id INT,
    status VARCHAR(20),
    time_sent TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_notifications_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

CREATE TABLE IF NOT EXISTS schedules (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    "day" VARCHAR(10),
    time_start TIME,
    time_end TIME,
    date DATE,
    working_day BOOLEAN
);
