package net.guidowb.mingming.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Payload {
	
	@Embeddable
	public static class Key implements Serializable {
		private static final long serialVersionUID = 1L;
		private String deploymentName;
		private String canaryType;
		private Integer canaryInstance;
		
		@ForSerializationOnly
		private Key() {}

		public Key(String deploymentName, String canaryType, Integer canaryInstance) {
			this.deploymentName = deploymentName;
			this.canaryType = canaryType;
			this.canaryInstance = canaryInstance;
		}
	}

	@EmbeddedId @JsonIgnore
	private Key key;
	private Date timestamp;
	private String payload;

	public Date getTimestamp() { return this.timestamp; }
	public String getDeploymentName() { return this.key.deploymentName; }
	public String getCanaryType() { return this.key.canaryType; }
	public Integer getCanaryInstance() { return this.key.canaryInstance; }
	public String getPayload() { return payload; }

	public void setTimestamp() { setTimestamp(new Date()); }
	public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
	public void setDeploymentName(String deploymentName) { this.key.deploymentName = deploymentName; }
	public void setCanaryType(String canaryType) { this.key.canaryType = canaryType; }
	public void setCanaryInstance(Integer canaryInstance) { this.key.canaryInstance = canaryInstance; }
	public void setPayload(String payload) { this.payload = payload; }

	@ForSerializationOnly
	protected Payload() { this.key = new Key(); }
}