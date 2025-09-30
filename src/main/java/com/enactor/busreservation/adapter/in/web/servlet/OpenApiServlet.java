package com.enactor.busreservation.adapter.in.web.servlet;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/openapi.json"})
public class OpenApiServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        OpenAPI openAPI = (new OpenAPI()).info((new Info()).title("Bus Reservation API").version("1.0").description("API documentation for Bus Reservation System")).addServersItem((new Server()).url(req.getContextPath()));
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(Json.pretty(openAPI));
    }
}