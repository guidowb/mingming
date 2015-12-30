package net.guidowb.mingming.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.guidowb.mingming.model.Payload;
import net.guidowb.mingming.repositories.PayloadRepository;

@RestController
@RequestMapping("/payloads")
public class PayloadController {

	@Autowired private PayloadRepository payloadRepository;
	
	@RequestMapping(method=RequestMethod.GET)
	public Iterable<Payload> getPayloads(@RequestParam(value="canaryType") String canaryType) {
		return payloadRepository.findByKeyCanaryType(canaryType);
	}

	@RequestMapping(method=RequestMethod.POST)
	public void postPayload(@RequestBody Payload payload) {

		// Validate
		if (payload.getDeploymentName() == null) throw new ValidationException("deploymentName in request body must not be null");
		if (payload.getCanaryType()     == null) throw new ValidationException("canaryType in request body must not be null");
		if (payload.getCanaryInstance() == null) throw new ValidationException("canaryInstance in request body must not be null");

		// Save
		payload.setTimestamp();
		payloadRepository.save(payload);
	}
}
