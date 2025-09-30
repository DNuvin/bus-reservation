package com.enactor.busreservation.adapter.in.web.servlet;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class OpenApiConfig implements ServletContextListener {
    public void contextInitialized(ServletContextEvent sce) {
        OpenAPI openAPI = (new OpenAPI()).info((new Info()).title("Bus Reservation API").version("1.0").description("API documentation for Bus Reservation System")).addServersItem((new Server()).url("http://localhost:8080/bus-reservation"));
        sce.getServletContext().setAttribute("openapi", openAPI);
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}