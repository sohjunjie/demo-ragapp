package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		var apiKey = System.getenv("GEMINI_API_KEY");
		log.info("api key = {}", apiKey);
		SpringApplication.run(DemoApplication.class, args);
	}

}
