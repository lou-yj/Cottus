package com.louyj.rhttptunnel.model.message;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class FileDataMessage extends BaseMessage {

	private String fileName;

	private boolean start;

	private boolean end;

	private byte[] data;

	private String fileHash;

	private long totalSize;

	private long currentSize;

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
