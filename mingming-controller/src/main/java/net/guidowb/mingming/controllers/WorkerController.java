package net.guidowb.mingming.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.guidowb.mingming.model.Work;
import net.guidowb.mingming.model.WorkStatus;
import net.guidowb.mingming.model.WorkerNotification;
import net.guidowb.mingming.model.WorkerInfo;
import net.guidowb.mingming.repositories.StatusRepository;
import net.guidowb.mingming.repositories.WorkRepository;
import net.guidowb.mingming.repositories.WorkerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/workers")
public class WorkerController {

	@Autowired private WorkerRepository workerRepository;
	@Autowired private StatusRepository statusRepository;
	@Autowired private WorkRepository workRepository;
	private List<DeferredResult<WorkerNotification>> workerListeners = new ArrayList<DeferredResult<WorkerNotification>>();
	
	@RequestMapping(method=RequestMethod.GET)
	public Iterable<WorkerInfo> listWorkers(@RequestParam(value="since", defaultValue="30") Integer since) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.SECOND, -since);
		return workerRepository.findByLastUpdateAfter(cal.getTime());
	}

	@RequestMapping(value="/events", method=RequestMethod.GET)
	public DeferredResult<WorkerNotification> getWorkerEvents(@RequestParam(value="since", defaultValue="0") Long since) {
		WorkerNotification result = new WorkerNotification();
		DeferredResult<WorkerNotification> response = new DeferredResult<WorkerNotification>(30000L, result);
		if (since < (new Date().getTime() - 60 * 1000)) {
			// If no timestamp is provided, or the time period exceeds our event buffer time,
			// we force the client to refresh the complete data set.
			result.add(new WorkerNotification.Refresh(listWorkers(30)));
			response.setResult(result);
			return response;
		}
		List<WorkerInfo> workers = workerRepository.findByLastChangeAfter(new Date(since));
		if (!workers.isEmpty()) {
			// If we had unreported changes, return them
			result.add(new WorkerNotification.Update(workers));
			response.setResult(result);
			return response;
		}
		else {
			// No events queued up. We're going to defer the response until we have some.
			synchronized(workerListeners) {
				workerListeners.add(response);
			}
			return response;
		}
	}

	@Scheduled(fixedDelay=1000)
	public void checkWorkerState() {
		Date now = new Date();
		Date late = new Date(now.getTime() - 6 * 1000);
		Date dead = new Date(late.getTime() - 10 * 1000);
		Date gone = new Date(dead.getTime() - 10 * 1000);
		List<WorkerInfo> changedWorkers = new ArrayList<WorkerInfo>();
		List<WorkerInfo> lateWorkers = workerRepository.findByLastUpdateBeforeAndInstanceState(late, "healthy");
		for (WorkerInfo worker : lateWorkers) {
			worker.setState("late");
			changedWorkers.add(workerRepository.save(worker));
			System.err.println("Worker " + worker.getInstanceId() + " state changed to late");
		}
		List<WorkerInfo> deadWorkers = workerRepository.findByLastUpdateBeforeAndInstanceState(dead, "late");
		for (WorkerInfo worker : deadWorkers) {
			worker.setState("dead");
			changedWorkers.add(workerRepository.save(worker));
			System.err.println("Worker " + worker.getInstanceId() + " state changed to dead");
		}
		List<WorkerInfo> goneWorkers = workerRepository.findByLastUpdateBeforeAndInstanceState(gone, "dead");
		for (WorkerInfo worker : goneWorkers) {
			worker.setState("gone");
			changedWorkers.add(workerRepository.save(worker));
			System.err.println("Worker " + worker.getInstanceId() + " state changed to gone");
		}
		if (!changedWorkers.isEmpty()) notifyListeners(changedWorkers);
	}

	public void notifyListeners(WorkerInfo worker) {
		System.err.println("Worker " + worker.getInstanceId() + " state changed to " + worker.getInstanceState());
		List<WorkerInfo> workers = new ArrayList<WorkerInfo>();
		workers.add(worker);
		notifyListeners(workers);
	}

	public void notifyListeners(Iterable<WorkerInfo> workers) {
		WorkerNotification notification = new WorkerNotification();
		notification.add(new WorkerNotification.Update(workers));
		synchronized(workerListeners) {
			if (!workerListeners.isEmpty()) {
				System.err.println("Notifying " + Integer.toString(workerListeners.size()) + " listener(s)");
			}
			for (DeferredResult<WorkerNotification> listener : workerListeners) {
				listener.setResult(notification);
			}
			workerListeners.clear();
		}
	}

	@RequestMapping(value="/{workerId}", method=RequestMethod.PUT)
	public void updateWorker(@PathVariable String workerId, @RequestBody WorkerInfo info) {

		// Validate
		if (info.getId() == null) throw new ValidationException("workerId in request body must not be null");
		if (!info.getId().equals(workerId)) throw new ValidationException("workerId in request body (%s) must match the one in request path (%s)", info.getId(), workerId);

		// Determine if this represents a change state
		WorkerInfo existing = workerRepository.findOne(info.getId());
		boolean stateChange = existing == null || !existing.getInstanceState().equals("healthy");

		// Update worker info
		info.setState("healthy");
		info.setLastUpdate();
		if (stateChange) info.setLastChange();
		else info.setLastChange(existing.getLastChange());
		workerRepository.save(info);

		// Notify listeners if state changed
		if (stateChange) notifyListeners(info);

		// Update work status
		Iterable<WorkStatus> workStatusList = info.getWorkStatus();
		if (workStatusList == null) return;
		for (WorkStatus workStatus : workStatusList) {
			if (workStatus.getWorkerId()  == null) throw new ValidationException("workerId in work status must not be null");
			if (workStatus.getWorkId()    == null) throw new ValidationException("workId in work status must not be null");
			if (workStatus.getTimestamp() == null) throw new ValidationException("timestamp in work status must not be null");
			if (!workStatus.getWorkerId().equals(workerId)) throw new ValidationException("workerId in work status must match the one in request path");
		}
		statusRepository.save(info.getWorkStatus());
	}
	
	@RequestMapping(value="/{workerId}", method=RequestMethod.GET)
	public WorkerInfo getWorker(@PathVariable String workerId) {
		return workerRepository.findOne(workerId);
	}

	@RequestMapping(value="/{workerId}/work", method=RequestMethod.GET)
	public Iterable<Work> listWork(@PathVariable String workerId) {
		WorkerInfo worker = workerRepository.findOne(workerId);
		Iterable<String> assignedWork = worker.getAssignedWork();
		return workRepository.findAll(assignedWork);
	}

	@RequestMapping(value="/{workerId}/work/{workId}", method=RequestMethod.POST)
	public void assignWork(@PathVariable String workerId, @PathVariable String workId) {
		WorkerInfo worker = workerRepository.findOne(workerId);
		worker.assignWork(workId);
		workerRepository.save(worker);
	}

	@RequestMapping(value="/{workerId}/work/{workId}", method=RequestMethod.DELETE)
	public void unassignWork(@PathVariable String workerId, @PathVariable String workId) {
		WorkerInfo worker = workerRepository.findOne(workerId);
		worker.unassignWork(workId);
		workerRepository.save(worker);
	}

	@RequestMapping(value="/{workerId}/status", method=RequestMethod.GET)
	public Iterable<WorkStatus> getStatus(@PathVariable String workerId) {
		return statusRepository.findByKeyWorkerId(workerId);
	}
}
