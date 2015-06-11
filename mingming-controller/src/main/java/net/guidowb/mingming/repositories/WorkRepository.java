package net.guidowb.mingming.repositories;

import net.guidowb.mingming.model.Work;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface WorkRepository extends CrudRepository<Work, String> {

}
