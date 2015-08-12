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

public class DisconnectingCanaries {

	private int count;
	private int timer = 0;
	private List<CanaryInfo> canaries = null;
	private ScheduledExecutorService updatePool = Executors.newScheduledThreadPool(1);
	private RestTemplate controller = new RestTemplate();
	private URI controllerURI;

	private DisconnectingCanaries(URI controller, int count) {
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
			
		}, 3, 5, TimeUnit.SECONDS);
	}

	private CanaryInfo createCanary(int instance) {
		MockEnvironment env = new MockEnvironment()
				.withProperty("vcap.application.uris[0]", "Fake Canaries")
				.withProperty("vcap.application.space_name", "dynamic test data set")
				.withProperty("vcap.application.application_name", "disconnecting-canary")
				.withProperty("vcap.application.instance_index", Integer.toString(instance));
		CanaryInfo canary = new CanaryInfo(env);
		return canary;
	}

	private synchronized void updateCanaries() {
		if (timer < 16) {
			for (CanaryInfo canary : canaries) {
				URI canaryURI = controllerURI.resolve("/canaries/").resolve(canary.getId());
				canary.setLastUpdate();
				try { controller.put(canaryURI, canary); }
				catch (Throwable t) {}
			}
		}
		timer = (timer + 5) % 60;
	}
	
	public static void create(URI controller, Integer count) {
		if (count == null) count = 10;
		DisconnectingCanaries fakeCanaries = new DisconnectingCanaries(controller, count);
		fakeCanaries.start();
	}
}
