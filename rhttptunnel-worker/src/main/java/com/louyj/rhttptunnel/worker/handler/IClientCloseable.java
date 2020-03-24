package com.louyj.rhttptunnel.worker.handler;

/**
 *
 * Created on 2020年3月24日
 *
 * @author Louyj
 *
 */
public interface IClientCloseable {

	void close(String clientId) throws Exception;

}
