package com.louyj.cottus.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.annotation.NoPermissionCheck;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@NoPermissionCheck
public class SecurityMessage extends BaseMessage {

	private String aesKey;

	@JsonCreator
	public SecurityMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public SecurityMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public String getAesKey() {
		return aesKey;
	}

	public void setAesKey(String aesKey) {
		this.aesKey = aesKey;
	}

}
