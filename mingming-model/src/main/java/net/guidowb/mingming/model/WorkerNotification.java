package net.guidowb.mingming.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkerNotification {

	private Date timestamp;
	private List<WorkerEvent> events = new ArrayList<WorkerEvent>();

	public Date getTimestamp() { return timestamp; }
	public Iterable<WorkerEvent> getEvents() { return events; }

	public void add(WorkerEvent event) { events.add(event); }

	public WorkerNotification() {
		this.timestamp = new Date();
	}

	public static abstract class WorkerEvent {
		public String getEventType() { return this.getClass().getSimpleName().toLowerCase(); }
	}

	public static class Refresh extends WorkerEvent {
		public Refresh(Iterable<WorkerInfo> workers) { this.workers = workers; }
		private Iterable<WorkerInfo> workers;
		public Iterable<WorkerInfo> getWorkers() { return workers; }
	}

	public static class Update extends WorkerEvent {
		public Update(Iterable<WorkerInfo> workers) { this.workers = workers; }
		private Iterable<WorkerInfo> workers;
		public Iterable<WorkerInfo> getWorkers() { return workers; }
	}
}
