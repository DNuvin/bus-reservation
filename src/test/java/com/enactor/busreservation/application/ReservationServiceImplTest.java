package com.enactor.busreservation.application;

import com.enactor.busreservation.domain.model.*;
import com.enactor.busreservation.domain.outbound.ReservationRepositoryPort;
import com.enactor.busreservation.application.service.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceImplTest {

    private ReservationRepositoryPort repository;
    private ReservationServiceImpl service;

    @BeforeEach
    void setup() {
        repository = mock(ReservationRepositoryPort.class);
        service = new ReservationServiceImpl(repository);
    }

    @Test
    void testCalculatePricePerSeat_ValidRoutes() {
        assertEquals(50, service.calculatePricePerSeat("A", "B"));
        assertEquals(100, service.calculatePricePerSeat("A", "C"));
        assertEquals(150, service.calculatePricePerSeat("A", "D"));
        assertEquals(50, service.calculatePricePerSeat("C", "D"));
    }

    @Test
    void testCalculatePricePerSeat_InvalidRoute() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.calculatePricePerSeat("X", "Y"));
        assertTrue(ex.getMessage().contains("Invalid route"));
    }

    @Test
    void testAddSeat_CallsRepository() {
        service.addSeat("S1");
        verify(repository, times(1)).addSeat(argThat(seat -> seat.getSeatId().equals("S1")));
    }

    @Test
    void testGetAllSeats_CallsRepository() {
        List<Seat> seats = Arrays.asList(new Seat("S1"), new Seat("S2"));
        when(repository.findAllSeats()).thenReturn(seats);

        List<Seat> result = service.getAllSeats();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAllSeats();
    }

    @Test
    void testUpdateReservationStatus_CallsRepository() {
        service.updateReservationStatus("R1", ReservationStatus.CONFIRMED);
        verify(repository, times(1)).updateReservationStatus("R1", ReservationStatus.CONFIRMED);
    }

    @Test
    void testCheckAvailability_SufficientSeats() {
        LocalDate date = LocalDate.now();
        List<Seat> seats = Arrays.asList(new Seat("S1"), new Seat("S2"));
        when(repository.findAvailableSeats(date, "A", "B", JourneyDirection.OUTBOUND)).thenReturn(seats);

        List<Seat> available = service.checkAvailability(date, "A", "B", 2);
        assertEquals(2, available.size());
    }

    @Test
    void testCheckAvailability_NotEnoughSeats() {
        LocalDate date = LocalDate.now();
        List<Seat> seats = Arrays.asList(new Seat("S1"));
        when(repository.findAvailableSeats(date, "A", "B", JourneyDirection.OUTBOUND)).thenReturn(seats);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.checkAvailability(date, "A", "B", 2));
        assertTrue(ex.getMessage().contains("Not enough seats available"));
    }

    @Test
    void testReserveSeats_Success() {
        LocalDate date = LocalDate.now();
        Seat s1 = new Seat("S1");
        Seat s2 = new Seat("S2");
        List<Seat> availableSeats = Arrays.asList(s1, s2);
        when(repository.findAvailableSeats(date, "A", "B", JourneyDirection.OUTBOUND)).thenReturn(availableSeats);

        Reservation reservation = service.reserveSeats(date, "A", "B", Arrays.asList("S1", "S2"));

        assertNotNull(reservation.getReservationId());
        assertEquals(2, reservation.getSeats().size());
        assertEquals(50 * 2, reservation.getTotalPrice());
        assertEquals(ReservationStatus.HELD, reservation.getStatus());
        assertEquals(JourneyDirection.OUTBOUND, reservation.getDirection());

        verify(repository, times(1)).save(any(Reservation.class), eq(50));
    }

    @Test
    void testReserveSeats_SeatAlreadyBooked() {
        LocalDate date = LocalDate.now();
        List<Seat> availableSeats = Arrays.asList(new Seat("S1"));
        when(repository.findAvailableSeats(date, "A", "B", JourneyDirection.OUTBOUND)).thenReturn(availableSeats);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.reserveSeats(date, "A", "B", Arrays.asList("S2")));

        assertTrue(ex.getMessage().contains("already booked"));
        verify(repository, never()).save(any(), anyInt());
    }

    @Test
    void testGetDirection_Outbound() {
        LocalDate date = LocalDate.now();
        Seat s1 = new Seat("S1");

        // Mock repository to return available seat
        when(repository.findAvailableSeats(date, "A", "B", JourneyDirection.OUTBOUND))
                .thenReturn(List.of(s1));

        Reservation reservation = service.reserveSeats(date, "A", "B", List.of("S1"));

        assertEquals(JourneyDirection.OUTBOUND, reservation.getDirection());
    }


    @Test
    void testGetDirection_Return() {
        Reservation reservation = new Reservation("R1", LocalDate.now(), "D", "B",
                List.of(new Seat("S1")), 50, ReservationStatus.HELD, JourneyDirection.RETURN);
        assertEquals(JourneyDirection.RETURN, reservation.getDirection());
    }
}
