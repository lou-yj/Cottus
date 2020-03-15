package com.louyj.rhttptunnel.model.message;

/**
 * 
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public enum MessageType {

	LONG_PULL, SLEEP,

	DISCOVER, CONNECT, EXIT, INFO, SET_CONFIG, GET_CONFIG,

	ACK, REJECT,

	FTP_MODE, SHELL_MODE,

	FTP_GET, FTP_PUT, FTP_FILE_SPLIT, FTP_FILE_MERGE, FTP_LS, FTP_PWD,

	EXEC_SHELL, EXEC_FILE

}
