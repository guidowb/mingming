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

import net.guidowb.mingming.model.WorkerInfo;

public class UnreliableWorkers {

	private int count;
	private List<WorkerInfo> workers = null;
	private Set<Integer> dyingWorkers = new HashSet<Integer>();
	private Set<Integer> deadWorkers = new HashSet<Integer>();
	private ScheduledExecutorService updatePool = Executors.newScheduledThreadPool(1);
	private RestTemplate controller = new RestTemplate();
	private URI controllerURI;

	private UnreliableWorkers(URI controller, int count) {
		this.controllerURI = controller;
		this.count = count;
	}

	private void start() {
		workers = new ArrayList<WorkerInfo>();
		for (int index = 0; index < count; index++) {
			workers.add(createWorker(index));
		}
		updatePool.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() { updateWorkers(); }
			
		}, 2, 1, TimeUnit.SECONDS);
	}

	private WorkerInfo createWorker(int instance) {
		MockEnvironment env = new MockEnvironment()
				.withProperty("vcap.application.uris[0]", "Fake Canaries")
				.withProperty("vcap.application.space_name", "dynamic test data set")
				.withProperty("vcap.application.application_name", "unreliable-canary")
				.withProperty("vcap.application.instance_index", Integer.toString(instance));
		WorkerInfo worker = new WorkerInfo(env);
		worker.setLastUpdate();
		return worker;
	}

	private synchronized void updateWorkers() {
		Set<WorkerInfo> toDelete = new HashSet<WorkerInfo>();
		
		// Send updates for workers that are not dying (if it's been more than 4 seconds since the last update)
		// Mark ones that have been dying for more than 30 seconds for deletion (actual deletion happens outside this loop)
		for (WorkerInfo worker : workers) {
			int index = worker.getInstanceIndex();
			if (dyingWorkers.contains(index)) {
				if (worker.secondsSinceUpdate() > 30) toDelete.add(worker);	
			}
			else {
				if (worker.secondsSinceUpdate() > 4) {
					URI workerURI = controllerURI.resolve("/workers/").resolve(worker.getId());
					worker.setLastUpdate();
					try { controller.put(workerURI, worker); }
					catch (Throwable t) {}
				}
			}
		}
		
		// Delete the workers that were marked for deletion
		// Add those workers to the dead list so that we know which ones to revive
		for (WorkerInfo worker : toDelete) {
			int index = worker.getInstanceIndex();
			workers.remove(worker);
			deadWorkers.add(index);
			dyingWorkers.remove(index);
		}
		
		// With low odds, mark random workers as dying
		// Doing this in a loop allows for multiple deaths in the same iteration
		while (Math.random() < 0.1) {
			dyingWorkers.add((int) Math.floor(Math.random() * count));
		}
		
		// If we have dying workers, with low odds, revive one
		// This is different than replacing, as the instance id remains the same
		if (!dyingWorkers.isEmpty() && Math.random() < 0.1) {
			int index = (int) Math.floor(Math.random() * dyingWorkers.size());
			dyingWorkers.remove(index);
		}

		// If we have dead workers, after a random interval, replace one with a new instance
		// Doing this in a loop allows for multiple replacements in the same iteration
		while (!deadWorkers.isEmpty() && Math.random() < 0.2) {
			int index = (int) Math.floor(Math.random() * deadWorkers.size());
			index = (int) deadWorkers.toArray()[index];
			deadWorkers.remove(index);
			workers.add(createWorker(index));
		}
	}
	
	public static void create(URI controller, Integer count) {
		if (count == null) count = 10;
		UnreliableWorkers fakeWorkers = new UnreliableWorkers(controller, count);
		fakeWorkers.start();
	}
}
