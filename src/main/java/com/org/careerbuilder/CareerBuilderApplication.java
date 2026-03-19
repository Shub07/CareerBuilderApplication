package com.org.careerbuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CareerBuilderApplication {
	private static final Logger log = LoggerFactory.getLogger(CareerBuilderApplication.class);
	public static void main(String[] args) {
		log.info("🚀 Application started successfully!");
		SpringApplication.run(CareerBuilderApplication.class, args);
	}

}
