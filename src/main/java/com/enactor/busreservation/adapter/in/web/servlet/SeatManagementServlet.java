package com.enactor.busreservation.adapter.in.web.servlet;

import com.enactor.busreservation.domain.model.Seat;
import com.enactor.busreservation.domain.service.ReservationServiceInterface;
import com.enactor.busreservation.adapter.in.web.gson.GsonProvider;
import com.enactor.busreservation.adapter.in.web.validation.RequestValidator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/seats")
public class SeatManagementServlet extends HttpServlet {

    private ReservationServiceInterface service;
    private Gson gson;

    @Override
    public void init() {
        service = (ReservationServiceInterface) getServletContext().getAttribute("reservationService");
        gson = GsonProvider.getGson();
    }

    @Operation(summary = "Add a new seat")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seat added"),
            @ApiResponse(responseCode = "400", description = "Invalid seat or JSON")
    })
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            JsonObject body = JsonParser.parseReader(req.getReader()).getAsJsonObject();
            String seatId = body.get("seatId").getAsString();

            RequestValidator.validateSeats(List.of(seatId));

            service.addSeat(seatId);
            responseMap.put("message", "Seat added");
        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            responseMap.put("error", e.getMessage());
        } catch (Exception e) {
            resp.setStatus(400);
            responseMap.put("error", "Invalid JSON body or missing seatId");
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(responseMap));
    }

    @Operation(summary = "Get all seats")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Seat> seats = service.getAllSeats();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(seats));
    }
}
