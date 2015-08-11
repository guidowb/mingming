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

public class DisconnectingWorkers {

	private int count;
	private int timer = 0;
	private List<WorkerInfo> workers = null;
	private ScheduledExecutorService updatePool = Executors.newScheduledThreadPool(1);
	private RestTemplate controller = new RestTemplate();
	private URI controllerURI;

	private DisconnectingWorkers(URI controller, int count) {
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
			
		}, 3, 5, TimeUnit.SECONDS);
	}

	private WorkerInfo createWorker(int instance) {
		MockEnvironment env = new MockEnvironment()
				.withProperty("vcap.application.uris[0]", "Fake Canaries")
				.withProperty("vcap.application.space_name", "dynamic test data set")
				.withProperty("vcap.application.application_name", "disconnecting-canary")
				.withProperty("vcap.application.instance_index", Integer.toString(instance));
		WorkerInfo worker = new WorkerInfo(env);
		return worker;
	}

	private synchronized void updateWorkers() {
		if (timer < 16) {
			for (WorkerInfo worker : workers) {
				URI workerURI = controllerURI.resolve("/workers/").resolve(worker.getId());
				worker.setLastUpdate();
				try { controller.put(workerURI, worker); }
				catch (Throwable t) {}
			}
		}
		timer = (timer + 5) % 60;
	}
	
	public static void create(URI controller, Integer count) {
		if (count == null) count = 10;
		DisconnectingWorkers fakeWorkers = new DisconnectingWorkers(controller, count);
		fakeWorkers.start();
	}
}
