package com.enactor.busreservation.adapter.in.web.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDate;

public class GsonProvider {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private GsonProvider() {} // prevent instantiation

    public static Gson getGson() {
        return gson;
    }
}
