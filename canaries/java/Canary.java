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
		public String  deploymentName;
		public String  payloadType;
		public Integer canaryInstance;
		public String  payload;

		public Payload(String payloadType, Object payload) {
			try { 
				this.deploymentName = getenv("DEPLOYMENT", "local");
				this.canaryInstance = Integer.valueOf(getenv("CF_INSTANCE_INDEX", "0"));
				this.payloadType = payloadType;
				this.payload = new ObjectMapper().writeValueAsString(payload);
			}
			catch (Exception ex) { throw new RuntimeException(ex); }
		}
		
		private String getenv(String key) { return getenv(key, null); }
		private String getenv(String key, String defaultValue) {
			String value = System.getenv(key);
			if (value == null) value = defaultValue;
			return value;
		}
	}
}
