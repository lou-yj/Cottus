package com.louyj.rhttptunnel.model.message.server;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class TaskMetricsMessage extends ServerMessage {

	public static enum ExecuteStatus {
		SCHEDULED, SUCCESS, FAILED, REPO_NEED_UPDATE
	}

	private String name;

	private ExecuteStatus status = ExecuteStatus.SUCCESS;

	private String errorMessage;

	// script runtime environment
	private Map<String, Object> sre = Maps.newHashMap();

	private Map<String, Object> tags = Maps.newHashMap();

	private Map<String, Object> fields = Maps.newHashMap();

	private Long timestamp;

	@JsonCreator
	public TaskMetricsMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public TaskMetricsMessage(ClientInfo client, String exchangeId, String serverMsgId) {
		super(client, exchangeId, serverMsgId);
	}

	public Map<String, Object> getSre() {
		return sre;
	}

	public void setSre(Map<String, Object> sre) {
		this.sre = sre;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getTags() {
		return tags;
	}

	public void setTags(Map<String, Object> tags) {
		this.tags = tags;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public ExecuteStatus getStatus() {
		return status;
	}

	public void setStatus(ExecuteStatus status) {
		this.status = status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String format() {
		if (ExecuteStatus.SUCCESS.equals(status)) {
			String tagStr = null;
			String fieldStr = null;
			{
				List<String> items = Lists.newArrayList();
				for (Entry<String, Object> entry : tags.entrySet()) {
					items.add(entry.getKey() + "=" + entry.getValue());
				}
				tagStr = StringUtils.join(items, ",");
			}
			{
				List<String> items = Lists.newArrayList();
				for (Entry<String, Object> entry : fields.entrySet()) {
					items.add(entry.getKey() + "=" + entry.getValue());
				}
				fieldStr = StringUtils.join(items, ",");
			}
			if (StringUtils.isNotBlank(tagStr)) {
				return String.format("%s,%s %s %d", name, tagStr, fieldStr, timestamp);
			} else {
				return String.format("%s %s %d", name, fieldStr, timestamp);
			}
		} else {
			return String.format("%s %s", status.name(), errorMessage);
		}
	}

}
