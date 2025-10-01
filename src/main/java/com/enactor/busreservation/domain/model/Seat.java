package com.enactor.busreservation.domain.model;

import java.util.Objects;

public class Seat {
    private final String seatId;


    public Seat(String seatId) {
        this.seatId = seatId;
    }

    public String getSeatId() { return seatId; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat)) return false;
        Seat seat = (Seat) o;
        return seatId.equals(seat.seatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seatId);
    }
}
