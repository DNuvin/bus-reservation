package com.enactor.busreservation.adapter.in.web.servlet;

import com.enactor.busreservation.domain.model.ReservationStatus;
import com.enactor.busreservation.domain.service.ReservationServiceInterface;
import com.enactor.busreservation.adapter.in.web.gson.GsonProvider;
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
import java.util.Map;

@WebServlet("/api/reservation/update")
public class UpdateReservationServlet extends HttpServlet {

    private ReservationServiceInterface service;

    @Override
    public void init() {
        service = (ReservationServiceInterface) getServletContext().getAttribute("reservationService");
    }

    @Operation(summary = "Update reservation status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation updated"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation status or missing fields")
    })
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            JsonObject body = JsonParser.parseReader(req.getReader()).getAsJsonObject();
            String reservationId = body.get("id").getAsString();
            String statusStr = body.get("status").getAsString();

            ReservationStatus status = ReservationStatus.valueOf(statusStr);

            service.updateReservationStatus(reservationId, status);
            responseMap.put("message", "Reservation updated");

        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            responseMap.put("error", "Invalid reservation status or missing fields");
        } catch (Exception e) {
            resp.setStatus(400);
            responseMap.put("error", "Invalid JSON body or missing fields");
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(GsonProvider.getGson().toJson(responseMap));
    }
}
