package com.louyj.cottus.model.message.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.message.BaseMessage;
import com.louyj.cottus.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
public class FileRequestMessage extends BaseMessage {

	private boolean absolute = false;

	private String path;

	private int partSize = 1 << 10 << 10;

	@JsonCreator
	public FileRequestMessage(@JsonProperty("client") ClientInfo client, @JsonProperty("path") String path) {
		super(client);
		this.path = path;
	}

	public FileRequestMessage(ClientInfo client, boolean absolute, String path) {
		super(client);
		this.absolute = absolute;
		this.path = path;
	}

	public boolean isAbsolute() {
		return absolute;
	}

	public String getPath() {
		return path;
	}

	public int getPartSize() {
		return partSize;
	}

	public void setPartSize(int partSize) {
		this.partSize = partSize;
	}

}
