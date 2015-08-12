package net.guidowb.mingming.repositories;

import java.util.Date;
import java.util.List;

import net.guidowb.mingming.model.CanaryInfo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface CanaryRepository extends CrudRepository<CanaryInfo, String> {

	public List<CanaryInfo> findByInstanceState(String state);
	public List<CanaryInfo> findByInstanceStateNot(String state);
	public List<CanaryInfo> findByLastChangeAfter(Date since);
	
	public List<CanaryInfo> findByLastUpdateBeforeAndInstanceState(Date since, String state);
}
