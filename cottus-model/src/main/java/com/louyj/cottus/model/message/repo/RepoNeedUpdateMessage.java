package com.louyj.cottus.model.message.repo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.message.BaseMessage;
import com.louyj.cottus.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class RepoNeedUpdateMessage extends BaseMessage {

	@JsonCreator
	public RepoNeedUpdateMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public RepoNeedUpdateMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

}
