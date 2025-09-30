package com.enactor.busreservation.config;

import com.enactor.busreservation.adapter.out.persistence.jdbc.ReservationJdbcRepository;
import com.enactor.busreservation.application.service.ReservationServiceImpl;
import com.enactor.busreservation.domain.service.ReservationServiceInterface;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class AppFactory {

    public static ReservationServiceInterface createService() {
        // Initialize HikariDataSource for H2 in-memory database
        HikariDataSource ds = createDataSource();
        initSchema(ds);
        return new ReservationServiceImpl(new ReservationJdbcRepository(ds));
    }

    private static HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:busdb;DB_CLOSE_DELAY=-1;MODE=MYSQL;DB_CLOSE_ON_EXIT=FALSE");// in-memory H2
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        return new HikariDataSource(config);
    }

    private static void initSchema(HikariDataSource ds) {
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {

            // Drop tables if exist (optional for clean start)
            stmt.execute("DROP TABLE IF EXISTS reservation_seats");
            stmt.execute("DROP TABLE IF EXISTS reservations");
            stmt.execute("DROP TABLE IF EXISTS seats");

            // Seats master table
            stmt.execute("CREATE TABLE seats (" +
                    "seat_id VARCHAR(5) PRIMARY KEY, " +
                    "reserved BOOLEAN DEFAULT FALSE)");

            // Reservations table with seat_count and seat_numbers
            stmt.execute("CREATE TABLE reservations (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "travel_date DATE NOT NULL, " +
                    "origin CHAR(1) NOT NULL, " +
                    "destination CHAR(1) NOT NULL, " +
                    "seat_count INT NOT NULL, " +
                    "seat_numbers VARCHAR(255) NOT NULL, " +   // e.g., "A1,A2"
                    "total_price INT NOT NULL, " +
                    "status VARCHAR(10) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Reservation → Seats mapping table with price
            stmt.execute(
                    "CREATE TABLE reservation_seats (" +
                            "reservation_id VARCHAR(50) NOT NULL, " +
                            "seat_id VARCHAR(5) NOT NULL, " +
                            "travel_date DATE NOT NULL, " +
                            "price INT NOT NULL, " +                     // per-seat price
                            "PRIMARY KEY (reservation_id, seat_id, travel_date), " +
                            "FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE, " +
                            "FOREIGN KEY (seat_id) REFERENCES seats(seat_id) ON DELETE CASCADE, " +
                            "CONSTRAINT unique_seat_per_trip UNIQUE (travel_date, seat_id)" +
                            ");"
            );


            // Populate seats only if empty
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM seats");
            rs.next();
            if (rs.getInt(1) == 0) {
                String[] seatLetters = {"A", "B", "C", "D"};
                for (int row = 1; row <= 10; row++) {
                    for (String letter : seatLetters) {
                        stmt.execute("INSERT INTO seats (seat_id, reserved) VALUES ('" + row + letter + "', FALSE)");
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DB schema", e);
        }
    }


}
