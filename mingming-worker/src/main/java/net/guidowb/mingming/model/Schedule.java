package net.guidowb.mingming.model;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="class")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ScheduleOnce.class, name = "once"),
    @JsonSubTypes.Type(value = ScheduleRepeat.class, name = "repeat")
})
public abstract class Schedule {

	protected abstract void schedule(Work work, ScheduledExecutorService service);
	protected void cancel() {};
	
	public static ScheduleOnce once() { return new ScheduleOnce(); }
	public static ScheduleRepeat repeat(Long period, TimeUnit unit) { return new ScheduleRepeat(period, unit); }

	// To work around Java's type erasure when constructing generic lists
	@JsonProperty("class")
	public String getClassname() { return this.getClass().getSimpleName().toLowerCase(); }
}
