package com.vmanam.recomendr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RecomendrApplication {
	public static void main(String[] args) {

		System.setProperty("aws.accessKeyId", "KEY");
		System.setProperty("aws.secretKey", "SECRET");

		SpringApplication.run(RecomendrApplication.class, args);
	}

}
