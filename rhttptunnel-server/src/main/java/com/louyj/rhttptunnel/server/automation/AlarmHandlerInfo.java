package com.louyj.rhttptunnel.server.automation;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

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

}
