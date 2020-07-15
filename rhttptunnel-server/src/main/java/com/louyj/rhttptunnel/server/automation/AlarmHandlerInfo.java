package com.louyj.rhttptunnel.server.automation;

import java.util.List;
import java.util.Map;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage.ExecuteStatus;

/**
 *
 * Create at 2020年7月9日
 *
 * @author Louyj
 *
 */
public class AlarmHandlerInfo {

	@QuerySqlField(index = true)
	private String uuid;

	@QuerySqlField(index = true)
	private String handlerId;

	@QuerySqlField(index = true)
	private String alarmId;

	@QuerySqlField(index = true)
	private boolean handled = false;

	@QuerySqlField(index = true)
	private int actionWaitCount;

	@QuerySqlField(index = true)
	private int actionAggrTime;

	@QuerySqlField(index = true)
	private Long evaluateTime;

	@QuerySqlField(index = true)
	private Pair<String, String> preventedBy;

	@QuerySqlField(index = true)
	private Long scheduledTime;

	@QuerySqlField(index = true)
	private Map<String, Object> params = Maps.newHashMap();

	@QuerySqlField(index = true)
	private List<ClientInfo> targetHosts;

	@QuerySqlField(index = true)
	private String scheduleId;

	@QuerySqlField(index = true)
	private ExecuteStatus status;

	@QuerySqlField(index = true)
	private String message;

	@QuerySqlField(index = true)
	private List<String> correlationAlarmIds;

	public int getActionWaitCount() {
		return actionWaitCount;
	}

	public void setActionWaitCount(int actionWaitCount) {
		this.actionWaitCount = actionWaitCount;
	}

	public int getActionAggrTime() {
		return actionAggrTime;
	}

	public void setActionAggrTime(int actionAggrTime) {
		this.actionAggrTime = actionAggrTime;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public boolean isHandled() {
		return handled;
	}

	public void setHandled(boolean handled) {
		this.handled = handled;
	}

	public String getHandlerId() {
		return handlerId;
	}

	public void setHandlerId(String handlerId) {
		this.handlerId = handlerId;
	}

	public String getAlarmId() {
		return alarmId;
	}

	public void setAlarmId(String alarmId) {
		this.alarmId = alarmId;
	}

	public Long getEvaluateTime() {
		return evaluateTime;
	}

	public void setEvaluateTime(Long evaluateTime) {
		this.evaluateTime = evaluateTime;
	}

	public Pair<String, String> getPreventedBy() {
		return preventedBy;
	}

	public void setPreventedBy(Pair<String, String> preventedBy) {
		this.preventedBy = preventedBy;
	}

	public Long getScheduledTime() {
		return scheduledTime;
	}

	public void setScheduledTime(Long scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public List<ClientInfo> getTargetHosts() {
		return targetHosts;
	}

	public void setTargetHosts(List<ClientInfo> targetHosts) {
		this.targetHosts = targetHosts;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public ExecuteStatus getStatus() {
		return status;
	}

	public void setStatus(ExecuteStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<String> getCorrelationAlarmIds() {
		return correlationAlarmIds;
	}

	public void setCorrelationAlarmIds(List<String> correlationAlarmIds) {
		this.correlationAlarmIds = correlationAlarmIds;
	}

}
