package com.enactor.busreservation.application;

import com.enactor.busreservation.application.queue.ReservationQueue;
import com.enactor.busreservation.application.queue.ReservationQueueProcessor;
import com.enactor.busreservation.domain.model.Reservation;
import com.enactor.busreservation.domain.model.ReservationRequest;
import com.enactor.busreservation.domain.service.ReservationServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationQueueProcessorTest {

    private ReservationQueue queue;
    private ReservationServiceInterface service;
    private ReservationQueueProcessor processor;

    @BeforeEach
    void setup() throws Exception {
        // Get the singleton queue
        queue = ReservationQueue.getInstance();

        // Clear previous requests via reflection
        java.lang.reflect.Field field = ReservationQueue.class.getDeclaredField("queue");
        field.setAccessible(true);
        ((java.util.concurrent.BlockingQueue<?>) field.get(queue)).clear();

        // Mock the service
        service = mock(ReservationServiceInterface.class);

        // Create processor
        processor = new ReservationQueueProcessor(queue, service);
    }

    @Test
    void testProcessor_SuccessCallback() throws InterruptedException {
        AtomicBoolean callbackCalled = new AtomicBoolean(false);

        // Reservation request with callback
        ReservationRequest request = new ReservationRequest(
                LocalDate.now(), "A", "B", List.of("S1"),
                new ReservationRequest.ReservationCallback() {
                    @Override
                    public void onSuccess(Reservation reservation) {
                        callbackCalled.set(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failure callback should not be called");
                    }
                }
        );

        // Mock service to return a reservation
        Reservation mockReservation = new Reservation("R1", LocalDate.now(), "A", "B", List.of(), 50, null);
        when(service.reserveSeats(any(), any(), any(), any())).thenReturn(mockReservation);

        queue.enqueue(request);

        // Start processor thread
        Thread processorThread = new Thread(() -> processor.run());
        processorThread.start();

        // Wait briefly for processing
        Thread.sleep(100);

        // Stop processor
        processor.stop();
        processorThread.interrupt(); // unblocks take()
        processorThread.join();

        assertTrue(callbackCalled.get(), "Success callback should be called");
        verify(service, times(1)).reserveSeats(any(), any(), any(), any());
    }

    @Test
    void testProcessor_FailureCallback() throws InterruptedException {
        AtomicBoolean failureCalled = new AtomicBoolean(false);

        // Reservation request with callback
        ReservationRequest request = new ReservationRequest(
                LocalDate.now(), "A", "B", List.of("S1"),
                new ReservationRequest.ReservationCallback() {
                    @Override
                    public void onSuccess(Reservation reservation) {
                        fail("Success callback should not be called");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        failureCalled.set(true);
                    }
                }
        );

        // Mock service to throw exception
        when(service.reserveSeats(any(), any(), any(), any())).thenThrow(new RuntimeException("Failed"));

        queue.enqueue(request);

        // Start processor thread
        Thread processorThread = new Thread(() -> processor.run());
        processorThread.start();

        // Wait briefly for processing
        Thread.sleep(100);

        // Stop processor
        processor.stop();
        processorThread.interrupt(); // unblocks take()
        processorThread.join();

        assertTrue(failureCalled.get(), "Failure callback should be called");
        verify(service, times(1)).reserveSeats(any(), any(), any(), any());
    }
}
