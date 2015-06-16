package net.guidowb.mingming.test;

import static org.junit.Assert.*;

import java.net.URI;

import net.guidowb.mingming.MingMingController;
import net.guidowb.mingming.model.WorkerInfo;

import org.junit.AfterClass;
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
public class MingmingWorkerTests {

	@Value("${local.server.port}")
	int port;

	private URI serverURI;
	RestTemplate client;
	private static WorkerPool workerPool = null;

	@Before
	public void setup() {
		serverURI = URI.create("http://localhost:" + Integer.toString(port));
		if (workerPool == null) workerPool = new WorkerPool(serverURI.toString());
		client = new RestTemplate();
	}

	@AfterClass
	public static void teardown() {
		if (workerPool != null) workerPool.shutdown();
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void workerStarts() {
		workerPool.start(1);
		sleep(11);
		WorkerInfo[] workers = client.getForObject(serverURI + "/workers?since=10", WorkerInfo[].class);
		assertEquals(1, workers.length);
	}
	
	@Test
	public void multipleWorkersStart() {
		workerPool.start(3);
		sleep(16);
		WorkerInfo[] workers = client.getForObject(serverURI + "/workers?since=10", WorkerInfo[].class);
		assertEquals(3, workers.length);
	}

	private void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
