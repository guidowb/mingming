package net.guidowb.mingming.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

@Entity
public abstract class WorkerEvent {

	@Id
    @Column(length=40)
    @GeneratedValue(generator="UUID")
    @GenericGenerator(name="UUID", strategy="net.guidowb.mingming.repositories.UUIDGenerator")
	protected String id;
	protected Date timestamp;
	
	public Date getTimestamp() { return timestamp; }
	public String getEventType() { return this.getClass().getSimpleName().toLowerCase(); }

	public static class Refresh extends WorkerEvent {
		public Refresh(Date timestamp, Iterable<WorkerInfo> workers) { this.timestamp = timestamp; this.workers = workers; }
		private Iterable<WorkerInfo> workers;
		public Iterable<WorkerInfo> getWorkers() { return workers; }
	}
}
