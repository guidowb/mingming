package net.guidowb.mingming.controller;

import java.util.HashMap;
import java.util.Map;

import net.guidowb.mingming.model.WorkerInfo;

public class WorkerRegistry {

	private Map<String, WorkerInfo> workers = new HashMap<String, WorkerInfo>();
	
	public String register(WorkerInfo worker) {
		workers.put(worker.getId(), worker);
		return worker.getId();
	}
	
	public Iterable<WorkerInfo> list() {
		return workers.values();
	}
}
