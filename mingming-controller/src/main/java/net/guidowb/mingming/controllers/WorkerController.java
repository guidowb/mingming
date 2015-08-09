package net.guidowb.mingming.controllers;

import java.util.Calendar;
import java.util.Date;

import net.guidowb.mingming.model.Work;
import net.guidowb.mingming.model.WorkStatus;
import net.guidowb.mingming.model.WorkerInfo;
import net.guidowb.mingming.repositories.StatusRepository;
import net.guidowb.mingming.repositories.WorkRepository;
import net.guidowb.mingming.repositories.WorkerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workers")
public class WorkerController {

	@Autowired private WorkerRepository workerRepository;
	@Autowired private StatusRepository statusRepository;
	@Autowired private WorkRepository workRepository;
	@Autowired private Environment env;

	@RequestMapping(method=RequestMethod.GET)
	public Iterable<WorkerInfo> listWorkers(@RequestParam(value="since", defaultValue="30") Integer since) {
		if (env.containsProperty("FAKEWORKERS")) return FakeWorkers.getWorkers();
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.SECOND, -since);
		return workerRepository.findByLastUpdateGreaterThan(cal.getTime());
	}

	@RequestMapping(value="/{workerId}", method=RequestMethod.PUT)
	public void updateWorker(@PathVariable String workerId, @RequestBody WorkerInfo info) {
		if (info.getId() == null) throw new ValidationException("workerId in request body must not be null");
		if (!info.getId().equals(workerId)) throw new ValidationException("workerId in request body (%s) must match the one in request path (%s)", info.getId(), workerId);
		info.setLastUpdate();
		workerRepository.save(info);
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
