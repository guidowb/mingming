package net.guidowb.mingming.repositories;

import net.guidowb.mingming.model.WorkStatus;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface StatusRepository extends CrudRepository<WorkStatus, WorkStatus.Key>, StatusRepositoryExtension {

	public Iterable<WorkStatus> findByKeyCanaryId(String canaryId);
	public Iterable<WorkStatus> findByKeyWorkId(String workId);
}
