package com.enactor.busreservation.domain.service;

import com.enactor.busreservation.domain.model.Reservation;
import com.enactor.busreservation.domain.model.ReservationStatus;
import com.enactor.busreservation.domain.model.Seat;

import java.time.LocalDate;
import java.util.List;

public interface ReservationServiceInterface {
    List<Seat> checkAvailability(LocalDate date, String origin, String destination);

    Reservation reserveSeats(LocalDate date, String origin, String destination, List<String> seatIds);

    void updateReservationStatus(String reservationId, ReservationStatus status);

    List<Seat> getAllSeats();

    void addSeat(String seatId);

    int calculatePricePerSeat(String origin, String destination);
}
