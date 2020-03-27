package com.louyj.rhttptunnel.worker.shell;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

/**
 *
 * Created on 2020年3月25日
 *
 * @author Louyj
 *
 */
@Component
public class ShellManager {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${work.directory}")
	private String workDirectory;

	private Map<String, ShellHolder> shellHolders = Maps.newConcurrentMap();

	public synchronized ShellHolder activeShell(String clientId) throws IOException {
		ShellHolder shellHolder = shellHolders.get(clientId);
		if (shellHolder == null) {
			shellHolder = new ShellHolder(workDirectory, clientId);
			shellHolder.start();
			shellHolders.put(clientId, shellHolder);
		}
		return shellHolder;
	}

	public ShellHolder getShell(String clientId) {
		return shellHolders.get(clientId);
	}

	public synchronized void destoryShell(String clientId) {
		ShellHolder shellHolder = shellHolders.remove(clientId);
		if (shellHolder == null) {
			return;
		}
		try {
			shellHolder.close();
		} catch (IOException | InterruptedException e) {
			logger.warn("Destory shell failed", e);
		}
	}

}
