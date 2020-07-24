package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.annotation.NoPermissionCheck;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@NoPermissionCheck
public class RsaExchangeMessage extends BaseMessage {

	private String publicKey;

	@JsonCreator
	public RsaExchangeMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public RsaExchangeMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

}
