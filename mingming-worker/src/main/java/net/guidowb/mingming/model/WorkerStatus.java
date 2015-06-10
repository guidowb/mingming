package net.guidowb.mingming.model;

import java.util.List;

public class WorkerStatus {

	private List<WorkStatus> workStatus;
	
	public void setWorkStatus(List<WorkStatus> status) { this.workStatus = status; }
	public List<WorkStatus> getWorkStatus() { return this.workStatus; }
}
