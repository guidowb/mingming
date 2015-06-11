package net.guidowb.mingming.model;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduleRepeat extends Schedule {

	private Long period;
	private TimeUnit unit;
	private ScheduledFuture<?> future;

	protected ScheduleRepeat() {}

	public ScheduleRepeat(Long period, TimeUnit unit) {
		this.period = period;
		this.unit = unit;
	}

	public Long getPeriod() { return period; }
	public TimeUnit getUnit() { return unit; }

	@Override
	public void schedule(Work work, ScheduledExecutorService service) {
		future = service.scheduleAtFixedRate(work, 0, period, unit);
	}

	@Override
	public void cancel() {
		future.cancel(false);
	}
}
