package net.guidowb.mingming.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.guidowb.mingming.model.Work;
import net.guidowb.mingming.model.WorkStatus;
import net.guidowb.mingming.model.CanaryNotification;
import net.guidowb.mingming.model.CanaryInfo;
import net.guidowb.mingming.repositories.StatusRepository;
import net.guidowb.mingming.repositories.WorkRepository;
import net.guidowb.mingming.repositories.CanaryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/canaries")
public class CanaryController {

	private static Logger logger = LoggerFactory.getLogger(CanaryController.class);

	@Autowired private CanaryRepository canaryRepository;
	@Autowired private StatusRepository statusRepository;
	@Autowired private WorkRepository workRepository;
	private List<DeferredResult<CanaryNotification>> canaryListeners = new ArrayList<DeferredResult<CanaryNotification>>();
	private final static Date startTime = new Date();
	
	@RequestMapping(method=RequestMethod.GET)
	public Iterable<CanaryInfo> listCanaries(@RequestParam(value="state", required=false) String state) {
		if (state != null) {
			return canaryRepository.findByInstanceState(state);
		}
		return canaryRepository.findByInstanceStateNot("gone");
	}

	private class DeferredTimeoutHandler implements Runnable {
		private DeferredResult<CanaryNotification> listener;
		private DeferredTimeoutHandler(DeferredResult<CanaryNotification> listener) { this.listener = listener; }
		@Override
		public void run() {
			synchronized(canaryListeners) {
				canaryListeners.remove(listener);
			}
		}	
	}

	@RequestMapping(value="/events", method=RequestMethod.GET)
	public DeferredResult<CanaryNotification> getCanaryEvents(@RequestParam(value="since", defaultValue="0") Long since, HttpServletRequest request) {
		CanaryNotification result = new CanaryNotification();
		DeferredResult<CanaryNotification> response = new DeferredResult<CanaryNotification>(30000L, result);
		if (since < (new Date().getTime() - 60 * 1000) || since < startTime.getTime()) {
			// If no time stamp is provided, or the time period exceeds our retention period for deleted items,
			// or it is before our most recent start, we force the client to refresh the complete data set.
			result.add(new CanaryNotification.Refresh(listCanaries(null)));
			response.setResult(result);
			return response;
		}
		List<CanaryInfo> canaries = canaryRepository.findByLastChangeAfter(new Date(since));
		if (!canaries.isEmpty()) {
			// If we had unreported changes, return them
			result.add(new CanaryNotification.Update(canaries));
			response.setResult(result);
			return response;
		}
		else {
			// No events queued up. We're going to defer the response until we have some.
			synchronized(canaryListeners) {
				response.onTimeout(new DeferredTimeoutHandler(response));
				canaryListeners.add(response);
			}
			return response;
		}
	}

	@Scheduled(fixedDelay=1000)
	public void checkCanaryState() {
		Date now = new Date();
		Date late = new Date(now.getTime() - 6 * 1000);
		Date dead = new Date(late.getTime() - 10 * 1000);
		Date gone = new Date(dead.getTime() - 10 * 1000);
		List<CanaryInfo> changedCanaries = new ArrayList<CanaryInfo>();
		List<CanaryInfo> lateCanaries = canaryRepository.findByLastUpdateBeforeAndInstanceState(late, "healthy");
		for (CanaryInfo canary : lateCanaries) {
			canary.setState("late");
			changedCanaries.add(canaryRepository.save(canary));
			logger.debug("Canary " + canary.getInstanceId() + " state changed to late");
		}
		List<CanaryInfo> deadCanaries = canaryRepository.findByLastUpdateBeforeAndInstanceState(dead, "late");
		for (CanaryInfo canary : deadCanaries) {
			canary.setState("dead");
			changedCanaries.add(canaryRepository.save(canary));
			logger.debug("Canary " + canary.getInstanceId() + " state changed to dead");
		}
		List<CanaryInfo> goneCanaries = canaryRepository.findByLastUpdateBeforeAndInstanceState(gone, "dead");
		for (CanaryInfo canary : goneCanaries) {
			canary.setState("gone");
			changedCanaries.add(canaryRepository.save(canary));
			logger.debug("Canary " + canary.getInstanceId() + " state changed to gone");
		}
		if (!changedCanaries.isEmpty()) notifyListeners(now, changedCanaries);
	}

