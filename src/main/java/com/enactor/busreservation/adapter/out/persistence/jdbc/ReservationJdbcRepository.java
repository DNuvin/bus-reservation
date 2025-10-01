package com.enactor.busreservation.adapter.out.persistence.jdbc;

import com.enactor.busreservation.domain.model.JourneyDirection;
import com.enactor.busreservation.domain.model.Reservation;
import com.enactor.busreservation.domain.model.ReservationStatus;
import com.enactor.busreservation.domain.model.Seat;
import com.enactor.busreservation.domain.outbound.ReservationRepositoryPort;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationJdbcRepository implements ReservationRepositoryPort {

    private final DataSource dataSource;

    public ReservationJdbcRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Reservation reservation, int pricePerSeat) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            // Insert into reservations table including direction
            String insertReservation = """
                        INSERT INTO reservations 
                        (id, travel_date, origin, destination, direction, seat_count, seat_numbers, total_price, status, created_at) 
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertReservation)) {
                ps.setString(1, reservation.getReservationId());
                ps.setDate(2, Date.valueOf(reservation.getTravelDate()));
                ps.setString(3, reservation.getOrigin());
                ps.setString(4, reservation.getDestination());
                ps.setString(5, reservation.getDirection().name()); // NEW: direction
                ps.setInt(6, reservation.getSeatCount());
                ps.setString(7, reservation.getSeatNumbers());
                ps.setInt(8, reservation.getTotalPrice());
                ps.setString(9, reservation.getStatus().name());
                ps.executeUpdate();
            }

            // Insert into reservation_seats table
            String insertSeat = """
                        INSERT INTO reservation_seats 
                        (reservation_id, seat_id, travel_date, price, direction) 
                        VALUES (?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertSeat)) {
                for (Seat seat : reservation.getSeats()) {
                    ps.setString(1, reservation.getReservationId());
                    ps.setString(2, seat.getSeatId());
                    ps.setDate(3, Date.valueOf(reservation.getTravelDate()));
                    ps.setInt(4, pricePerSeat);
                    ps.setString(5, reservation.getDirection().name()); // add direction
                    ps.executeUpdate();
                }
            }


            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving reservation", e);
        }
    }

    @Override
    public Reservation findById(String id) {
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * FROM reservations WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return null;

                LocalDate date = rs.getDate("travel_date").toLocalDate();
                String origin = rs.getString("origin");
                String destination = rs.getString("destination");
                String direction = rs.getString("direction");
                int totalPrice = rs.getInt("total_price");
                ReservationStatus status = ReservationStatus.valueOf(rs.getString("status"));

                // Fetch reserved seats
                List<Seat> seats = new ArrayList<>();
                String seatQuery = """
                            SELECT s.seat_id
                            FROM seats s
                            JOIN reservation_seats rs ON s.seat_id = rs.seat_id
                            WHERE rs.reservation_id = ?
                        """;
                try (PreparedStatement ps2 = conn.prepareStatement(seatQuery)) {
                    ps2.setString(1, id);
                    ResultSet seatRS = ps2.executeQuery();
                    while (seatRS.next()) {
                        seats.add(new Seat(
                                seatRS.getString("seat_id")
                        ));
                    }
                }

                return new Reservation(id, date, origin, destination, seats, totalPrice, status, JourneyDirection.valueOf(direction));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching reservation by id", e);
        }
    }

    @Override
    public List<Seat> findAllSeats() {
        List<Seat> allSeats = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT seat_id FROM seats")) {
            while (rs.next()) {
                allSeats.add(new Seat(
                        rs.getString("seat_id")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all seats", e);
        }
        return allSeats;
    }

    @Override
    public void addSeat(Seat seat) {
        try (Connection conn = dataSource.getConnection()) {
            String insert = "INSERT INTO seats (seat_id) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setString(1, seat.getSeatId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding seat", e);
        }
    }

    @Override
    public List<Seat> findBookedSeats(LocalDate date, String origin, String destination) {
        List<Seat> booked = new ArrayList<>();
        String query = """
                    SELECT s.seat_id
                    FROM seats s
                    JOIN reservation_seats rs ON s.seat_id = rs.seat_id
                    JOIN reservations r ON rs.reservation_id = r.id
                    WHERE r.travel_date = ? AND r.origin = ? AND r.destination = ?
                """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setDate(1, Date.valueOf(date));
                ps.setString(2, origin.trim().toUpperCase());
                ps.setString(3, destination.trim().toUpperCase());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    booked.add(new Seat(
                            rs.getString("seat_id")
                    ));
                }
            }

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching booked seats", e);
        }

        return booked;
    }

    @Override
    public List<Seat> findAvailableSeats(LocalDate date, String origin, String destination, JourneyDirection direction) {

        List<Seat> availableSeats = new ArrayList<>();

        String query = """
                    SELECT s.seat_id
                    FROM seats s
                    WHERE s.seat_id NOT IN (
                        SELECT rs.seat_id
                        FROM reservation_seats rs
                        JOIN reservations r ON rs.reservation_id = r.id
                        WHERE r.travel_date = ? AND r.direction = ?
                    )
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDate(1, Date.valueOf(date));
            ps.setString(2, direction.name());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                availableSeats.add(new Seat(
                        rs.getString("seat_id")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching available seats", e);
        }

        return availableSeats;
    }


    @Override
    public void updateReservationStatus(String reservationId, ReservationStatus status) {
        String query = "UPDATE reservations SET status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, status.name());
            ps.setString(2, reservationId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Reservation not found for ID: " + reservationId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating reservation status", e);
        }
    }
}
