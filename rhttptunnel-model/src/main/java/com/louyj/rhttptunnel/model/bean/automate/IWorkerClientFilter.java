package com.louyj.rhttptunnel.model.bean.automate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Create at 2020年7月9日
 *
 * @author Louyj
 *
 */
public interface IWorkerClientFilter {

	List<ClientInfo> filterWorkerClients(Map<String, String> labels, Set<String> noLables);

}
