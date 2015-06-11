package net.guidowb.mingming;

import static org.junit.Assert.*;

import java.net.URI;

import net.guidowb.mingming.MingMingController;
import net.guidowb.mingming.model.Work;
import net.guidowb.mingming.work.Ping;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MingMingController.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class MingmingControllerTests {

	@Value("${local.server.port}")
	int port;

	private URI serverURI;

	@Before
	public void setup() {
		serverURI = URI.create("http://localhost:" + Integer.toString(port));
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void correctlyUnmarshallsWorkSubclasses() {
		RestTemplate template = new RestTemplate();
		Work[] result = template.getForObject(serverURI.resolve("/work"), Work[].class);
		assertEquals(2, result.length);
		assertEquals(Ping.class, result[0].getClass());
	}
}
