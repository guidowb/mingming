package net.guidowb.mingming.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.guidowb.mingming.model.Schedule;
import net.guidowb.mingming.model.Work;
import net.guidowb.mingming.model.WorkerInfo;
import net.guidowb.mingming.model.WorkerStatus;
import net.guidowb.mingming.repositories.StatusRepository;
import net.guidowb.mingming.repositories.WorkRepository;
import net.guidowb.mingming.repositories.WorkerRepository;
import net.guidowb.mingming.work.Ping;

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
public class ControllerEndpoints {

	@Autowired private WorkerRepository workerRepository;
	@Autowired private WorkRepository workRepository;
	@Autowired private StatusRepository statusRepository;

	@RequestMapping(value="/workers", method=RequestMethod.POST)
	public ResponseEntity<?> registerWorker(@RequestBody WorkerInfo worker) {
		worker = workerRepository.save(worker);
		
	    HttpHeaders headers = new HttpHeaders();
	    headers.setLocation(URI.create(worker.getId()));
	    return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	@RequestMapping(value="/workers", method=RequestMethod.GET)
	public Iterable<WorkerInfo> listWorkers() {
		return workerRepository.findAll();
	}

	@RequestMapping(value="/workers/{workerId}", method=RequestMethod.GET)
	public WorkerInfo getWorker(@PathVariable String workerId) {
		return workerRepository.findOne(workerId);
	}

	@RequestMapping(value="/workers/{workerId}/work", method=RequestMethod.GET)
	public Iterable<Work> getInstructions(@PathVariable String workerId) {
		WorkerInfo worker = workerRepository.findOne(workerId);
		return worker.getAssignedWork();
	}

	@RequestMapping(value="/workers/{workerId}/status", method=RequestMethod.PUT)
	public void reportStatus(@PathVariable String workerId, @RequestBody WorkerStatus status) {
		statusRepository.save(status.getWorkStatus());
	}

	@RequestMapping(value="/work", method=RequestMethod.GET)
	public Iterable<Work> getWork() {
		List<Work> result = new ArrayList<Work>();
		result.add(new Ping(Schedule.once(), "blah1"));
		result.add(new Ping(Schedule.repeat(5L, TimeUnit.SECONDS), "blah2"));
		return result;
	}

}
