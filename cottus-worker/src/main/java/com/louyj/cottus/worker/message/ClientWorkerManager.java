package com.louyj.cottus.worker.message;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.cottus.worker.shell.ShellManager;

/**
 *
 * Created on 2020年3月23日
 *
 * @author Louyj
 *
 */
@Component
public class ClientWorkerManager {

	@Autowired
	private MessageExchanger messageExchanger;

	@Autowired
	private ShellManager shellManager;

	private Map<String, ClientWorker> workers = Maps.newConcurrentMap();

	public List<String> clientIds() {
		return Lists.newArrayList(workers.keySet());
	}

	public void ensureThreads(Collection<String> ids) {
		Set<String> clientIds = Sets.newHashSet(ids);
		for (String clientId : clientIds) {
			if (workers.containsKey(clientId) == false) {
				ClientWorker worker = new ClientWorker(clientId, messageExchanger, shellManager);
				worker.start();
				workers.put(clientId, worker);
			}
		}
		for (String clientId : workers.keySet()) {
			if (clientIds.contains(clientId) == false) {
				ClientWorker threadWorker = workers.get(clientId);
				threadWorker.setShouldBreak(true);
				workers.remove(clientId);
			}
		}
	}

}
