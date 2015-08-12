package net.guidowb.mingming.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import net.guidowb.mingming.work.Ping;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	public static class Key implements Serializable {
		private static final long serialVersionUID = 1L;
		private Date timestamp;
		private String workId;
		private String canaryId;
		
		@ForSerializationOnly
		private Key() {}

		public Key(String workId, String canaryId, Date timestamp) {
			this.workId = workId;
			this.canaryId = canaryId;
			this.timestamp = timestamp;
		}
	}

	@EmbeddedId @JsonIgnore
	private Key key;

	public Date getTimestamp() { return this.key.timestamp; }
	public String getWorkId() { return this.key.workId; }
	public String getCanaryId() { return this.key.canaryId; }

	public void setTimestamp() { setTimestamp(new Date()); }
	public void setTimestamp(Date timestamp) { this.key.timestamp = timestamp; }
	public void setWorkId(String workId) { this.key.workId = workId; }
	public void setCanaryId(String canaryId) { this.key.canaryId = canaryId; }

	@ForSerializationOnly
	protected WorkStatus() { this.key = new Key(); }

	// To work around Java's type erasure when constructing generic lists
	@JsonProperty("class")
	public String getClassname() { return this.getClass().getSimpleName().toLowerCase(); }
}