package com.nwltecnologia.studiobelle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StudiobelleApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudiobelleApplication.class, args);
	}

}
