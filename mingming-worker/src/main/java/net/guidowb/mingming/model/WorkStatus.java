package net.guidowb.mingming.model;

import net.guidowb.mingming.work.Ping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="class")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Ping.PingStatus.class, name = "pingstatus")
})
public abstract class WorkStatus {
	// To work around Java's type erasure when constructing generic lists
	@JsonProperty("class")
	public String getClassname() { return this.getClass().getSimpleName().toLowerCase(); }
}