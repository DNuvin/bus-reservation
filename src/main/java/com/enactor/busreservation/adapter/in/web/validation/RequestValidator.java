package com.enactor.busreservation.adapter.in.web.validation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class RequestValidator {

    public static final List<String> ALLOWED_STOPS = List.of("A", "B", "C", "D");
    public static final List<String> ALL_SEATS = List.of(
            "1A","1B","1C","1D","2A","2B","2C","2D","3A","3B","3C","3D",
            "4A","4B","4C","4D","5A","5B","5C","5D","6A","6B","6C","6D",
            "7A","7B","7C","7D","8A","8B","8C","8D","9A","9B","9C","9D",
            "10A","10B","10C","10D"
    );

    public static LocalDate validateDate(String dateStr) throws IllegalArgumentException {
        if (dateStr == null) throw new IllegalArgumentException("Date is required");
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD.");
        }
    }

    public static void validateOriginDestination(String origin, String destination) throws IllegalArgumentException {
        if (origin == null || destination == null ||
                !ALLOWED_STOPS.contains(origin) || !ALLOWED_STOPS.contains(destination)) {
            throw new IllegalArgumentException("Origin and destination must be one of " + ALLOWED_STOPS);
        }
    }

    public static void validateSeats(List<String> seats) throws IllegalArgumentException {
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("Seat list cannot be empty");
        }
        for (String seat : seats) {
            if (!ALL_SEATS.contains(seat)) {
                throw new IllegalArgumentException("Invalid seat selected: " + seat);
            }
        }
    }
}
