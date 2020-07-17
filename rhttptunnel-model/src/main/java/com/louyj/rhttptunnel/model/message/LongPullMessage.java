package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class LongPullMessage extends BaseMessage {

	private String cid;

	@JsonCreator
	public LongPullMessage(@JsonProperty("client") ClientInfo client, @JsonProperty("cid") String cid) {
		super(client);
		this.cid = cid;
	}

	public String getCid() {
		return cid;
	}

}
