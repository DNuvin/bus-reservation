package com.enactor.busreservation.adapter.in.web.servlet;

import com.enactor.busreservation.application.queue.ReservationQueue;
import com.enactor.busreservation.domain.model.Reservation;
import com.enactor.busreservation.domain.model.ReservationRequest;
import com.enactor.busreservation.adapter.in.web.gson.GsonProvider;
import com.enactor.busreservation.adapter.in.web.validation.RequestValidator;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@WebServlet("/api/reserve")
public class ReserveServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ReserveServlet.class);
    private ReservationQueue queue;
    private Gson gson;

    @Override
    public void init() {
        queue = ReservationQueue.getInstance();
        gson = GsonProvider.getGson();
    }

    @Operation(summary = "Reserve seats for a given route and date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request or seats already booked"),
            @ApiResponse(responseCode = "500", description = "Reservation failed due to server error")
    })
    @Override
    protected void doPost(
            @Parameter(description = "Reservation parameters in form-data") HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            LocalDate date = RequestValidator.validateDate(req.getParameter("date"));
            String origin = req.getParameter("origin");
            String destination = req.getParameter("destination");
            List<String> seatIds = Arrays.asList(req.getParameter("seatIds").split(","));

            RequestValidator.validateOriginDestination(origin, destination);
            RequestValidator.validateSeats(seatIds);

            log.info("Received reservation request for seats {} from {} → {} on {}", seatIds, origin, destination, date);

            CountDownLatch latch = new CountDownLatch(1);

            ReservationRequest request = new ReservationRequest(date, origin, destination, seatIds,
                    new ReservationRequest.ReservationCallback() {
                        @Override
                        public void onSuccess(Reservation reservation) {
                            responseMap.put("reservationId", reservation.getReservationId());
                            responseMap.put("assignedSeats", reservation.getSeats().stream().map(s -> s.getSeatId()).toList());
                            responseMap.put("departure", reservation.getOrigin());
                            responseMap.put("arrival", reservation.getDestination());
                            responseMap.put("totalPriceFinal", reservation.getTotalPrice());
                            latch.countDown();
                            log.info("Reservation completed: {}", reservation.getReservationId());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            responseMap.put("error", e.getMessage());
                            latch.countDown();
                            log.warn("Reservation failed: {}", e.getMessage());
                        }
                    });

            queue.enqueue(request);

            latch.await();

        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            responseMap.put("error", e.getMessage());
        } catch (InterruptedException e) {
            resp.setStatus(500);
            responseMap.put("error", "Reservation interrupted");
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(responseMap));
    }
}
