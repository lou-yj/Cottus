package com.louyj.cottus.server.automation;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.bean.automate.Executor;
import com.louyj.rhttptunnel.model.bean.automate.ExecutorTask;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage.ExecuteStatus;

/**
 *
 * Create at 2020年7月9日
 *
 * @author Louyj
 *
 */
public class ExecutorStatus {

	@QuerySqlField(index = true)
	private String scheduledId;

	@QuerySqlField(index = true)
	private Executor executor;

	@QuerySqlField
	private ExecuteStatus status = ExecuteStatus.PENDING;

	@QuerySqlField
	private List<Pair<List<ClientInfo>, ExecutorTask>> finalTasks = Lists.newArrayList();

	@QuerySqlField
	private List<ExecuteStatus> taskStatus = Lists.newArrayList();

	@QuerySqlField
	private List<Integer> taskRetrys = Lists.newArrayList();

	public ExecutorStatus(Executor executor, List<Pair<List<ClientInfo>, ExecutorTask>> finalTasks,
			String scheduledId) {
		super();
		this.scheduledId = scheduledId;
		this.executor = executor;
		this.finalTasks = finalTasks;
		finalTasks.forEach(e -> taskStatus.add(ExecuteStatus.PENDING));
		finalTasks.forEach(e -> taskRetrys.add(0));
	}

	public ExecuteStatus getStatus() {
		return status;
	}

	public void setStatus(ExecuteStatus status) {
		this.status = status;
	}

	public List<Integer> getTaskRetrys() {
		return taskRetrys;
	}

	public void setTaskRetrys(List<Integer> taskRetrys) {
		this.taskRetrys = taskRetrys;
	}

	public String getScheduledId() {
		return scheduledId;
	}

	public void setScheduledId(String scheduledId) {
		this.scheduledId = scheduledId;
	}

	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public List<Pair<List<ClientInfo>, ExecutorTask>> getFinalTasks() {
		return finalTasks;
	}

	public void setFinalTasks(List<Pair<List<ClientInfo>, ExecutorTask>> finalTasks) {
		this.finalTasks = finalTasks;
	}

	public List<ExecuteStatus> getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(List<ExecuteStatus> taskStatus) {
		this.taskStatus = taskStatus;
	}

}
