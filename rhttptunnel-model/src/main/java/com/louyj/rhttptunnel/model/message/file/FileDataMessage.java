package com.louyj.rhttptunnel.model.message.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.annotation.NoLogFields;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@NoLogFields(values = { "data" })
public class FileDataMessage extends BaseMessage {

	private String fileName;

	private boolean start;

	private boolean end;

	private byte[] data;

	private String fileHash;

	private long totalSize;

	private long currentSize;

	@JsonCreator
	public FileDataMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public FileDataMessage(ClientInfo client, String fileName, boolean start, boolean end, byte[] data,
			String fileHash) {
		super(client);
		this.fileName = fileName;
		this.start = start;
		this.end = end;
		this.data = data;
		this.fileHash = fileHash;
	}

	public void setSize(long totalSize, long currentSize) {
		this.totalSize = totalSize;
		this.currentSize = currentSize;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public long getCurrentSize() {
		return currentSize;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean isStart() {
		return start;
	}

	public boolean isEnd() {
		return end;
	}

	public byte[] getData() {
		return data;
	}

	public String getFileHash() {
		return fileHash;
	}

}
