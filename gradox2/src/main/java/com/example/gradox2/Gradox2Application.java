package com.example.gradox2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Gradox2Application {

	public static void main(String[] args) {
		SpringApplication.run(Gradox2Application.class, args);
	}

}
