package net.guidowb.mingming.fakes;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.client.RestTemplate;

import net.guidowb.mingming.model.CanaryInfo;

public class ReliableCanaries {

	private int count;
	private List<CanaryInfo> canaries = null;
	private ScheduledExecutorService updatePool = Executors.newScheduledThreadPool(1);
	private RestTemplate controller = new RestTemplate();
	private URI controllerURI;

	private ReliableCanaries(URI controller, int count) {
		this.controllerURI = controller;
		this.count = count;
	}

	private void start() {
		canaries = new ArrayList<CanaryInfo>();
		for (int index = 0; index < count; index++) {
			canaries.add(createCanary(index));
		}
		updatePool.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() { updateCanaries(); }
			
		}, 1, 5, TimeUnit.SECONDS);
	}

	private CanaryInfo createCanary(int instance) {
		MockEnvironment env = new MockEnvironment()
				.withProperty("vcap.application.uris[0]", "Fake Canaries")
				.withProperty("vcap.application.space_name", "dynamic test data set")
				.withProperty("vcap.application.application_name", "reliable-canary")
				.withProperty("vcap.application.instance_index", Integer.toString(instance));
		CanaryInfo canary = new CanaryInfo(env);
		canary.setLastUpdate();
		return canary;
	}

	private synchronized void updateCanaries() {
		for (CanaryInfo canary : canaries) {
			URI canaryURI = controllerURI.resolve("/canaries/").resolve(canary.getId());
			try { controller.put(canaryURI, canary); }
			catch (Throwable t) {}
		}
	}
	
	public static void create(URI controller, Integer count) {
		if (count == null) count = 10;
		ReliableCanaries fakeCanaries = new ReliableCanaries(controller, count);
		fakeCanaries.start();
	}
}
