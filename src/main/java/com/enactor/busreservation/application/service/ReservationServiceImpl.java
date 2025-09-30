package com.enactor.busreservation.application.service;

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
    public List<Seat> checkAvailability(LocalDate date, String origin, String destination) {
        log.info("Checking availability for route {} → {} on {}", origin, destination, date);
        List<Seat> available = repository.findAvailableSeats(date, origin, destination);
        log.info("Available seats: {}", available.stream().map(Seat::getSeatId).toList());
        return available;
    }

    @Override
    public Reservation reserveSeats(LocalDate date, String origin, String destination, List<String> seatIds) {
        synchronized (this) {
            log.info("Attempting to reserve seats {} from {} → {} on {}", seatIds, origin, destination, date);
            List<Seat> available = checkAvailability(date, origin, destination);

            List<Seat> seatsToReserve = new ArrayList<>();
            for (String id : seatIds) {
                Seat seat = available.stream()
                        .filter(s -> s.getSeatId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> {
                            log.warn("Seat {} already booked", id);
                            return new RuntimeException("Seat " + id + " already booked");
                        });
                seat.setReserved(true);
                seatsToReserve.add(seat);
            }

            int pricePerSeat = calculatePricePerSeat(origin, destination);
            int totalPrice = seatsToReserve.size() * pricePerSeat;

            Reservation reservation = new Reservation(
                    "R-" + System.currentTimeMillis(),
                    date,
                    origin,
                    destination,
                    seatsToReserve,
                    totalPrice,
                    ReservationStatus.HELD
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
        repository.addSeat(new Seat(seatId, false));
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
}
