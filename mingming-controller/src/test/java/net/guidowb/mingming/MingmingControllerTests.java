package net.guidowb.mingming;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import net.guidowb.mingming.MingMingController;
import net.guidowb.mingming.model.Work;
import net.guidowb.mingming.model.WorkerInfo;
import net.guidowb.mingming.work.Ping;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
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

	private static AtomicInteger workerIndex = new AtomicInteger();

	private WorkerInfo createWorker() {
		Integer expectedIndex = workerIndex.getAndIncrement();
		String expectedId = "id-" + Integer.toString(expectedIndex);
		String expectedName = "expected-name-" + Integer.toString(expectedIndex);
		String expectedURI = "http://expected-uri-" + Integer.toString(expectedIndex);
		String expectedIP = "1.2.3.4";
		Integer expectedPort = 5678 + expectedIndex;
		Environment env = new MockEnvironment()
			.withProperty("vcap.application.instance_id", expectedId)
			.withProperty("vcap.application.application_name", expectedName)
			.withProperty("vcap.application.instance_index", Integer.toString(expectedIndex))
			.withProperty("vcap.application.uris[0]", expectedURI)
			.withProperty("CF_INSTANCE_IP", expectedIP)
			.withProperty("SERVER_PORT", Integer.toString(expectedPort));
		WorkerInfo worker = new WorkerInfo(env);
		return worker;
	}

	private URI postWorker(WorkerInfo worker) {
		RestTemplate template = new RestTemplate();
		return serverURI.resolve("/workers/").resolve(template.postForLocation(serverURI.resolve("/workers"), worker));
	}

	@Test
	public void registerWorkerReturnsCorrectURI() {
		WorkerInfo worker = createWorker();
		URI location = postWorker(worker);
		assertEquals(URI.create(serverURI + "/workers/" + worker.getId()), location);
	}
	
	@Test
	public void getWorkerReturnsCorrectProperties() {
		WorkerInfo workerIn = createWorker();
		RestTemplate template = new RestTemplate();
		URI location = postWorker(workerIn);
		WorkerInfo workerOut = template.getForObject(location, WorkerInfo.class);
		assertEquals(workerIn.getId(), workerOut.getId());
		assertEquals(workerIn.getApplicationName(), workerOut.getApplicationName());
		assertEquals(workerIn.getApplicationRoute(), workerOut.getApplicationRoute());
		assertEquals(workerIn.getInstanceId(), workerOut.getInstanceId());
		assertEquals(workerIn.getInstanceIndex(), workerOut.getInstanceIndex());
		assertEquals(workerIn.getInstanceHost(), workerOut.getInstanceHost());
		assertEquals(workerIn.getInstancePort(), workerOut.getInstancePort());
		assertNull(workerOut.getAssignedWork());
	}
}
