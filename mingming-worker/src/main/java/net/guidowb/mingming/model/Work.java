package net.guidowb.mingming.model;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import net.guidowb.mingming.work.Ping;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="class")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Work.class, name = "work"),
    @JsonSubTypes.Type(value = Ping.class, name = "ping")
})
public abstract class Work implements Runnable {

	private String id;
	private Schedule schedule;

	protected Work() {}

	protected Work(Schedule schedule) {
		this.id = UUID.randomUUID().toString();
		this.schedule = schedule;
	}
	
	public String getId() { return id; }
	public Schedule getSchedule() { return schedule; }
	public @JsonIgnore abstract WorkStatus getStatus();

	public void schedule(ScheduledExecutorService service) {
		schedule.schedule(this, service);
	}
	
	public void cancel() {
		schedule.cancel();
	}

	// To work around Java's type erasure when constructing generic lists
	@JsonProperty("class")
	public String getClassname() { return this.getClass().getSimpleName().toLowerCase(); }
}
