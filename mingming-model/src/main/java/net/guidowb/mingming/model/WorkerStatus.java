package net.guidowb.mingming.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WorkerStatus {

	public String workerId;

	private @JsonIgnore Map<String, WorkStatus> workStatus = new HashMap<String, WorkStatus>();
	
	public Iterable<WorkStatus> getWorkStatus() { return this.workStatus.values(); }
	public void addWork(Work work) { this.workStatus.put(work.getId(), work.getStatus(workerId)); }
	public void removeWork(Work work) { this.workStatus.remove(work.getId()); }

	@ForSerializationOnly
	private WorkerStatus() {}

	public WorkerStatus(String workerId) {
		this.workerId = workerId;
	}
}
