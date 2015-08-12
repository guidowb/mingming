package net.guidowb.mingming.test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import net.guidowb.mingming.model.CanaryInfo;
import net.guidowb.mingming.model.CanaryNotification;
import net.guidowb.mingming.model.CanaryNotification.Refresh;
import net.guidowb.mingming.model.CanaryNotification.Update;
import net.guidowb.mingming.model.CanaryNotification.WorkerEvent;

public class CanaryState {

	private Long timestamp = 0L;
	private URI serverURI;

	private Map<String, CanaryInfo> canaries = new HashMap<String, CanaryInfo>();

	public CanaryState(URI serverURI) {
		this.serverURI = serverURI;
		update();
	}

	public CanaryState update() {
		RestTemplate client = new RestTemplate();
		URI eventURI = serverURI.resolve("/canaries/events?since=" + Long.toString(timestamp));
		CanaryNotification notification = client.getForObject(eventURI, CanaryNotification.class);
		timestamp = notification.getTimestamp().getTime();
		for (WorkerEvent event : notification.getEvents()) {
			if (event instanceof Refresh) {
				Refresh refresh = (Refresh) event;
				canaries.clear();
				for (CanaryInfo canary : refresh.getCanaries()) canaries.put(canary.getId(), canary);
			}
			if (event instanceof Update) {
				Update update = (Update) event;
				for (CanaryInfo canary : update.getCanaries()) canaries.put(canary.getId(), canary);
			}
		}
		return this;
	}

	public CanaryState compare() {
		CanaryState diff = new CanaryState(serverURI);
		for (String canary : canaries.keySet()) diff.canaries.remove(canary);
		return diff;
	}

	public int getCount() { return canaries.size(); }
	public Iterable<CanaryInfo> getCanaries() { return canaries.values(); }
}
