package com.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    /**
     * "Şu an" bilgisini tek yerden sağlar; testlerde sabit bir zamanla değiştirilebilir.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
