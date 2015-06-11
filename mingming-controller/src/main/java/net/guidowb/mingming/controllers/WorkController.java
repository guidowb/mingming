package net.guidowb.mingming.controllers;

import java.net.URI;
import net.guidowb.mingming.model.Work;
import net.guidowb.mingming.repositories.WorkRepository;
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
@RequestMapping("/work")
public class WorkController {

	@Autowired private WorkRepository workRepository;

	@RequestMapping(method=RequestMethod.POST)
	public ResponseEntity<?> createWork(@RequestBody Work work) {
		work = workRepository.save(work);
		
	    HttpHeaders headers = new HttpHeaders();
	    headers.setLocation(URI.create(work.getId()));
	    return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}


	@RequestMapping(method=RequestMethod.GET)
	public Iterable<Work> listWork() {
		return workRepository.findAll();
	}

	@RequestMapping(value="/{workId}", method=RequestMethod.GET)
	public Work getWork(@PathVariable String workId) {
		return workRepository.findOne(workId);
	}
}
