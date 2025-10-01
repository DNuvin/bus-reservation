package com.enactor.busreservation.application.service;

import com.enactor.busreservation.domain.model.JourneyDirection;
import com.enactor.busreservation.domain.model.Reservation;
import com.enactor.busreservation.domain.model.ReservationStatus;
import com.enactor.busreservation.domain.model.Seat;
import com.enactor.busreservation.domain.outbound.ReservationRepositoryPort;
import com.enactor.busreservation.domain.service.ReservationServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationServiceImpl implements ReservationServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepositoryPort repository;

    public ReservationServiceImpl(ReservationRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<Seat> checkAvailability(LocalDate date, String origin, String destination, int passengerCount) {
        log.info("Checking availability for route {} → {} on {}", origin, destination, date);
        JourneyDirection direction = getDirection(origin, destination);

        List<Seat> available = repository.findAvailableSeats(date, origin, destination, direction);
        log.info("Available seats: {}", available.stream().map(Seat::getSeatId).toList());

        if (available.size() < passengerCount) {
            String msg = String.format(
                    "Not enough seats available for %d passengers. Only %d seat(s) available.",
                    passengerCount, available.size()
            );
            log.warn(msg);
            throw new IllegalArgumentException(msg); // changed to IllegalArgumentException
        }

        return available;
    }

    @Override
    public Reservation reserveSeats(LocalDate date, String origin, String destination, List<String> seatIds) {
        synchronized (this) {
            log.info("Attempting to reserve seats {} from {} → {} on {}", seatIds, origin, destination, date);

            int passengerCount = seatIds.size();
            List<Seat> available = checkAvailability(date, origin, destination, passengerCount);

            List<Seat> seatsToReserve = new ArrayList<>();
            List<String> alreadyBooked = new ArrayList<>();

            for (String id : seatIds) {
                Seat seat = available.stream()
                        .filter(s -> s.getSeatId().equals(id))
                        .findFirst()
                        .orElse(null);

                if (seat == null) {
                    alreadyBooked.add(id); // collect all unavailable seats
                } else {
                    seatsToReserve.add(seat);
                }
            }

            if (!alreadyBooked.isEmpty()) {
                String msg = "Seats already booked: " + String.join(", ", alreadyBooked);
                log.warn(msg);
                throw new RuntimeException(msg); // throw with all reserved seats
            }

            int pricePerSeat = calculatePricePerSeat(origin, destination);
            int totalPrice = seatsToReserve.size() * pricePerSeat;
            JourneyDirection direction = getDirection(origin, destination);

            Reservation reservation = new Reservation(
                    "R-" + System.currentTimeMillis(),
                    date,
                    origin,
                    destination,
                    seatsToReserve,
                    totalPrice,
                    ReservationStatus.HELD,
                    direction
            );

            repository.save(reservation, pricePerSeat);
            log.info("Reservation successful: {} | Seats: {} | Total Price: {}",
                    reservation.getReservationId(), seatIds, totalPrice);

            return reservation;
        }
    }


    @Override
    public void updateReservationStatus(String reservationId, ReservationStatus status) {
        log.info("Updating reservation {} status to {}", reservationId, status);
        repository.updateReservationStatus(reservationId, status);
    }

    @Override
    public List<Seat> getAllSeats() {
        log.info("Fetching all seats");
        List<Seat> seats = repository.findAllSeats();
        log.info("Total seats fetched: {}", seats.size());
        return seats;
    }

    @Override
    public void addSeat(String seatId) {
        log.info("Adding new seat: {}", seatId);
        repository.addSeat(new Seat(seatId));
    }

    @Override
    public int calculatePricePerSeat(String origin, String destination) {
        origin = origin.toUpperCase();
        destination = destination.toUpperCase();

        log.info("Calculating price per seat for route {} → {}", origin, destination);

        if ((origin.equals("A") && destination.equals("B")) || (origin.equals("B") && destination.equals("A"))) return 50;
        if ((origin.equals("A") && destination.equals("C")) || (origin.equals("C") && destination.equals("A"))) return 100;
        if ((origin.equals("A") && destination.equals("D")) || (origin.equals("D") && destination.equals("A"))) return 150;
        if ((origin.equals("B") && destination.equals("C")) || (origin.equals("C") && destination.equals("B"))) return 50;
        if ((origin.equals("B") && destination.equals("D")) || (origin.equals("D") && destination.equals("B"))) return 100;
        if ((origin.equals("C") && destination.equals("D")) || (origin.equals("D") && destination.equals("C"))) return 50;

        log.error("Invalid route requested: {} → {}", origin, destination);
        throw new IllegalArgumentException("Invalid route: " + origin + " → " + destination);
    }

    private JourneyDirection getDirection(String origin, String destination) {
        origin = origin.toUpperCase();
        destination = destination.toUpperCase();

        // OUTBOUND: start from A
        if (origin.equals("A") && (destination.equals("B") || destination.equals("C") || destination.equals("D")) ||
                origin.equals("B") && destination.equals("C") ||
                origin.equals("B") && destination.equals("D") ||
                origin.equals("C") && destination.equals("D")) {
            return JourneyDirection.OUTBOUND;
        }
        // RETURN: start from D
        else if (origin.equals("D") && (destination.equals("C") || destination.equals("B") || destination.equals("A")) ||
                origin.equals("C") && destination.equals("B") ||
                origin.equals("C") && destination.equals("A") ||
                origin.equals("B") && destination.equals("A")) {
            return JourneyDirection.RETURN;
        } else {
            throw new IllegalArgumentException("Invalid route: " + origin + " → " + destination);
        }
    }


}
