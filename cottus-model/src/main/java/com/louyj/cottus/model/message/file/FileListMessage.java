package com.louyj.cottus.model.message.file;

import static com.louyj.cottus.model.message.ClientInfo.SERVER;

import java.util.List;

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
public class FileListMessage extends BaseMessage {

	private String path;

	private List<String> files;

	@JsonCreator
	public FileListMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public FileListMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public static FileListMessage cack(ClientInfo client, String exchangeId) {
		return new FileListMessage(client, exchangeId);
	}

	public static FileListMessage sack(String exchangeId) {
		return new FileListMessage(SERVER, exchangeId);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

}
