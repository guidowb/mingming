package net.guidowb.mingming.work;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import net.guidowb.mingming.model.ForSerializationOnly;
import net.guidowb.mingming.model.Schedule;
import net.guidowb.mingming.model.Work;
import net.guidowb.mingming.model.WorkStatus;
import net.guidowb.mingming.model.WorkerInfo;

@Entity
public class Ping extends Work {

	private String url;
	private @Transient RestTemplate restTemplate = null;
	private @Transient PingStatus status = null;

	@Entity
	public static class PingStatus extends WorkStatus {
		Long numSuccesses;
		Long numFailures;
		Date lastAttempted;
		Date lastSucceeded;
		Date lastFailed;
		Long minElapsed = Long.MAX_VALUE;
		Long avgElapsed;
		Long maxElapsed;
		
		@ForSerializationOnly
		private PingStatus() {}
	
		public PingStatus(String workerId, String workId) {
			super(workerId, workId);
		}
	}

	@ForSerializationOnly
	private Ping() {}

	public Ping(Schedule schedule, String url) {
		super(schedule);
		this.url = url;
	}
	
	public String getUrl() { return url; }

	@Override
	public void run() {
		if (restTemplate == null) {
			SimpleClientHttpRequestFactory http = new SimpleClientHttpRequestFactory();
			http.setConnectTimeout(1 * 1000);
			http.setReadTimeout(1 * 1000);
			restTemplate = new RestTemplate(http);
		}
		try {
			Long start = System.nanoTime();
			WorkerInfo responder = restTemplate.getForObject(url, WorkerInfo.class);
			Long end = System.nanoTime();
			Long elapsed = end - start;
			recordSuccess(responder, elapsed);
		}
		catch (RestClientException exception) {
			recordFailure(exception.getMostSpecificCause().getMessage());
		}
	}

	private void recordSuccess(WorkerInfo responder, Long elapsed) {
		Date now = new Date();
		status.numSuccesses++;
		status.lastAttempted = now;
		status.lastSucceeded = now;
		
		// Exponential moving average
		Long decay = Math.min(30,  status.numSuccesses);
		status.avgElapsed = (status.avgElapsed * (decay - 1) + elapsed) / decay;
		
		if (elapsed < status.minElapsed) status.minElapsed = elapsed;
		if (elapsed > status.maxElapsed) status.maxElapsed = elapsed; 
	}
	
	private void recordFailure(String message) {
		Date now = new Date();
		status.numFailures ++;
		status.lastAttempted = now;
		status.lastFailed = now;
	}

	@Override
	public WorkStatus getStatus(String workerId) {
		if (status == null) status = new PingStatus(workerId, getId());
		return status;
	}
}
