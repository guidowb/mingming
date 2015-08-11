package net.guidowb.mingming.fakes;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.client.RestTemplate;

import net.guidowb.mingming.model.WorkerInfo;

public class ReliableWorkers {

	private int count;
	private List<WorkerInfo> workers = null;
	private ScheduledExecutorService updatePool = Executors.newScheduledThreadPool(1);
	private RestTemplate controller = new RestTemplate();
	private URI controllerURI;

	private ReliableWorkers(URI controller, int count) {
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
			
		}, 1, 5, TimeUnit.SECONDS);
	}

	private WorkerInfo createWorker(int instance) {
		MockEnvironment env = new MockEnvironment()
				.withProperty("vcap.application.uris[0]", "Fake Canaries")
				.withProperty("vcap.application.space_name", "dynamic test data set")
				.withProperty("vcap.application.application_name", "reliable-canary")
				.withProperty("vcap.application.instance_index", Integer.toString(instance));
		WorkerInfo worker = new WorkerInfo(env);
		worker.setLastUpdate();
		return worker;
	}

	private synchronized void updateWorkers() {
		for (WorkerInfo worker : workers) {
			URI workerURI = controllerURI.resolve("/workers/").resolve(worker.getId());
			try { controller.put(workerURI, worker); }
			catch (Throwable t) {}
		}
	}
	
	public static void create(URI controller, Integer count) {
		if (count == null) count = 10;
		ReliableWorkers fakeWorkers = new ReliableWorkers(controller, count);
		fakeWorkers.start();
	}
}
