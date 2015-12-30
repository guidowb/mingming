package net.guidowb.mingming.repositories;

import net.guidowb.mingming.model.Payload;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface PayloadRepository extends CrudRepository<Payload, Payload.Key>, StatusRepositoryExtension {

	public Iterable<Payload> findByKeyCanaryType(String canaryType);
}
