package net.guidowb.mingming;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.guidowb.mingming.controllers.FakeWorkers;

@SpringBootApplication
@EnableScheduling
public class MingMingController implements CommandLineRunner {

	@Autowired private Environment env;

	public static void main(String[] args) {
        SpringApplication.run(MingMingController.class, args);
    }

	@Override
	public void run(String... args) throws Exception {
	    String serverPort = env.getProperty("SERVER_PORT", "8080");
	    String serverHost = env.getProperty("vcap.application.uris[0]", "localhost:" + serverPort);
	    URI serverURI = URI.create("http://" + serverHost);
		if (env.containsProperty("FAKEWORKERS")) FakeWorkers.create(serverURI, 10);
	}
}
