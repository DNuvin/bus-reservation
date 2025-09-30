package com.enactor.busreservation.adapter.in.web.servlet;

import com.enactor.busreservation.domain.model.Seat;
import com.enactor.busreservation.domain.service.ReservationServiceInterface;
import com.enactor.busreservation.adapter.in.web.gson.GsonProvider;
import com.enactor.busreservation.adapter.in.web.validation.RequestValidator;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/api/check-availability")
public class CheckAvailabilityServlet extends HttpServlet {

    private ReservationServiceInterface service;
    private Gson gson;

    @Override
    public void init() {
        service = (ReservationServiceInterface) getServletContext().getAttribute("reservationService");
        gson = GsonProvider.getGson();
    }

    @Operation(summary = "Check available seats for a route and date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Available seats retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            LocalDate date = RequestValidator.validateDate(req.getParameter("date"));
            String origin = req.getParameter("origin");
            String destination = req.getParameter("destination");
            RequestValidator.validateOriginDestination(origin, destination);

            int passengerCount = Integer.parseInt(req.getParameter("passengerCount"));

            List<Seat> availableSeats = service.checkAvailability(date, origin, destination);
            int pricePerSeat = service.calculatePricePerSeat(origin, destination);
            int totalPrice = passengerCount * pricePerSeat;

            responseMap.put("available", availableSeats.stream().map(Seat::getSeatId).collect(Collectors.toList()));
            responseMap.put("priceEach", pricePerSeat);
            responseMap.put("totalPriceFinal", totalPrice);

        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            responseMap.put("error", e.getMessage());
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(responseMap));
    }
}
