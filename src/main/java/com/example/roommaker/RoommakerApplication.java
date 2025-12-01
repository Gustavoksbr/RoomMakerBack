package com.example.roommaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RoommakerApplication {

	public static void main(String[] args) {

		SpringApplication.run(RoommakerApplication.class, args);
	}
}
