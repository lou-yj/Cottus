package com.louyj.rhttptunnel.model.message;

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

	public FileRequestMessage(ClientInfo client, String path) {
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
