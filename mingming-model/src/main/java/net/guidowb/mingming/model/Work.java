package net.guidowb.mingming.model;

import java.util.concurrent.ScheduledExecutorService;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

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
@Entity
public abstract class Work implements Runnable {

	@Id
    @Column(length=40)
    @GeneratedValue(generator="UUID")
    @GenericGenerator(name="UUID", strategy="net.guidowb.mingming.repositories.UUIDGenerator")
	private String id;

	private Schedule schedule;

	protected Work() {}

	protected Work(Schedule schedule) {
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
