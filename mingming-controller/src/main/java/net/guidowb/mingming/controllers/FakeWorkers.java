package net.guidowb.mingming.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.mock.env.MockEnvironment;

import net.guidowb.mingming.model.WorkerInfo;

public class FakeWorkers {

	private List<WorkerInfo> workers = null;
	private static FakeWorkers instance = null;
	private int count;
	private Set<Integer> dyingWorkers = new HashSet<Integer>();
	private Set<Integer> deadWorkers = new HashSet<Integer>();
	private ScheduledExecutorService updatePool = Executors.newScheduledThreadPool(1);

	private FakeWorkers(int count) { this.count = count; }

	private WorkerInfo createWorker(int instance) {
		MockEnvironment env = new MockEnvironment()
				.withProperty("vcap.application.uris[0]", "Fake Canaries")
				.withProperty("vcap.application.space_name", "dynamic test data set")
				.withProperty("vcap.application.application_name", "fake")
				.withProperty("vcap.application.instance_index", Integer.toString(instance));
		WorkerInfo worker = new WorkerInfo(env);
		worker.setLastUpdate();
		return worker;
	}

	private synchronized void updateWorkers() {
		Set<WorkerInfo> toDelete = new HashSet<WorkerInfo>();
		if (workers == null) {
			workers = new ArrayList<WorkerInfo>();
			for (int index = 0; index < count; index++) {
				workers.add(createWorker(index));
			}
		}
		for (WorkerInfo worker : workers) {
			int index = worker.getInstanceIndex();
			if (dyingWorkers.contains(index)) {
				if (worker.secondsSinceUpdate() > 30) toDelete.add(worker);	
			}
			else worker.setLastUpdate();
		}
		for (WorkerInfo worker : toDelete) {
			int index = worker.getInstanceIndex();
			workers.remove(worker);
			deadWorkers.add(index);
			dyingWorkers.remove(index);
		}
		if (Math.random() < 0.1) {
			dyingWorkers.add((int) Math.floor(Math.random() * count));
		}
		if (!deadWorkers.isEmpty() && Math.random() < 0.2) {
			int index = (int) Math.floor(Math.random() * deadWorkers.size());
			index = (int) deadWorkers.toArray()[index];
			deadWorkers.remove(index);
			workers.add(createWorker(index));
		}
	}

	private synchronized Iterable<WorkerInfo> listWorkers() {
		if (workers == null) {
			workers = new ArrayList<WorkerInfo>();
			for (int index = 0; index < count; index++) {
				workers.add(createWorker(index));
			}
			updatePool.scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() { updateWorkers(); }
				
			}, 1, 1, TimeUnit.SECONDS);
		}
		return workers;
	}

	public static Iterable<WorkerInfo> getWorkers() {
		if (instance == null) instance = new FakeWorkers(10);
		return instance.listWorkers();
	}
}
