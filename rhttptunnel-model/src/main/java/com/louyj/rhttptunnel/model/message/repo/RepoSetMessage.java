package com.louyj.rhttptunnel.model.message.repo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.bean.automate.RepoConfig;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

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
