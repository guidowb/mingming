package net.guidowb.mingming.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class CanaryNotification {

	private Date timestamp;
	private List<WorkerEvent> events = new ArrayList<WorkerEvent>();

	public Date getTimestamp() { return timestamp; }
	public Iterable<WorkerEvent> getEvents() { return events; }

	public void add(WorkerEvent event) { events.add(event); }

	public CanaryNotification() {
		this.timestamp = new Date();
	}

	@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="class")
	@JsonSubTypes({
	    @JsonSubTypes.Type(value = Refresh.class, name = "refresh"),
	    @JsonSubTypes.Type(value = Update.class,  name = "update")
	})
	public static abstract class WorkerEvent {
		public String getEventType() { return this.getClass().getSimpleName().toLowerCase(); }

		// To work around Java's type erasure when constructing generic lists
		@JsonProperty("class")
		public String getClassname() { return this.getClass().getSimpleName().toLowerCase(); }
	}

	public static class Refresh extends WorkerEvent {
		@ForSerializationOnly private Refresh() {}
		public Refresh(Iterable<CanaryInfo> canaries) { this.canaries = canaries; }
		private Iterable<CanaryInfo> canaries;
		public Iterable<CanaryInfo> getCanaries() { return canaries; }
	}

	public static class Update extends WorkerEvent {
		@ForSerializationOnly private Update() {}
		public Update(Iterable<CanaryInfo> canaries) { this.canaries = canaries; }
		private Iterable<CanaryInfo> canaries;
		public Iterable<CanaryInfo> getCanaries() { return canaries; }
	}
}
