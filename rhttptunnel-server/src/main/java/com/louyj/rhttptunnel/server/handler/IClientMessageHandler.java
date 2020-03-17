package com.louyj.rhttptunnel.server.handler;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public interface IClientMessageHandler extends IMessageHandler {

	default boolean asyncMode() {
		return true;
	}

}
