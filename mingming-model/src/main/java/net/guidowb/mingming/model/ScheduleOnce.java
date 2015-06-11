package net.guidowb.mingming.model;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.Entity;

@Entity
public class ScheduleOnce extends Schedule {

	protected ScheduleOnce() {}

	@Override
	public void schedule(Work work, ScheduledExecutorService service) {
		service.schedule(work, 0, TimeUnit.NANOSECONDS);
	}

}
