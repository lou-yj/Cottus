package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.bean.RepoConfig;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class RepoSetMessage extends BaseMessage {

	private RepoConfig repoConfig;

	@JsonCreator
	public RepoSetMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public RepoSetMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public RepoConfig getRepoConfig() {
		return repoConfig;
	}

	public void setRepoConfig(RepoConfig repoConfig) {
		this.repoConfig = repoConfig;
	}

}
