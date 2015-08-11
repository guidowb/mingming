package net.guidowb.mingming.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.core.env.Environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class WorkerInfo {

	private @Id String instanceId;
	private String  instanceHost;
	private Integer instanceIndex;
	private Integer instancePort;
	private String  instanceState;
	private String  applicationName;
	private String  applicationRoute;
	private String  spaceId;
	private String  spaceName;
	private Date    lastUpdate;
	private Date    lastChange;
	private Date    created;
	private @JsonIgnore @ElementCollection List<String> assignedWork = new ArrayList<String>();
	private @Transient Map<String, WorkStatus> workStatus = new HashMap<String, WorkStatus>();

	public WorkerInfo() {}

	public WorkerInfo(Environment env) {
	    this.instanceId = env.getProperty("vcap.application.instance_id", UUID.randomUUID().toString());
	    this.applicationName = env.getProperty("vcap.application.application_name", "unknown");
	    this.instanceIndex = Integer.valueOf(env.getProperty("vcap.application.instance_index", "0"));
	    this.applicationRoute = env.getProperty("vcap.application.uris[0]", "localhost:8080");
	    this.instanceHost = env.getProperty("CF_INSTANCE_IP", "intentionally-unknown");
	    this.instancePort = Integer.valueOf(env.getProperty("SERVER_PORT", "8080"));
	    this.spaceId = env.getProperty("vcap.application.space_id", "unknown");
	    this.spaceName = env.getProperty("vcap.application.space_name", "unknown");
	    this.created = new Date();
	    this.instanceState = "healthy";
	}
	
	public String getId() { return instanceId; }
	public String getInstanceId() { return instanceId; }
	public String getInstanceHost() { return instanceHost; }
	public Integer getInstanceIndex() { return instanceIndex; }
	public Integer getInstancePort() { return instancePort; }
	public String getInstanceState() { return instanceState != null ? instanceState : "healthy"; }
	public String getApplicationName() { return applicationName; }
	public String getApplicationRoute() { return applicationRoute; }
	public String getSpaceId() { return spaceId; }
	public String getSpaceName() { return spaceName; }
	public Date getLastUpdate() { return lastUpdate; }
	public Date getLastChange() { return lastChange; }
	public Date getCreated() { return created; }
	public @JsonProperty Long secondsSinceUpdate() { return (new Date().getTime() - lastUpdate.getTime()) / 1000; }
	public @JsonProperty Long secondsSinceCreation() { return (new Date().getTime() - created.getTime()) / 1000; }

	public Iterable<String> getAssignedWork() { return assignedWork; }
	public void assignWork(String workId) { assignedWork.add(workId); }
	public void unassignWork(String workId) { assignedWork.remove(workId); }
	
	public void reportWorkStatus(WorkStatus status) { workStatus.put(status.getWorkId(), status); }
	public void setWorkStatus(Iterable<WorkStatus> list) {
		if (list == null) return;
		for (WorkStatus item : list) {
			workStatus.put(item.getWorkId(), item);
		}
	}
	public Iterable<WorkStatus> getWorkStatus() { return this.workStatus.size() == 0 ? null : this.workStatus.values(); }

	public void setLastUpdate() { lastUpdate = new Date(); }
	public void setLastChange() { setLastChange(new Date()); }
	public void setLastChange(Date change) { lastChange = change; }
	public void setState(String state) {
		if (!state.equals(this.instanceState)) setLastChange(); 
		this.instanceState = state;
	}
}
