package com.enactor.busreservation.application.queue;

import com.enactor.busreservation.domain.model.ReservationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReservationQueue {

    private static final Logger log = LoggerFactory.getLogger(ReservationQueue.class);
    private static final ReservationQueue INSTANCE = new ReservationQueue();
    private final BlockingQueue<ReservationRequest> queue = new LinkedBlockingQueue<>();

    private ReservationQueue() {}

    public static ReservationQueue getInstance() {
        return INSTANCE;
    }

    public void enqueue(ReservationRequest request) {
        log.info("Enqueuing reservation request for seats {} from {} → {} on {}",
                request.getSeatIds(), request.getOrigin(), request.getDestination(), request.getDate());
        queue.offer(request);
    }

    public ReservationRequest dequeue() throws InterruptedException {
        ReservationRequest request = queue.take();
        log.info("Dequeued reservation request for seats {} from {} → {} on {}",
                request.getSeatIds(), request.getOrigin(), request.getDestination(), request.getDate());
        return request;
    }
}
