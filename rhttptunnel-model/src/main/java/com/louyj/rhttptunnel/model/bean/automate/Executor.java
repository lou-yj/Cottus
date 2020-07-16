package com.louyj.rhttptunnel.model.bean.automate;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.ScriptContentType;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.TaskType;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class Executor {

	private final static Logger logger = LoggerFactory.getLogger(Executor.class);

	public static enum ScheduleType {
		CRONJOB, MANUAL
	}

	public static enum TaskExecuteMode {
		SERIAL, PARALLEL
	}

	public static enum TaskDispatchMode {
		ANY_TARGET, ALL_TARGET
	}

	private String name;

	private ScheduleType type = ScheduleType.CRONJOB;

	private String scheduleExpression;

	private TaskExecuteMode taskExecuteMode = TaskExecuteMode.SERIAL;

	private List<ExecutorTask> tasks = Lists.newArrayList();

	private long updateTime = System.currentTimeMillis();

	public void check(File ruleFile, String repoCommitIdPath) {
		if (StringUtils.isAnyBlank(name, scheduleExpression)) {
			String message = String.format("Bad format for executor %s in file %s", name, ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
		if (CollectionUtils.isEmpty(tasks)) {
			String message = String.format("Task is empty for executor %s in file %s", name, ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
		for (ExecutorTask task : tasks) {
			task.check(name, ruleFile, repoCommitIdPath);
		}
	}

	public List<Pair<List<ClientInfo>, ExecutorTask>> parseFinalTasks(IWorkerClientFilter workerSessionManager)
			throws JsonParseException, JsonMappingException, IOException {
		List<Pair<List<ClientInfo>, ExecutorTask>> result = Lists.newArrayList();
		int index = 0;
		for (ExecutorTask task : tasks) {
			task = task.clone();
			List<ClientInfo> toWorkers = workerSessionManager.filterWorkerClients(task.getTargets(), Sets.newHashSet());
			if (toWorkers.size() <= 0) {
				logger.warn("No worker matched for executor {} task {}", name, index);
			} else {
				switch (task.getTaskDispatchMode()) {
				case ANY_TARGET:
					Random random = new Random(System.currentTimeMillis());
					int workerIndex = random.nextInt(toWorkers.size());
					toWorkers = Arrays.asList(toWorkers.get(workerIndex));
					break;
				case ALL_TARGET:
					break;
				}
			}
			if (CollectionUtils.size(task.getParams()) <= 1) {
				if (StringUtils.isBlank(task.getName())) {
					task.setName(String.valueOf(index));
				}
				result.add(Pair.of(toWorkers, task));
			} else {
				for (Map<String, Object> param : task.getParams()) {
					ExecutorTask clone = task.clone();
					clone.setParams(Arrays.asList(param));
					if (StringUtils.isBlank(clone.getName())) {
						clone.setName(String.valueOf(index));
					}
					result.add(Pair.of(toWorkers, clone));
				}
			}
			index++;
		}
		return result;
	}

	public TaskScheduleMessage toMessage(String scheduledId, ClientInfo client, ExecutorTask task, String repoCommitId,
			int taskIndex) {
		TaskScheduleMessage taskMessage = new TaskScheduleMessage(client);
		taskMessage.setExecutor(name);
		taskMessage.setName(task.getName());
		taskMessage.setCommitId(repoCommitId);
		taskMessage.setLanguage(task.getLanguage());
		if (CollectionUtils.isEmpty(task.getParams())) {
			taskMessage.setParams(Collections.emptyMap());
		} else {
			taskMessage.setParams(task.getParams().get(0));
		}
		taskMessage.setMetricsCollectType(task.getMetricsCollectType());
		taskMessage.setMetricsType(task.getMetricsType());
		taskMessage.setTimeout(task.getTimeout());
		taskMessage.setCollectStdLog(task.isCollectStdLog());
		if (StringUtils.isNotBlank(task.getScript())) {
			taskMessage.setScript(task.getScript());
			taskMessage.setScriptContentType(ScriptContentType.TEXT);
		}
		if (StringUtils.isNotBlank(task.getScriptFile())) {
			taskMessage.setScript(task.getScriptFile());
			taskMessage.setScriptContentType(ScriptContentType.FILE);
		}
		if (MapUtils.isEmpty(task.getExpected()) == false) {
			taskMessage.setExpected(task.getExpected());
		}
		taskMessage.setScheduledId(scheduledId);
		taskMessage.setType(TaskType.EXECUTOR);
		return taskMessage;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ScheduleType getType() {
		return type;
	}

	public void setType(ScheduleType type) {
		this.type = type;
	}

	public String getScheduleExpression() {
		return scheduleExpression;
	}

	public void setScheduleExpression(String scheduleExpression) {
		this.scheduleExpression = scheduleExpression;
	}

	public TaskExecuteMode getTaskExecuteMode() {
		return taskExecuteMode;
	}

	public void setTaskExecuteMode(TaskExecuteMode taskExecuteMode) {
		this.taskExecuteMode = taskExecuteMode;
	}

	public List<ExecutorTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<ExecutorTask> tasks) {
		this.tasks = tasks;
	}

}
