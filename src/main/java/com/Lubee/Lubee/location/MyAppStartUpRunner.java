package com.Lubee.Lubee.location;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyAppStartUpRunner implements CommandLineRunner {

    @Autowired
    private LocationApiClient locationApiClient;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("MyAppStartUpRunner...");
        System.out.println("loadCultureLocation...");
        locationApiClient.loadCultureLocation();
        //System.out.println("loadRestaurantLocation...");
        //locationApiClient.loadRestaurantLocation();
        System.out.println("MyAppStartUpRunner - FINISHED");
    }

}