	public void notifyListeners(CanaryInfo canary) {
		logger.debug("Canary " + canary.getInstanceId() + " state changed to " + canary.getInstanceState());
		List<CanaryInfo> canaries = new ArrayList<CanaryInfo>();
		canaries.add(canary);
		notifyListeners(canary.getLastChange(), canaries);
	}

	public void notifyListeners(Date timestamp, Iterable<CanaryInfo> canaries) {
		CanaryNotification notification = new CanaryNotification(timestamp);
		notification.add(new CanaryNotification.Update(canaries));
		synchronized(canaryListeners) {
			if (!canaryListeners.isEmpty()) {
				logger.debug("Notifying " + Integer.toString(canaryListeners.size()) + " listener(s)");
			}
			for (DeferredResult<CanaryNotification> listener : canaryListeners) {
				listener.setResult(notification);
			}
			canaryListeners.clear();
		}
	}

	@RequestMapping(value="/{canaryId}", method=RequestMethod.PUT)
	public void updateCanary(@PathVariable String canaryId, @RequestBody CanaryInfo info) {

		// Validate
		if (info.getId() == null) throw new ValidationException("canaryId in request body must not be null");
		if (!info.getId().equals(canaryId)) throw new ValidationException("canaryId in request body (%s) must match the one in request path (%s)", info.getId(), canaryId);

		// Determine if this represents a change state
		CanaryInfo existing = canaryRepository.findOne(info.getId());
		boolean stateChange = existing == null || !existing.getInstanceState().equals("healthy");

		// Update canary info
		info.setState("healthy");
		info.setLastUpdate();
		if (stateChange) info.setLastChange();
		else info.setLastChange(existing.getLastChange());
		canaryRepository.save(info);

		// Notify listeners if state changed
		if (stateChange) notifyListeners(info);

		// Update work status
		Iterable<WorkStatus> workStatusList = info.getWorkStatus();
		if (workStatusList == null) return;
		for (WorkStatus workStatus : workStatusList) {
			if (workStatus.getCanaryId()  == null) throw new ValidationException("canaryId in work status must not be null");
			if (workStatus.getWorkId()    == null) throw new ValidationException("workId in work status must not be null");
			if (workStatus.getTimestamp() == null) throw new ValidationException("timestamp in work status must not be null");
			if (!workStatus.getCanaryId().equals(canaryId)) throw new ValidationException("canaryId in work status must match the one in request path");
		}
		statusRepository.save(info.getWorkStatus());
	}
	
	@RequestMapping(value="/{canaryId}", method=RequestMethod.GET)
	public CanaryInfo getCanary(@PathVariable String canaryId) {
		return canaryRepository.findOne(canaryId);
	}

	@RequestMapping(value="/{canaryId}/work", method=RequestMethod.GET)
	public Iterable<Work> listWork(@PathVariable String canaryId) {
		CanaryInfo canary = canaryRepository.findOne(canaryId);
		Iterable<String> assignedWork = canary.getAssignedWork();
		return workRepository.findAll(assignedWork);
	}

	@RequestMapping(value="/{canaryId}/work/{workId}", method=RequestMethod.POST)
	public void assignWork(@PathVariable String canaryId, @PathVariable String workId) {
		CanaryInfo canary = canaryRepository.findOne(canaryId);
		canary.assignWork(workId);
		canaryRepository.save(canary);
	}

	@RequestMapping(value="/{canaryId}/work/{workId}", method=RequestMethod.DELETE)
	public void unassignWork(@PathVariable String canaryId, @PathVariable String workId) {
		CanaryInfo canary = canaryRepository.findOne(canaryId);
		canary.unassignWork(workId);
		canaryRepository.save(canary);
	}

	@RequestMapping(value="/{canaryId}/status", method=RequestMethod.GET)
	public Iterable<WorkStatus> getStatus(@PathVariable String canaryId) {
		return statusRepository.findByKeyCanaryId(canaryId);
	}
}
