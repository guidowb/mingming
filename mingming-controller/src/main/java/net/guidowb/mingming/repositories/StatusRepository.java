package net.guidowb.mingming.repositories;

import net.guidowb.mingming.model.WorkStatus;

import org.springframework.data.repository.CrudRepository;

public interface StatusRepository extends CrudRepository<WorkStatus, WorkStatus.Key>, StatusRepositoryExtension {
	
}
