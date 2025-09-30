package com.enactor.busreservation.config;

import com.enactor.busreservation.application.queue.ReservationQueue;
import com.enactor.busreservation.application.queue.ReservationQueueProcessor;
import com.enactor.busreservation.domain.service.ReservationServiceInterface;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ReservationServiceInterface service = AppFactory.createService();
            H2ConsoleServer.start();
            sce.getServletContext().setAttribute("reservationService", service);
            ReservationQueue queue = ReservationQueue.getInstance();
            ReservationQueueProcessor processor = new ReservationQueueProcessor(queue, service);
            Thread processorThread = new Thread(processor, "ReservationQueueProcessor");
            processorThread.setDaemon(true);
            processorThread.start();
            sce.getServletContext().setAttribute("reservationQueueProcessor", processor);

            System.out.println("ReservationService initialized successfully.");
        } catch (Exception e) {
            e.printStackTrace(); // Make sure Tomcat logs show any initialization error
            throw new RuntimeException("Failed to initialize ReservationService", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Context destroyed.");
    }
}
