package net.guidowb.mingming.repositories;

import net.guidowb.mingming.model.WorkerInfo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface WorkerRepository extends CrudRepository<WorkerInfo, String> {

}
