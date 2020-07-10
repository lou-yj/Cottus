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
public class UncompressFileMessage extends BaseMessage {

	private String source;

	private String target;

	private String type;

	private boolean deleteSource = false;

	@JsonCreator
	public UncompressFileMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public UncompressFileMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public boolean isDeleteSource() {
		return deleteSource;
	}

	public void setDeleteSource(boolean deleteSource) {
		this.deleteSource = deleteSource;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
