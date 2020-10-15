package com.louyj.cottus.server.workerlabel;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import com.louyj.cottus.server.IgniteRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.util.JsonUtils;

@Component
public class WorkerLabelManager {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private IgniteRegistry igniteRegistry;

	private IgniteCache<Object, Object> cache;

	@PostConstruct
	public void setup() {
		cache = igniteRegistry.getOrCreateCache("workerlabels", String.class, LabelRule.class);
	}

	public void registryRule(LabelRule rule) {
		if (rule.getHostInfo() == null
				|| StringUtils.isAnyBlank(rule.getHostInfo().getHost(), rule.getHostInfo().getIp())) {
			logger.warn("Bad label rule {}", JsonUtils.gson().toJson(rule));
		}
		String identify = rule.getHostInfo().identify();
		LabelRule oldRule = (LabelRule) cache.get(identify);
		if (oldRule == null) {
			cache.put(identify, rule);
		} else {
			oldRule.getLabels().putAll(rule.getLabels());
			cache.put(identify, oldRule);
		}
	}

	public LabelRule findRule(ClientInfo clientInfo) {
		HostInfo hostInfo = new HostInfo(clientInfo.getHost(), clientInfo.getIp());
		String id = hostInfo.identify();
		LabelRule rule = (LabelRule) cache.get(id);
		if (rule != null) {
			return rule;
		}
		rule = new LabelRule();
		rule.setHostInfo(hostInfo);
		return rule;
	}

	@SuppressWarnings("unchecked")
	public List<HostInfo> findHostsByLabels(Map<String, String> searchlabels) {
		SqlFieldsQuery sql = new SqlFieldsQuery("SELECT hostInfo,labels FROM LabelRule");
		List<HostInfo> result = Lists.newArrayList();
		try (QueryCursor<List<?>> cursor = cache.query(sql)) {
			for (List<?> row : cursor) {
				Map<String, String> labels = (Map<String, String>) row.get(1);
				if (mapContains(labels, searchlabels)) {
					result.add((HostInfo) row.get(0));
				}
			}
		}
		return result;
	}

	private boolean mapContains(Map<String, String> labels, Map<String, String> searchlabels) {
		for (Entry<String, String> entry : searchlabels.entrySet()) {
			String labelValue = labels.get(entry.getKey());
			if (StringUtils.equals(labelValue, entry.getValue()) == false) {
				return false;
			}
		}
		return true;
	}

}
