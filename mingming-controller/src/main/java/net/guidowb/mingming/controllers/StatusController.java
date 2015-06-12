package net.guidowb.mingming.controllers;

import net.guidowb.mingming.model.WorkStatus;
import net.guidowb.mingming.repositories.StatusRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/status")
public class StatusController {

	@Autowired private StatusRepository statusRepository;

	@RequestMapping(method=RequestMethod.GET)
	public Iterable<WorkStatus> listStatus() {
		return statusRepository.findAll();
	}
}
