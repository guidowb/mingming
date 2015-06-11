package net.guidowb.mingming.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.guidowb.mingming.model.Work;

public class WorkRegistry {

	private Map<String, Work> workById = new HashMap<String, Work>();
	private Map<String, Map<String, Work>> workByWorker = new HashMap<String, Map<String, Work>>();
	private final List<Work> noWork = new ArrayList<Work>();

	public String define(Work work) {
		String id = work.getId();
		workById.put(id, work);
		return id;
	}
	
	public void assign(Work work, String workerId) {
		Map<String, Work> workForWorker = workByWorker.get(workerId);
		if (workForWorker == null) {
			workForWorker = new HashMap<String, Work>();
			workByWorker.put(workerId, workForWorker);
		}
		workForWorker.put(work.getId(), work);
	}
	
	public Iterable<Work> workForWorker(String workerId) {
		Map<String, Work> work = workByWorker.get(workerId);
		if (work != null) return workByWorker.get(workerId).values();
		else return noWork;
	}
}
