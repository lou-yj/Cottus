package com.louyj.rhttptunnel.worker.shell;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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

	@Value("${work.directory}")
	private String workDirectory;

	private Map<String, ShellWrapper> shellHolders = Maps.newConcurrentMap();

	public synchronized ShellWrapper activeShell(String clientId) throws IOException, InterruptedException {
		ShellWrapper shellHolder = shellHolders.get(clientId);
		if (shellHolder == null || shellHolder.isAlive() == false) {
			IOUtils.closeQuietly(shellHolder);
			shellHolder = new ShellWrapper(workDirectory);
			shellHolder.setup();
			shellHolders.put(clientId, shellHolder);
		}
		return shellHolder;
	}

	public ShellWrapper getShell(String clientId) {
		return shellHolders.get(clientId);
	}

	public synchronized void destoryShell(String clientId) {
		ShellWrapper shellHolder = shellHolders.remove(clientId);
		if (shellHolder == null) {
			return;
		}
		shellHolder.close();
	}

}
