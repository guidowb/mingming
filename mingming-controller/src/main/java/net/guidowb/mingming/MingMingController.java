package net.guidowb.mingming;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.guidowb.mingming.fakes.FakeWorkers;

@SpringBootApplication
@EnableScheduling
public class MingMingController implements CommandLineRunner {

	@Autowired private Environment env;

	public static void main(String[] args) {
        SpringApplication.run(MingMingController.class, args);
    }

	@Override
	public void run(String... args) throws Exception {
		FakeWorkers.start(env);
	}
}
