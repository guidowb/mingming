package net.guidowb.mingming.worker;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import net.guidowb.mingming.model.StatusReportingService;
import net.guidowb.mingming.model.Work;
import net.guidowb.mingming.model.WorkStatus;
import net.guidowb.mingming.model.WorkerInfo;

@SpringBootApplication
public class MingMingWorker implements CommandLineRunner, StatusReportingService {

	private URI controllerUri;
	private URI workerUri;
	private WorkerInfo workerInfo;
	private Map<String, Work> activeWork = new HashMap<String, Work>();
	private @Autowired Environment workerEnvironment;
	private ScheduledExecutorService updatePool = Executors.newScheduledThreadPool(1);
	private ScheduledExecutorService workerPool = Executors.newScheduledThreadPool(10);
	private RestTemplate controller = new RestTemplate();

	public String getId() { return workerInfo.getInstanceId(); }

	public static void main(String[] args) {
        SpringApplication.run(MingMingWorker.class, args);
    }

	public void run(String... args) {
		this.controllerUri = URI.create(workerEnvironment.getProperty("CONTROLLER"));
		this.workerInfo = new WorkerInfo(workerEnvironment);
		this.workerUri = controllerUri.resolve("/workers/").resolve(workerInfo.getId());
		startUpdates();
	}
	
	private void downloadInstructions() {
		Work[] newWork = controller.getForObject(workerUri + "/work", Work[].class);
		Set<String> unreferencedWork = activeWork.keySet();

		for (Work work : newWork) {
			unreferencedWork.remove(work.getId());
			if (activeWork.containsKey(work.getId())) continue;
			activeWork.put(work.getId(), work);
			work.schedule(workerPool, this);
		}
		
		for (String toDelete : unreferencedWork) {
			Work deletedWork = activeWork.remove(toDelete);
			if (deletedWork != null) {
				deletedWork.cancel();
			}
		}
	}

	@Override
	public void reportStatus(WorkStatus status) {
		status.setTimestamp();
		status.setWorkerId(workerInfo.getId());
		workerInfo.reportWorkStatus(status);
	}

	private void uploadStatus() {
		controller.put(workerUri, workerInfo);
	} 

	private Throwable updateFailure = null;

	private void reportUpdateFailure(Throwable t) {
		if (updateFailure != null && updateFailure.getMessage().equals(t.getMessage())) return;
		System.err.println("Disconnected from controller: " + t.getMessage());
		updateFailure = t;
	}

	private void reportUpdateSuccess() {
		if (updateFailure == null) return;
		updateFailure = null;
		System.err.println("Reconnected");
	}

	private void startUpdates() {
		updatePool.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					uploadStatus();
					downloadInstructions();
					reportUpdateSuccess();
				}
				catch (Throwable t) {
					// Failure to connect to the controller should be assumed to be
					// temporary and should not stop us from continuing to try.
					// But we do want to see an indication that something is wrong...
					reportUpdateFailure(t);
				}
			}	
		}, 0, 5, TimeUnit.SECONDS);
	}

}
