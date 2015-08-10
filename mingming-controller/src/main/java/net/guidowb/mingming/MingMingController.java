package net.guidowb.mingming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MingMingController {

	public static void main(String[] args) {
        SpringApplication.run(MingMingController.class, args);
    }
}
