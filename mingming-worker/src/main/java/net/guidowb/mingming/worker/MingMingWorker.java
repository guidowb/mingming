package net.guidowb.mingming.worker;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import net.guidowb.mingming.model.Work;
import net.guidowb.mingming.model.WorkStatus;
import net.guidowb.mingming.model.WorkerInfo;
import net.guidowb.mingming.model.WorkerStatus;

public class MingMingWorker implements CommandLineRunner {

	private URI controllerUri;
	private URI workerUri;
	private WorkerInfo workerInfo;
	private WorkerStatus workerStatus = new WorkerStatus();
	private Map<String, Work> activeWork = new HashMap<String, Work>();
	private @Autowired Environment env;
	private ScheduledExecutorService updatePool = Executors.newScheduledThreadPool(1);
	private ScheduledExecutorService workerPool = Executors.newScheduledThreadPool(10);
	private RestTemplate controller = new RestTemplate();

	public String getId() { return workerInfo.getInstanceId(); }

	public static void main(String[] args) {
        SpringApplication.run(MingMingWorker.class, args);
    }

	private MingMingWorker() throws URISyntaxException {
		this.controllerUri = URI.create(env.getProperty("CONTROLLER"));
	}

	public void run(String... args) {
		register();
		scheduleUpdates();
	}
	
	private void register() {
		if (workerInfo == null) workerInfo = new WorkerInfo(env);
		RestTemplate controller = new RestTemplate();
		URI workersUri = controllerUri.resolve("/workers/");
		this.workerUri = workersUri.resolve(controller.postForLocation(workersUri, workerInfo));
	}

	private void updateWork() {
		Work[] newWork = controller.getForObject(workerUri.resolve("/work"), Work[].class);
		Set<String> unreferencedWork = activeWork.keySet();

		for (Work work : newWork) {
			unreferencedWork.remove(work.getId());
			if (activeWork.containsKey(work.getId())) continue;
			activeWork.put(work.getId(), work);
			work.schedule(workerPool);
		}
		
		for (String toDelete : unreferencedWork) {
			Work deletedWork = activeWork.remove(toDelete);
			if (deletedWork != null) deletedWork.cancel();
		}
	}

	private void reportStatus() {
		List<WorkStatus> workStatus = new ArrayList<WorkStatus>();
		for (Work work : activeWork.values()) {
			workStatus.add(work.getStatus().setMetadata(this.getId(), work.getId()));
		}
		workerStatus.setWorkStatus(workStatus);
		controller.put(workerUri.resolve("/status"), workerStatus);
	}

	private void scheduleUpdates() {
		updatePool.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				updateWork();
				reportStatus();
			}	
		}, 0, 5, TimeUnit.SECONDS);
	}
}
