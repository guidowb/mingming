package net.guidowb.mingming.repositories;

import net.guidowb.mingming.model.WorkerInfo;

import org.springframework.data.repository.CrudRepository;

public interface WorkerRepository extends CrudRepository<WorkerInfo, String> {

}
