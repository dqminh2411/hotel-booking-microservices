package com.hotelbooking.hotelservice.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

	@Bean
	public Logger.Level feignLoggerLevel() {
		return Logger.Level.BASIC;
	}
}

