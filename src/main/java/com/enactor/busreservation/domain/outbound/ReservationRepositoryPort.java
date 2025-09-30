package com.enactor.busreservation.domain.outbound;

import com.enactor.busreservation.domain.model.Reservation;
import com.enactor.busreservation.domain.model.ReservationStatus;
import com.enactor.busreservation.domain.model.Seat;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepositoryPort {
    List<Seat> findAvailableSeats(LocalDate date, String origin, String destination);

    void save(Reservation reservation, int pricePerSeat);

    void updateReservationStatus(String reservationId, ReservationStatus status);

    List<Seat> findAllSeats();

    void addSeat(Seat seat);

    Reservation findById(String id);

    List<Seat> findBookedSeats(LocalDate date, String origin, String destination);

}
