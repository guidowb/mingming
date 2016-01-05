import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@EnableAutoConfiguration
public class Canary implements CommandLineRunner {

	public static void main(String[] args) throws Exception {
        SpringApplication.run(Canary.class, args);
    }

	public void run(String... args) {
		URI controllerUri = URI.create(System.getenv("CONTROLLER"));
		postEnvironment(controllerUri);
	}

	public void postEnvironment(URI uri) {
		RestTemplate controller = new RestTemplate();
		Payload payload = new Payload("env", System.getenv());
		controller.postForLocation(uri.resolve("/payloads"), payload);
	}

	public class Payload {
		public String deploymentName;
		public String payloadType;
		public Integer canaryInstance;
		public String payload;

		public Payload(String payloadType, Object payload) {
			try { 
				ObjectMapper mapper = new ObjectMapper();
				HashMap<String, Object> vcap_application = mapper.readValue(System.getenv("VCAP_APPLICATION"), new TypeReference<Map<String, Object>>(){});
				ArrayList<String> application_uris = (ArrayList<String>) vcap_application.get("uris");
				this.deploymentName = application_uris.get(0);
				this.canaryInstance = Integer.valueOf(vcap_application.get("instance_index").toString());
				this.payloadType = payloadType;
				this.payload = new ObjectMapper().writeValueAsString(payload);
			}
			catch (Exception ex) { throw new RuntimeException(ex); }
		}
	}
}
