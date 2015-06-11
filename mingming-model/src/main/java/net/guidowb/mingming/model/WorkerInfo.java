package net.guidowb.mingming.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.core.env.Environment;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class WorkerInfo {

	private @Id String instanceId;
	private String  instanceHost;
	private Integer instanceIndex;
	private Integer instancePort;
	private String  applicationName;
	private String  applicationRoute;
	private @JsonIgnore Date lastUpdate;
	private @JsonIgnore @ElementCollection List<String> assignedWork = new ArrayList<String>();

	public WorkerInfo() {}

	public WorkerInfo(Environment env) {
	    this.instanceId = env.getProperty("vcap.application.instance_id", "local");
	    this.applicationName = env.getProperty("vcap.application.application_name", "unknown");
	    this.instanceIndex = Integer.valueOf(env.getProperty("vcap.application.instance_index", "0"));
	    this.applicationRoute = env.getProperty("vcap.application.uris[0]", "localhost:8080");
	    this.instanceHost = env.getProperty("CF_INSTANCE_IP", "intentionally-unknown");
	    this.instancePort = Integer.valueOf(env.getProperty("SERVER_PORT", "8080"));
	}
	
	public String getId() { return instanceId; }
	public String getInstanceId() { return instanceId; }
	public String getInstanceHost() { return instanceHost; }
	public Integer getInstanceIndex() { return instanceIndex; }
	public Integer getInstancePort() { return instancePort; }
	public String getApplicationName() { return applicationName; }
	public String getApplicationRoute() { return applicationRoute; }
	
	public Iterable<String> getAssignedWork() { return assignedWork; }
	public void assignWork(String workId) { assignedWork.add(workId); }
	public void unassignWork(String workId) { assignedWork.remove(workId); }
}
