package com.registo.horas_estagio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.registo.horas_estagio"})
public class HorasstagedApplication {

	public static void main(String[] args) {
		SpringApplication.run(HorasstagedApplication.class, args);
	}

}

