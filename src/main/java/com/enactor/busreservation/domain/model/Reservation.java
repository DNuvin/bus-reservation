package com.enactor.busreservation.domain.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Reservation {
    private final String reservationId;
    private final LocalDate travelDate;
    private final String origin;
    private final String destination;
    private final int seatCount;
    private final String seatNumbers;
    private final int totalPrice;
    private ReservationStatus status;
    private final List<Seat> seats;

    public Reservation(String reservationId, LocalDate travelDate,
                       String origin, String destination,
                       List<Seat> seats, int totalPrice,
                       ReservationStatus status) {

        this.reservationId = reservationId;
        this.travelDate = travelDate;
        this.origin = origin;
        this.destination = destination;
        this.seats = new ArrayList<>(seats);
        this.seatCount = seats.size();
        this.seatNumbers = seats.stream()
                .map(Seat::getSeatId)
                .reduce((a, b) -> a + "," + b).orElse("");
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public String getReservationId() { return reservationId; }
    public LocalDate getTravelDate() { return travelDate; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public int getSeatCount() { return seatCount; }
    public String getSeatNumbers() { return seatNumbers; }
    public int getTotalPrice() { return totalPrice; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public List<Seat> getSeats() { return seats; }
}
