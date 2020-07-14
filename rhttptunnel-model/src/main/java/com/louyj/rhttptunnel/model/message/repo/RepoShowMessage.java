package com.louyj.rhttptunnel.model.message.repo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class RepoShowMessage extends BaseMessage {

	@JsonCreator
	public RepoShowMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public RepoShowMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

}
