package com.enactor.busreservation.config;

import org.h2.tools.Server;

public class H2ConsoleServer {

    public static void start() {
        try {
            // Start Web console for the same JVM (can see in-memory DB)
            Server webServer = Server.createWebServer("-webPort", "8082", "-webAllowOthers").start();
            System.out.println("H2 Web Console started at: " + webServer.getURL());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to start H2 Web Console", e);
        }
    }
}
