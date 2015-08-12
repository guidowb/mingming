package net.guidowb.mingming.fakes;

import java.net.URI;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FakeCanaries {

	public static class CanaryConfig {
		@JsonProperty String  type;
		@JsonProperty Integer count;
		@JsonProperty String  space;
		@JsonProperty String  name;
	}

	public static void start(Environment env) {
	    String config = env.getProperty("FAKECANARIES");
	    if (config == null) return;
	    
	    CanaryConfig[] canaries;
	    try {
	    	ObjectMapper mapper = new ObjectMapper();
	    	try {
	    		canaries = mapper.readValue(config, CanaryConfig[].class);
	    	}
		    catch (JsonMappingException ex) {
		    	CanaryConfig canary = mapper.readValue(config, CanaryConfig.class);
		    	canaries = new CanaryConfig[1];
		    	canaries[0] = canary;
		    }
	    }
	    catch (Throwable t) {
	    	System.err.println("FAKECANARIES: " + t.getMessage());
	    	return;
	    }

	    String serverPort = env.getProperty("SERVER_PORT", "8080");
	    String serverHost = env.getProperty("vcap.application.uris[0]", "localhost:" + serverPort);
	    URI serverURI = URI.create("http://" + serverHost);
	    
	    for (CanaryConfig canary : canaries) {
	    	if (canary.type == null) {
	    		System.err.println("FAKECANARIES: Entry must specify type");
	    	}
	    	else if (canary.type.equalsIgnoreCase("reliable")) {
	    		ReliableCanaries.create(serverURI, canary.count);
	    	}
	    	else if (canary.type.equalsIgnoreCase("unreliable")) {
	    		UnreliableCanaries.create(serverURI, canary.count);
	    	}
	    	else if (canary.type.equalsIgnoreCase("disconnecting")) {
	    		DisconnectingCanaries.create(serverURI, canary.count);
	    	}
	    	else {
	    		System.err.println("FAKECANARIES: Unknown canary type: " + canary.type);
	    	}
	    }
	}
}
