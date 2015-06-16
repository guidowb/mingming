package net.guidowb.mingming.repositories;

import java.util.Date;

import net.guidowb.mingming.model.WorkerInfo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface WorkerRepository extends CrudRepository<WorkerInfo, String> {

	public Iterable<WorkerInfo> findByLastUpdateGreaterThan(Date since);
}
