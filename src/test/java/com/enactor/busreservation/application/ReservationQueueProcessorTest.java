package com.enactor.busreservation.application.queue;

import com.enactor.busreservation.domain.model.*;
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
        queue = ReservationQueue.getInstance();

        java.lang.reflect.Field field = ReservationQueue.class.getDeclaredField("queue");
        field.setAccessible(true);
        ((java.util.concurrent.BlockingQueue<?>) field.get(queue)).clear();

        service = mock(ReservationServiceInterface.class);

        processor = new ReservationQueueProcessor(queue, service);
    }

    @Test
    void testProcessor_SuccessCallback() throws InterruptedException {
        AtomicBoolean callbackCalled = new AtomicBoolean(false);

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

        Reservation mockReservation = new Reservation(
                "R1",
                LocalDate.now(),
                "A",
                "B",
                List.of(),
                50,
                ReservationStatus.HELD,
                JourneyDirection.OUTBOUND
        );

        when(service.reserveSeats(any(), any(), any(), any())).thenReturn(mockReservation);

        queue.enqueue(request);

        Thread processorThread = new Thread(() -> processor.run());
        processorThread.start();

        Thread.sleep(100);

        processor.stop();
        processorThread.interrupt();
        processorThread.join();

        assertTrue(callbackCalled.get());
        verify(service, times(1)).reserveSeats(any(), any(), any(), any());
    }

    @Test
    void testProcessor_FailureCallback() throws InterruptedException {
        AtomicBoolean failureCalled = new AtomicBoolean(false);

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

        when(service.reserveSeats(any(), any(), any(), any())).thenThrow(new RuntimeException("Failed"));

        queue.enqueue(request);

        Thread processorThread = new Thread(() -> processor.run());
        processorThread.start();

        Thread.sleep(100);

        processor.stop();
        processorThread.interrupt();
        processorThread.join();

        assertTrue(failureCalled.get());
        verify(service, times(1)).reserveSeats(any(), any(), any(), any());
    }
}
