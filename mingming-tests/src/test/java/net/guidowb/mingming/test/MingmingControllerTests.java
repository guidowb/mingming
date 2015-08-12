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
import net.guidowb.mingming.model.CanaryInfo;
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

	private static AtomicInteger canaryIndex = new AtomicInteger();

	private CanaryInfo createCanary() {
		Integer expectedIndex = canaryIndex.getAndIncrement();
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
		CanaryInfo canary = new CanaryInfo(env);
		return canary;
	}

	private String postCanary(CanaryInfo canary) {
		client.put(serverURI + "/canaries/" + canary.getId(), canary);
		return canary.getId();
	}

	@Test
	public void registerCanaryReturnsCorrectId() {
		CanaryInfo canary = createCanary();
		String id = postCanary(canary);
		assertEquals(canary.getId(), id);
	}
	
	@Test
	public void getCanaryReturnsCorrectProperties() {
		CanaryInfo canaryIn = createCanary();
		String id = postCanary(canaryIn);
		CanaryInfo canaryOut = client.getForObject(serverURI + "/canaries/" + id, CanaryInfo.class);
		assertEquals(canaryIn.getId(), canaryOut.getId());
		assertEquals(canaryIn.getApplicationName(), canaryOut.getApplicationName());
		assertEquals(canaryIn.getApplicationRoute(), canaryOut.getApplicationRoute());
		assertEquals(canaryIn.getInstanceId(), canaryOut.getInstanceId());
		assertEquals(canaryIn.getInstanceIndex(), canaryOut.getInstanceIndex());
		assertEquals(canaryIn.getInstanceHost(), canaryOut.getInstanceHost());
		assertEquals(canaryIn.getInstancePort(), canaryOut.getInstancePort());
	}

	private Ping createPing() { return createPing(null); }
	private Ping createPing(Schedule schedule) {
		Ping ping = new Ping(schedule, serverURI.toString());
		return ping;
	}

	private PingStatus createPingStatus(CanaryInfo canary) {
		PingStatus status = new PingStatus();
		status.setCanaryId(canary.getId());
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
		CanaryInfo canaryIn = createCanary();
		canaryIn.assignWork("non-existent-work-1");
		canaryIn.assignWork("non-existent-work-2");
		canaryIn.assignWork("non-existent-work-3");
		String id = postCanary(canaryIn);
		Work[] assignedWork = client.getForObject(serverURI + "/canaries/" + id + "/work", Work[].class);
		assertEquals(0, assignedWork.length);
	}

	@Test
	public void assignedWorkIsIgnoredInJson() {
		CanaryInfo canaryIn = createCanary();
		String canaryId = postCanary(canaryIn);
		String work1 = postWork(createPing());
		String work2 = postWork(createPing());
		String work3 = postWork(createPing());
		client.postForLocation(serverURI + "/canaries/" + canaryId + "/work/" + work1, null);
		client.postForLocation(serverURI + "/canaries/" + canaryId + "/work/" + work2, null);
		client.postForLocation(serverURI + "/canaries/" + canaryId + "/work/" + work3, null);
		CanaryInfo canaryOut = client.getForObject(serverURI + "/canaries/" + canaryId, CanaryInfo.class);
		assertFalse(canaryOut.getAssignedWork().iterator().hasNext());
	}

	@Test
	public void assignedWorkIsReturned() {
		CanaryInfo canaryIn = createCanary();
		String canaryId = postCanary(canaryIn);
		String work1 = postWork(createPing());
		String work2 = postWork(createPing());
		String work3 = postWork(createPing());
		client.postForLocation(serverURI + "/canaries/" + canaryId + "/work/" + work1, null);
		client.postForLocation(serverURI + "/canaries/" + canaryId + "/work/" + work2, null);
		client.postForLocation(serverURI + "/canaries/" + canaryId + "/work/" + work3, null);
		Work[] assignedWork = client.getForObject(serverURI + "/canaries/" + canaryId + "/work", Work[].class);
		assertEquals(3, assignedWork.length);
	}
	
	@Test
	public void unassignedWorkIsRemoved() {
		CanaryInfo canaryIn = createCanary();
		String canaryId = postCanary(canaryIn);
		String work1 = postWork(createPing());
		String work2 = postWork(createPing());
		String work3 = postWork(createPing());
		client.postForLocation(serverURI + "/canaries/" + canaryId + "/work/" + work1, null);
		client.postForLocation(serverURI + "/canaries/" + canaryId + "/work/" + work2, null);
		client.postForLocation(serverURI + "/canaries/" + canaryId + "/work/" + work3, null);
		client.delete(serverURI + "/canaries/" + canaryId + "/work/" + work2);
		Work[] assignedWork = client.getForObject(serverURI + "/canaries/" + canaryId + "/work", Work[].class);
		assertEquals(2, assignedWork.length);
	}
	
	@Test
	public void reportStatus() {
		CanaryInfo canaryIn = createCanary();
		canaryIn.reportWorkStatus(createPingStatus(canaryIn));
		canaryIn.reportWorkStatus(createPingStatus(canaryIn));
		canaryIn.reportWorkStatus(createPingStatus(canaryIn));
		postCanary(canaryIn);
	}
	
	@Test
	public void statusIsPersisted() {
		CanaryInfo canaryIn = createCanary();
		canaryIn.reportWorkStatus(createPingStatus(canaryIn));
		canaryIn.reportWorkStatus(createPingStatus(canaryIn));
		canaryIn.reportWorkStatus(createPingStatus(canaryIn));
		String canaryId = postCanary(canaryIn);
		WorkStatus[] stati = client.getForObject(serverURI + "/canaries/" + canaryId + "/status", WorkStatus[].class);
		assertEquals(3, stati.length);
	}
}
