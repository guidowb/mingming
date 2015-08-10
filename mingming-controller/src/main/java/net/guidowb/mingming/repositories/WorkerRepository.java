package net.guidowb.mingming.repositories;

import java.util.Date;
import java.util.List;

import net.guidowb.mingming.model.WorkerInfo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface WorkerRepository extends CrudRepository<WorkerInfo, String> {

	public List<WorkerInfo> findByLastUpdateAfter(Date since);
	public List<WorkerInfo> findByLastChangeAfter(Date since);
	
	public List<WorkerInfo> findByLastUpdateBeforeAndInstanceState(Date since, String state);
}
