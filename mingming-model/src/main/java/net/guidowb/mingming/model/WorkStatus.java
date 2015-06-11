package net.guidowb.mingming.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import net.guidowb.mingming.work.Ping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="class")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Ping.PingStatus.class, name = "pingstatus")
})
@Entity
public abstract class WorkStatus {
	
	@Embeddable
	public class Key implements Serializable {
		private static final long serialVersionUID = 1L;
		private Date timestamp;
		private String workId;
		private String workerId;
		
		public Key(String workId, String workerId, Date timestamp) {
			this.workId = workId;
			this.workerId = workerId;
			this.timestamp = timestamp;
		}
	}

	@EmbeddedId
	private Key key;

	public Date getTimestamp() { return this.key.timestamp; }
	public String getWorkId() { return this.key.workId; }
	public String getWorkerId() { return this.key.workerId; }

	public WorkStatus setMetadata(String workerId, String workId) {
		this.key = new Key(workId, workerId, new Date());
		return this;
	}


	// To work around Java's type erasure when constructing generic lists
	@JsonProperty("class")
	public String getClassname() { return this.getClass().getSimpleName().toLowerCase(); }
}