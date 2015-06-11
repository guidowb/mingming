package net.guidowb.mingming.controllers;

import java.net.URI;

import net.guidowb.mingming.model.Work;
import net.guidowb.mingming.model.WorkerInfo;
import net.guidowb.mingming.model.WorkerStatus;
import net.guidowb.mingming.repositories.StatusRepository;
import net.guidowb.mingming.repositories.WorkRepository;
import net.guidowb.mingming.repositories.WorkerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workers")
public class WorkerController {

	@Autowired private WorkerRepository workerRepository;
	@Autowired private StatusRepository statusRepository;
	@Autowired private WorkRepository workRepository;

	@RequestMapping(method=RequestMethod.POST)
	public ResponseEntity<?> registerWorker(@RequestBody WorkerInfo worker) {
		worker = workerRepository.save(worker);
		
	    HttpHeaders headers = new HttpHeaders();
	    headers.setLocation(URI.create(worker.getId()));
	    return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	@RequestMapping(method=RequestMethod.GET)
	public Iterable<WorkerInfo> listWorkers() {
		return workerRepository.findAll();
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

	@RequestMapping(value="/{workerId}/status", method=RequestMethod.PUT)
	public void reportStatus(@PathVariable String workerId, @RequestBody WorkerStatus status) {
		statusRepository.save(status.getWorkStatus());
	}
}
