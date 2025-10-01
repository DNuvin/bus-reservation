package com.enactor.busreservation.adapter;

import com.enactor.busreservation.adapter.out.persistence.jdbc.ReservationJdbcRepository;
import com.enactor.busreservation.domain.model.*;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationJdbcRepositoryTest {

    private static DataSource dataSource;
    private ReservationJdbcRepository repository;

    @BeforeAll
    static void setupDB() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("sa");
        dataSource = ds;

        try (var conn = ds.getConnection();
             var stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE seats (
                    seat_id VARCHAR(10) PRIMARY KEY
                )
            """);

            stmt.execute("""
                CREATE TABLE reservations (
                    id VARCHAR(20) PRIMARY KEY,
                    travel_date DATE,
                    origin VARCHAR(10),
                    destination VARCHAR(10),
                    direction VARCHAR(20),
                    seat_count INT,
                    seat_numbers VARCHAR(255),
                    total_price INT,
                    status VARCHAR(20),
                    created_at TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE reservation_seats (
                    reservation_id VARCHAR(20),
                    seat_id VARCHAR(10),
                    travel_date DATE,
                    price INT,
                    direction VARCHAR(20)
                )
            """);
        }
    }

    @BeforeEach
    void setup() throws Exception {
        repository = new ReservationJdbcRepository(dataSource);

        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM reservation_seats");
            stmt.execute("DELETE FROM reservations");
            stmt.execute("DELETE FROM seats");

            stmt.execute("INSERT INTO seats(seat_id) VALUES('S1')");
            stmt.execute("INSERT INTO seats(seat_id) VALUES('S2')");
            stmt.execute("INSERT INTO seats(seat_id) VALUES('S3')");
        }
    }

    @Test
    void testAddSeatAndFindAllSeats() {
        repository.addSeat(new Seat("S4"));
        List<Seat> seats = repository.findAllSeats();
        assertEquals(4, seats.size());
        assertTrue(seats.stream().anyMatch(s -> s.getSeatId().equals("S4")));
    }

    @Test
    void testSaveAndFindById() {
        LocalDate date = LocalDate.now();
        Seat seat1 = new Seat("S1");
        Seat seat2 = new Seat("S2");
        Reservation reservation = new Reservation("R1", date, "A", "B",
                List.of(seat1, seat2), 100, ReservationStatus.HELD, JourneyDirection.OUTBOUND);

        repository.save(reservation, 50);

        Reservation fetched = repository.findById("R1");
        assertNotNull(fetched);
        assertEquals(2, fetched.getSeats().size());
        assertEquals(ReservationStatus.HELD, fetched.getStatus());

        List<Seat> allSeats = repository.findAllSeats();
        assertEquals(3, allSeats.size()); // total seats in DB
    }

    @Test
    void testFindAvailableSeats() {
        LocalDate date = LocalDate.now();
        Reservation reservation = new Reservation("R2", date, "A", "B",
                List.of(new Seat("S1")), 50, ReservationStatus.HELD, JourneyDirection.OUTBOUND);
        repository.save(reservation, 50);

        List<Seat> available = repository.findAvailableSeats(date, "A", "B", JourneyDirection.OUTBOUND);
        assertEquals(2, available.size()); // S2, S3 should be available
        assertTrue(available.stream().anyMatch(s -> s.getSeatId().equals("S2")));
        assertTrue(available.stream().anyMatch(s -> s.getSeatId().equals("S3")));
    }

    @Test
    void testFindBookedSeats() {
        LocalDate date = LocalDate.now();
        Reservation reservation = new Reservation("R3", date, "A", "C",
                List.of(new Seat("S2")), 50, ReservationStatus.HELD, JourneyDirection.OUTBOUND);
        repository.save(reservation, 50);

        List<Seat> booked = repository.findBookedSeats(date, "A", "C");
        assertEquals(1, booked.size());
        assertEquals("S2", booked.get(0).getSeatId());
    }

    @Test
    void testUpdateReservationStatus() {
        LocalDate date = LocalDate.now();
        Reservation reservation = new Reservation("R4", date, "B", "C",
                List.of(new Seat("S3")), 50, ReservationStatus.HELD, JourneyDirection.OUTBOUND);
        repository.save(reservation, 50);

        repository.updateReservationStatus("R4", ReservationStatus.CONFIRMED);
        Reservation fetched = repository.findById("R4");
        assertEquals(ReservationStatus.CONFIRMED, fetched.getStatus());
    }
}
