package com.enactor.busreservation.domain.model;

import java.util.Objects;

public class Seat {
    private final String seatId;
    private boolean reserved;

    public Seat(String seatId, boolean reserved) {
        this.seatId = seatId;
        this.reserved = reserved;
    }

    public String getSeatId() { return seatId; }
    public boolean isReserved() { return reserved; }
    public void setReserved(boolean reserved) { this.reserved = reserved; }

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
