package com.enactor.busreservation.application;

import com.enactor.busreservation.application.queue.ReservationQueue;
import com.enactor.busreservation.domain.model.ReservationRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationQueueTest {

    @Test
    void testSingletonInstance() {
        ReservationQueue instance1 = ReservationQueue.getInstance();
        ReservationQueue instance2 = ReservationQueue.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    void testEnqueueAndDequeue() throws InterruptedException {
        ReservationQueue queue = ReservationQueue.getInstance();

        ReservationRequest request = new ReservationRequest(
                LocalDate.now(),
                "A",
                "B",
                List.of("S1", "S2"),
                null
        );

        queue.enqueue(request);
        ReservationRequest dequeued = queue.dequeue();

        assertEquals(request.getOrigin(), dequeued.getOrigin());
        assertEquals(request.getDestination(), dequeued.getDestination());
        assertEquals(request.getSeatIds(), dequeued.getSeatIds());
    }
}
