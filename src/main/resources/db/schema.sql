-- ==============================
-- Bus Reservation System Schema (H2)
-- ==============================

-- Drop existing tables if any (for reset)
DROP TABLE IF EXISTS reservation_seats;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS seats;

-- Seats master table
CREATE TABLE seats (
    seat_id VARCHAR(5) PRIMARY KEY,
    reserved BOOLEAN DEFAULT FALSE
);

-- Reservations table
CREATE TABLE reservations (
    id VARCHAR(50) PRIMARY KEY,
    travel_date DATE NOT NULL,
    origin CHAR(1) NOT NULL,
    destination CHAR(1) NOT NULL,
    seat_count INT NOT NULL,
    seat_numbers VARCHAR(255) NOT NULL, -- e.g., "A1,A2,A3"
    total_price INT NOT NULL,
    status VARCHAR(10) NOT NULL, -- e.g., CONFIRMED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Reservation → Seats mapping table
CREATE TABLE reservation_seats (
    reservation_id VARCHAR(50) NOT NULL,
    seat_id VARCHAR(5) NOT NULL,
    travel_date DATE NOT NULL,
    price INT NOT NULL,
    PRIMARY KEY (reservation_id, seat_id, travel_date),
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id) ON DELETE CASCADE
);