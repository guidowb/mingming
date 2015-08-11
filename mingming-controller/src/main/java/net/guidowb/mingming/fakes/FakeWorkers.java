package net.guidowb.mingming.fakes;

import java.net.URI;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FakeWorkers {

	public static class WorkerConfig {
		@JsonProperty String  type;
		@JsonProperty Integer count;
		@JsonProperty String  space;
		@JsonProperty String  name;
	}

	public static void start(Environment env) {
	    String config = env.getProperty("FAKEWORKERS");
	    if (config == null) return;
	    
	    WorkerConfig[] workers;
	    try {
	    	ObjectMapper mapper = new ObjectMapper();
	    	try {
	    		workers = mapper.readValue(config, WorkerConfig[].class);
	    	}
		    catch (JsonMappingException ex) {
		    	WorkerConfig worker = mapper.readValue(config, WorkerConfig.class);
		    	workers = new WorkerConfig[1];
		    	workers[0] = worker;
		    }
	    }
	    catch (Throwable t) {
	    	System.err.println("FAKEWORKERS: " + t.getMessage());
	    	return;
	    }

	    String serverPort = env.getProperty("SERVER_PORT", "8080");
	    String serverHost = env.getProperty("vcap.application.uris[0]", "localhost:" + serverPort);
	    URI serverURI = URI.create("http://" + serverHost);
	    
	    for (WorkerConfig worker : workers) {
	    	if (worker.type == null) {
	    		System.err.println("FAKEWORKERS: Entry must specify type");
	    	}
	    	else if (worker.type.equalsIgnoreCase("reliable")) {
	    		ReliableWorkers.create(serverURI, worker.count);
	    	}
	    	else if (worker.type.equalsIgnoreCase("unreliable")) {
	    		UnreliableWorkers.create(serverURI, worker.count);
	    	}
	    	else if (worker.type.equalsIgnoreCase("disconnecting")) {
	    		DisconnectingWorkers.create(serverURI, worker.count);
	    	}
	    }
	}
}
