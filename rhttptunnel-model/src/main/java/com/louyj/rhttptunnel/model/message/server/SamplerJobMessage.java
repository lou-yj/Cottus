package com.louyj.rhttptunnel.model.message.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.bean.Sampler;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class SamplerJobMessage extends ServerMessage {

	private String commitId;

	private Sampler Sampler;

	@JsonCreator
	public SamplerJobMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public Sampler getSampler() {
		return Sampler;
	}

	public void setSampler(Sampler sampler) {
		Sampler = sampler;
	}

}
