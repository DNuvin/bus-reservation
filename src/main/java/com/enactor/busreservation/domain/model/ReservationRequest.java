package com.enactor.busreservation.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a reservation request for the queue.
 */
public class ReservationRequest {

    private final LocalDate date;
    private final String origin;
    private final String destination;
    private final List<String> seatIds;
    private final ReservationCallback callback;

    public ReservationRequest(LocalDate date, String origin, String destination, List<String> seatIds, ReservationCallback callback) {
        this.date = date;
        this.origin = origin;
        this.destination = destination;
        this.seatIds = seatIds;
        this.callback = callback;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public List<String> getSeatIds() {
        return seatIds;
    }

    public ReservationCallback getCallback() {
        return callback;
    }

    public interface ReservationCallback {
        void onSuccess(Reservation reservation);
        void onFailure(Exception e);
    }
}
