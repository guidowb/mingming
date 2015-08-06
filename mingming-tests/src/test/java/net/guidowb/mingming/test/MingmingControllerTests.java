package net.guidowb.mingming.test;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.guidowb.mingming.MingMingController;
import net.guidowb.mingming.model.Schedule;
import net.guidowb.mingming.model.ScheduleOnce;
import net.guidowb.mingming.model.ScheduleRepeat;
import net.guidowb.mingming.model.Work;
import net.guidowb.mingming.model.WorkStatus;
import net.guidowb.mingming.model.WorkerInfo;
import net.guidowb.mingming.work.Ping;
import net.guidowb.mingming.work.Ping.PingStatus;

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
	private RestTemplate client;

	@Before
	public void setup() {
		serverURI = URI.create("http://localhost:" + Integer.toString(port));
		client = new RestTemplate();
	}

	@Test
	public void contextLoads() {
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

	private String postWorker(WorkerInfo worker) {
		client.put(serverURI + "/workers/" + worker.getId(), worker);
		return worker.getId();
	}

	@Test
	public void registerWorkerReturnsCorrectId() {
		WorkerInfo worker = createWorker();
		String id = postWorker(worker);
		assertEquals(worker.getId(), id);
	}
	
	@Test
	public void getWorkerReturnsCorrectProperties() {
		WorkerInfo workerIn = createWorker();
		String id = postWorker(workerIn);
		WorkerInfo workerOut = client.getForObject(serverURI + "/workers/" + id, WorkerInfo.class);
		assertEquals(workerIn.getId(), workerOut.getId());
		assertEquals(workerIn.getApplicationName(), workerOut.getApplicationName());
		assertEquals(workerIn.getApplicationRoute(), workerOut.getApplicationRoute());
		assertEquals(workerIn.getInstanceId(), workerOut.getInstanceId());
		assertEquals(workerIn.getInstanceIndex(), workerOut.getInstanceIndex());
		assertEquals(workerIn.getInstanceHost(), workerOut.getInstanceHost());
		assertEquals(workerIn.getInstancePort(), workerOut.getInstancePort());
	}

	private Ping createPing() { return createPing(null); }
	private Ping createPing(Schedule schedule) {
		Ping ping = new Ping(schedule, serverURI.toString());
		return ping;
	}

	private PingStatus createPingStatus(WorkerInfo worker) {
		PingStatus status = new PingStatus();
		status.setWorkerId(worker.getId());
		status.setWorkId(postWork(createPing()));
		status.setTimestamp();
		return status;
	}

	private String postWork(Work work) {
		return client.postForLocation(serverURI + "/work", work).toString();
	}

	@Test
	public void createWorkReturnsUUID() {
		Work work = createPing();
		String id = postWork(work);
		assertTrue(id.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
	}

	@Test
	public void getWorkReturnsCorrectSubclassForPing() {
		Ping ping = createPing();
		String id = postWork(ping);
		Work work = client.getForObject(serverURI + "/work/" + id, Work.class);
		assertTrue(work instanceof Ping);
	}

	@Test
	public void getWorkReturnsCorrectScheduleForOnce() {
		Ping ping = createPing(Schedule.once());
		String id = postWork(ping);
		Work work = client.getForObject(serverURI + "/work/" + id, Work.class);
		assertTrue(work.getSchedule() instanceof ScheduleOnce);
	}

	@Test
	public void getWorkReturnsCorrectScheduleForRepeat() {
		Ping ping = createPing(Schedule.repeat(5L, TimeUnit.MINUTES));
		String id = postWork(ping);
		Work work = client.getForObject(serverURI + "/work/" + id, Work.class);
		assertTrue(work.getSchedule() instanceof ScheduleRepeat);
		ScheduleRepeat repeat = (ScheduleRepeat) work.getSchedule();
		assertEquals((Long) 5L, repeat.getPeriod());
		assertEquals(TimeUnit.MINUTES, repeat.getUnit());
	}

	@Test
	public void assignedWorkIsIgnoredOnPost() {
		WorkerInfo workerIn = createWorker();
		workerIn.assignWork("non-existent-work-1");
		workerIn.assignWork("non-existent-work-2");
		workerIn.assignWork("non-existent-work-3");
		String id = postWorker(workerIn);
		Work[] assignedWork = client.getForObject(serverURI + "/workers/" + id + "/work", Work[].class);
		assertEquals(0, assignedWork.length);
	}

	@Test
	public void assignedWorkIsIgnoredInJson() {
		WorkerInfo workerIn = createWorker();
		String workerId = postWorker(workerIn);
		String work1 = postWork(createPing());
		String work2 = postWork(createPing());
		String work3 = postWork(createPing());
		client.postForLocation(serverURI + "/workers/" + workerId + "/work/" + work1, null);
		client.postForLocation(serverURI + "/workers/" + workerId + "/work/" + work2, null);
		client.postForLocation(serverURI + "/workers/" + workerId + "/work/" + work3, null);
		WorkerInfo workerOut = client.getForObject(serverURI + "/workers/" + workerId, WorkerInfo.class);
		assertFalse(workerOut.getAssignedWork().iterator().hasNext());
	}

	@Test
	public void assignedWorkIsReturned() {
		WorkerInfo workerIn = createWorker();
		String workerId = postWorker(workerIn);
		String work1 = postWork(createPing());
		String work2 = postWork(createPing());
		String work3 = postWork(createPing());
		client.postForLocation(serverURI + "/workers/" + workerId + "/work/" + work1, null);
		client.postForLocation(serverURI + "/workers/" + workerId + "/work/" + work2, null);
		client.postForLocation(serverURI + "/workers/" + workerId + "/work/" + work3, null);
		Work[] assignedWork = client.getForObject(serverURI + "/workers/" + workerId + "/work", Work[].class);
		assertEquals(3, assignedWork.length);
	}
	
	@Test
	public void unassignedWorkIsRemoved() {
		WorkerInfo workerIn = createWorker();
		String workerId = postWorker(workerIn);
		String work1 = postWork(createPing());
		String work2 = postWork(createPing());
		String work3 = postWork(createPing());
		client.postForLocation(serverURI + "/workers/" + workerId + "/work/" + work1, null);
		client.postForLocation(serverURI + "/workers/" + workerId + "/work/" + work2, null);
		client.postForLocation(serverURI + "/workers/" + workerId + "/work/" + work3, null);
		client.delete(serverURI + "/workers/" + workerId + "/work/" + work2);
		Work[] assignedWork = client.getForObject(serverURI + "/workers/" + workerId + "/work", Work[].class);
		assertEquals(2, assignedWork.length);
	}
	
	@Test
	public void reportStatus() {
		WorkerInfo workerIn = createWorker();
		workerIn.reportWorkStatus(createPingStatus(workerIn));
		workerIn.reportWorkStatus(createPingStatus(workerIn));
		workerIn.reportWorkStatus(createPingStatus(workerIn));
		postWorker(workerIn);
	}
	
	@Test
	public void statusIsPersisted() {
		WorkerInfo workerIn = createWorker();
		workerIn.reportWorkStatus(createPingStatus(workerIn));
		workerIn.reportWorkStatus(createPingStatus(workerIn));
		workerIn.reportWorkStatus(createPingStatus(workerIn));
		String workerId = postWorker(workerIn);
		WorkStatus[] stati = client.getForObject(serverURI + "/workers/" + workerId + "/status", WorkStatus[].class);
		assertEquals(3, stati.length);
	}
}
