package com.famistry.famistry_personnel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class FamistryPersonnelApplication {

	public static void main(String[] args) {
		SpringApplication.run(FamistryPersonnelApplication.class, args);
	}

}
