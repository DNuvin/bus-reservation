package com.enactor.busreservation.application.queue;

import com.enactor.busreservation.domain.model.Reservation;
import com.enactor.busreservation.domain.model.ReservationRequest;
import com.enactor.busreservation.domain.service.ReservationServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class ReservationQueueProcessor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ReservationQueueProcessor.class);

    private final ReservationQueue queue;
    private final ReservationServiceInterface service;
    private volatile boolean running = true;

    public ReservationQueueProcessor(ReservationQueue queue, ReservationServiceInterface service) {
        this.queue = queue;
        this.service = service;
    }

    @Override
    public void run() {
        log.info("ReservationQueueProcessor started.");
        while (running) {
            try {
                ReservationRequest request = queue.dequeue();
                processRequest(request);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("ReservationQueueProcessor interrupted, stopping.");
                break;
            } catch (Exception e) {
                log.error("Error processing reservation request: {}", e.getMessage(), e);
            }
        }
        log.info("ReservationQueueProcessor stopped.");
    }

    private void processRequest(ReservationRequest request) {
        LocalDate date = request.getDate();
        String origin = request.getOrigin();
        String destination = request.getDestination();
        List<String> seatIds = request.getSeatIds();

        log.info("Processing reservation request for seats {} from {} → {} on {}", seatIds, origin, destination, date);

        try {
            Reservation reservation = service.reserveSeats(date, origin, destination, seatIds);
            log.info("Reservation successful: {}", reservation.getReservationId());
            request.getCallback().onSuccess(reservation);
        } catch (Exception e) {
            log.error("Failed to reserve seats {}: {}", seatIds, e.getMessage(), e);
            request.getCallback().onFailure(e);
        }
    }

    public void stop() {
        running = false;
        log.info("Stopping ReservationQueueProcessor...");
    }
}
