package net.guidowb.mingming.fakes;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.client.RestTemplate;

import net.guidowb.mingming.model.CanaryInfo;

public class UnreliableCanaries {

	private int count;
	private List<CanaryInfo> canaries = null;
	private Set<Integer> dyingCanaries = new HashSet<Integer>();
	private Set<Integer> deadCanaries = new HashSet<Integer>();
	private ScheduledExecutorService updatePool = Executors.newScheduledThreadPool(1);
	private RestTemplate controller = new RestTemplate();
	private URI controllerURI;

	private UnreliableCanaries(URI controller, int count) {
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
			
		}, 2, 1, TimeUnit.SECONDS);
	}

	private CanaryInfo createCanary(int instance) {
		MockEnvironment env = new MockEnvironment()
				.withProperty("vcap.application.uris[0]", "Fake Canaries")
				.withProperty("vcap.application.space_name", "dynamic test data set")
				.withProperty("vcap.application.application_name", "unreliable-canary")
				.withProperty("vcap.application.instance_index", Integer.toString(instance));
		CanaryInfo canary = new CanaryInfo(env);
		canary.setLastUpdate();
		return canary;
	}

	private synchronized void updateCanaries() {
		Set<CanaryInfo> toDelete = new HashSet<CanaryInfo>();
		
		// Send updates for canaries that are not dying (if it's been more than 4 seconds since the last update)
		// Mark ones that have been dying for more than 30 seconds for deletion (actual deletion happens outside this loop)
		for (CanaryInfo canary : canaries) {
			int index = canary.getInstanceIndex();
			if (dyingCanaries.contains(index)) {
				if (canary.secondsSinceUpdate() > 30) toDelete.add(canary);	
			}
			else {
				if (canary.secondsSinceUpdate() > 4) {
					URI canaryURI = controllerURI.resolve("/canaries/").resolve(canary.getId());
					canary.setLastUpdate();
					try { controller.put(canaryURI, canary); }
					catch (Throwable t) {}
				}
			}
		}
		
		// Delete the canaries that were marked for deletion
		// Add those canaries to the dead list so that we know which ones to revive
		for (CanaryInfo canary : toDelete) {
			int index = canary.getInstanceIndex();
			canaries.remove(canary);
			deadCanaries.add(index);
			dyingCanaries.remove(index);
		}
		
		// With low odds, mark random canaries as dying
		// Doing this in a loop allows for multiple deaths in the same iteration
		while (Math.random() < 0.1) {
			dyingCanaries.add((int) Math.floor(Math.random() * count));
		}
		
		// If we have dying canaries, with low odds, revive one
		// This is different than replacing, as the instance id remains the same
		if (!dyingCanaries.isEmpty() && Math.random() < 0.1) {
			int index = (int) Math.floor(Math.random() * dyingCanaries.size());
			dyingCanaries.remove(index);
		}

		// If we have dead canaries, after a random interval, replace one with a new instance
		// Doing this in a loop allows for multiple replacements in the same iteration
		while (!deadCanaries.isEmpty() && Math.random() < 0.2) {
			int index = (int) Math.floor(Math.random() * deadCanaries.size());
			index = (int) deadCanaries.toArray()[index];
			deadCanaries.remove(index);
			canaries.add(createCanary(index));
		}
	}
	
	public static void create(URI controller, Integer count) {
		if (count == null) count = 10;
		UnreliableCanaries fakeCanaries = new UnreliableCanaries(controller, count);
		fakeCanaries.start();
	}
}
