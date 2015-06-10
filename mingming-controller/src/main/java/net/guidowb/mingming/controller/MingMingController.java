package net.guidowb.mingming.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class MingMingController implements CommandLineRunner {

	private @Autowired Environment env;

	public static void main(String[] args) {
        SpringApplication.run(MingMingController.class, args);
    }

	@Override
	public void run(String... args) throws Exception {
	}
}
