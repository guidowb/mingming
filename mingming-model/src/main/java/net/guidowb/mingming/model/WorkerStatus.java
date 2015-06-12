package net.guidowb.mingming.model;

import java.util.HashMap;
import java.util.Map;

public class WorkerStatus {

	public String workerId;

	private Map<String, WorkStatus> workStatus = new HashMap<String, WorkStatus>();
	
	@ForSerializationOnly
	private WorkerStatus() {}

	public WorkerStatus(String workerId) {
		this.workerId = workerId;
	}

	public String getWorkerId() { return workerId; }
	public void setWorkerId(String workerId) { this.workerId = workerId; }

	public Iterable<WorkStatus> getWorkStatus() { return this.workStatus.values(); }
	public void addWork(Work work) { this.workStatus.put(work.getId(), work.getStatus(workerId)); }
	public void removeWork(Work work) { this.workStatus.remove(work.getId()); }

	public void setWorkStatus(Iterable<WorkStatus> list) {
		for (WorkStatus item : list) {
			workStatus.put(item.getWorkId(), item);
		}
	}
}
