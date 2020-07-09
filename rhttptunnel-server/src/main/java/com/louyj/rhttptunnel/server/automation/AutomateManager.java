package com.louyj.rhttptunnel.server.automation;

import static com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage.ExecuteStatus.SCHEDULED;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.configuration.CacheConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.bean.automate.Alarmer;
import com.louyj.rhttptunnel.model.bean.automate.Executor;
import com.louyj.rhttptunnel.model.bean.automate.ExecutorTask;
import com.louyj.rhttptunnel.model.bean.automate.Handler;
import com.louyj.rhttptunnel.model.bean.automate.RepoConfig;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.server.ServerMessage;
import com.louyj.rhttptunnel.model.message.server.TaskAckMessage;
import com.louyj.rhttptunnel.model.message.server.TaskLogMessage;
import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage;
import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage.ExecuteStatus;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.server.SystemClient;
import com.louyj.rhttptunnel.server.SystemClient.SystemClientListener;
import com.louyj.rhttptunnel.server.automation.event.ExecStatusEvent;
import com.louyj.rhttptunnel.server.automation.event.MetricsEvent;
import com.louyj.rhttptunnel.server.session.WorkerSessionManager;
import com.louyj.rhttptunnel.server.workerlabel.LabelRule;
import com.louyj.rhttptunnel.server.workerlabel.WorkerLabelManager;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
@Component
public class AutomateManager implements SystemClientListener {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final String CONFIG_REPO = "config:repo";
	private static final String AUTOMATE_EXECUTOR = "automate:executor";
	private static final String AUTOMATE_ALARMER = "automate:alarmer";
	private static final String AUTOMATE_HANDLER = "automate:handler";
	private static final String EXEC_HOST = "HOST";
	private static final String EXEC_IP = "IP";

	@Value("${data.dir:/data}")
	private String dataDir;
	@Autowired
	private Ignite ignite;
	@Autowired
	private SystemClient systemClient;
	@Autowired
	private WorkerSessionManager workerSessionManager;
	@Autowired
	private WorkerLabelManager workerLabelManager;

	// repo
	private RepoConfig repoConfig;
	private String repoCommitId;

	// cache
	private IgniteCache<Object, Object> configCache;
	private IgniteCache<Object, Object> auditCache;
	private IgniteCache<Object, Object> scheduleStatusCache;

	private List<Executor> executors = Lists.newArrayList();
	private List<Alarmer> alarmers = Lists.newArrayList();
	private List<Handler> handlers = Lists.newArrayList();
	private AlarmService alarmService;
	private ThreadPoolTaskScheduler taskScheduler;

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		alarmService = new AlarmService();
		taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(10);
		configCache = ignite.getOrCreateCache("automate");
		auditCache = ignite.getOrCreateCache(new CacheConfiguration<>().setName("automateAudit")
				.setIndexedTypes(String.class, ScheduledTaskAudit.class));
		scheduleStatusCache = ignite.getOrCreateCache(new CacheConfiguration<>().setName("automateScheduleStatus")
				.setIndexedTypes(String.class, ExecutorStatus.class));
		this.repoConfig = (RepoConfig) configCache.get(CONFIG_REPO);
		this.executors = (List<Executor>) configCache.get(AUTOMATE_EXECUTOR);
		this.alarmers = (List<Alarmer>) configCache.get(AUTOMATE_ALARMER);
		this.handlers = (List<Handler>) configCache.get(AUTOMATE_HANDLER);
	}

	public void updateRepoConfig(RepoConfig repoConfig) {
		setRepoConfig(repoConfig);
		configCache.put(CONFIG_REPO, repoConfig);
	}

	public void updateRules(List<Executor> samplers, List<Alarmer> rules, List<Handler> handlers) {
		this.executors = samplers;
		this.alarmers = rules;
		this.handlers = handlers;
		configCache.put(AUTOMATE_EXECUTOR, samplers);
		configCache.put(AUTOMATE_ALARMER, rules);
		configCache.put(AUTOMATE_HANDLER, handlers);
		updateSchedulers();
		updateAlarmers();
	}

	public void scheduleExecutorTask(Executor executor) throws JsonParseException, JsonMappingException, IOException {
		String scheduledId = DateTime.now().toString("yyMMddHHmmssSSS");
		logger.info("Start schedule executor {} task execute mode {} schedule id {}", executor.getName(),
				executor.getTaskExecuteMode(), scheduledId);
		List<Pair<List<ClientInfo>, ExecutorTask>> finalTasks = executor.parseFinalTasks(workerSessionManager);
		ExecutorStatus executorStatus = new ExecutorStatus(executor, finalTasks, scheduledId);
		logger.info("Total {} tasks for executor {}", finalTasks.size(), executor.getName());
		scheduleNextTask(executorStatus);
	}

	public String getRepoCommitId() {
		return repoCommitId;
	}

	public void setRepoCommitId(String repoCommitId) {
		this.repoCommitId = repoCommitId;
	}

	public RepoConfig getRepoConfig() {
		return repoConfig;
	}

	public void setRepoConfig(RepoConfig repoConfig) {
		this.repoConfig = repoConfig;
	}

	public String getDataDir() {
		return dataDir;
	}

	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	@Override
	public List<Class<? extends BaseMessage>> listenSendMessages() {
		return Arrays.asList(TaskScheduleMessage.class);
	}

	@Override
	public List<Class<? extends BaseMessage>> listenReceiveMessages() {
		return Arrays.asList(TaskMetricsMessage.class, TaskLogMessage.class, TaskAckMessage.class);
	}

	@Override
	public void onSendMessage(BaseMessage message, List<ClientInfo> toWorkers) {
		if (message instanceof TaskScheduleMessage) {
			TaskScheduleMessage taskMessage = (TaskScheduleMessage) message;
			long millis = DateTime.now().getMillis();
			for (ClientInfo toWorker : toWorkers) {
				ScheduledTaskAudit taskAudit = new ScheduledTaskAudit();
				taskAudit.setExecutor(taskMessage.getExecutor());
				taskAudit.setName(taskMessage.getName());
				taskAudit.setTime(millis);
				taskAudit.getSre().put(EXEC_HOST, toWorker.getHost());
				taskAudit.getSre().put(EXEC_IP, toWorker.getIp());
				taskAudit.setStatus(ExecuteStatus.SCHEDULED);
				taskAudit.setParams(taskMessage.getParams());
				taskAudit.setScheduleId(taskMessage.getScheduledId());
				String key = auditTaskKey(taskMessage, toWorker);
				auditCache.put(key, taskAudit);
			}
		}
	}

	@Override
	public void onReceiveMessage(BaseMessage message) {
		if (message instanceof TaskMetricsMessage) {
			TaskMetricsMessage metricsMessage = (TaskMetricsMessage) message;
			ClientInfo toWorker = metricsMessage.getClient();
			String key = auditTaskKey(metricsMessage, toWorker);
			ScheduledTaskAudit taskAudit = (ScheduledTaskAudit) auditCache.get(key);
			if (taskAudit == null) {
				logger.warn("Executor audit is null for {}", JsonUtils.gson().toJson(message));
				return;
			}
			taskAudit.getMetrics().add(metricsMessage.format());
			auditCache.put(key, taskAudit);
			alarmService.sendEvent(MetricsEvent.make(taskAudit, metricsMessage));
		} else if (message instanceof TaskLogMessage) {
			TaskLogMessage logMessage = (TaskLogMessage) message;
			ClientInfo toWorker = logMessage.getClient();
			String key = auditTaskKey(logMessage, toWorker);
			ScheduledTaskAudit taskAudit = (ScheduledTaskAudit) auditCache.get(key);
			if (taskAudit == null) {
				logger.warn("Executor audit is null for {}", JsonUtils.gson().toJson(message));
				return;
			}
			taskAudit.setStdout(logMessage.getOut());
			taskAudit.setStderr(logMessage.getErr());
			auditCache.put(key, taskAudit);
		} else if (message instanceof TaskAckMessage) {
			TaskAckMessage ackMessage = (TaskAckMessage) message;
			ClientInfo toWorker = ackMessage.getClient();
			String key = auditTaskKey(ackMessage, toWorker);
			ScheduledTaskAudit taskAudit = (ScheduledTaskAudit) auditCache.get(key);
			if (taskAudit == null) {
				logger.warn("Executor audit is null for {}", JsonUtils.gson().toJson(message));
				return;
			}
			taskAudit.setStatus(ackMessage.getStatus());
			taskAudit.setMessage(ackMessage.getMessage());
			auditCache.put(key, taskAudit);
			alarmService.sendEvent(ExecStatusEvent.make(taskAudit, ackMessage));
			String scheduleStatusKey = scheduleStatusKey(taskAudit.getExecutor(), taskAudit.getScheduleId());
			ExecutorStatus executorStatus = (ExecutorStatus) scheduleStatusCache.get(scheduleStatusKey);
			scheduleNextTask(executorStatus);
		}
	}

	private void updateSchedulers() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(10);
		for (Executor executor : executors) {
			String schedule = executor.getScheduleExpression();
			SamplerScheduleTask task = new SamplerScheduleTask(executor, this);
			taskScheduler.schedule(task, new CronTrigger(schedule));
		}
		this.taskScheduler = taskScheduler;
	}

	private void updateAlarmers() {
		alarmService.resetEsper(alarmers);
	}

	private void scheduleOneTask(Executor executor, Pair<List<ClientInfo>, ExecutorTask> pair, int taskIndex,
			ExecutorStatus executorStatus) {
		List<ClientInfo> toWorkers = pair.getLeft();
		ExecutorTask task = pair.getRight();
		List<String> workerText = Lists.newArrayList();
		toWorkers.forEach(e -> workerText.add(e.getHost()));
		logger.info("Start schedule task {} executor {}, matched workers {}", taskIndex, executor.getName(),
				workerText);
		TaskScheduleMessage taskMessage = executor.toMessage(executorStatus.getScheduledId(),
				systemClient.session().getClientInfo(), task, repoCommitId, taskIndex);
		for (ClientInfo toWorker : toWorkers) {
			LabelRule labelRule = workerLabelManager.findRule(toWorker);
			if (labelRule != null) {
				taskMessage.setLabels(labelRule.getLabels());
			} else {
				taskMessage.setLabels(Maps.newHashMap());
			}
			systemClient.exchange(taskMessage, Arrays.asList(toWorker));
		}
		executorStatus.getTaskStatus().set(taskIndex, SCHEDULED);
		executorStatus.setStatus(SCHEDULED);
		taskIndex++;
	}

	private String auditTaskKey(ServerMessage message, ClientInfo toWorker) {
		return "audit:task:" + message.getServerMsgId() + ":" + toWorker.getHost() + ":" + toWorker.getIp();
	}

	private String scheduleStatusKey(String executorName, String scheduleId) {
		return "executor:schedule:status:" + executorName + ":" + scheduleId;
	}

	private void scheduleNextTask(ExecutorStatus executorStatus) {
		if (executorStatus.getStatus().isFailed()) {
			logger.warn("Executor {} with schedule id {} has already failed with status {}",
					executorStatus.getExecutor().getName(), executorStatus.getScheduledId(),
					executorStatus.getStatus());
			return;
		}
		Executor executor = executorStatus.getExecutor();
		List<Pair<List<ClientInfo>, ExecutorTask>> finalTasks = executorStatus.getFinalTasks();
		switch (executor.getTaskExecuteMode()) {
		case SERIAL: {
			for (int taskIndex = 0; taskIndex < finalTasks.size(); taskIndex++) {
				ExecuteStatus executeStatus = executorStatus.getTaskStatus().get(taskIndex);
				if (executeStatus.isFailed()) {
					Pair<List<ClientInfo>, ExecutorTask> pair = finalTasks.get(taskIndex);
					ExecutorTask executorTask = pair.getRight();
					int maxRetry = executorTask.getMaxRetry();
					Integer retry = executorStatus.getTaskRetrys().get(taskIndex);
					if (retry >= maxRetry) {
						executorStatus.setStatus(ExecuteStatus.FAILED_MAX_RETRIED);
						logger.warn("Task {}[{}] of executor {} failed with max retry", pair.getRight().getName(),
								taskIndex, executor.getName());
						break;
					} else {
						retry = retry + 1;
						executorStatus.getTaskRetrys().set(taskIndex, retry);
						scheduleOneTask(executor, pair, taskIndex, executorStatus);
						logger.info("Rescheduled task {}[{}] of executor {}, retry {}", pair.getRight().getName(),
								taskIndex, executor.getName(), retry);
						break;
					}
				}
				if (ExecuteStatus.PENDING.equals(executeStatus)) {
					Pair<List<ClientInfo>, ExecutorTask> pair = finalTasks.get(taskIndex);
					scheduleOneTask(executor, pair, taskIndex, executorStatus);
					logger.info("Scheduled task {}[{}] of executor {}", pair.getRight().getName(), taskIndex,
							executor.getName());
					break;
				}
			}
		}
			break;
		case PARALLEL: {
			for (int taskIndex = 0; taskIndex < finalTasks.size(); taskIndex++) {
				ExecuteStatus executeStatus = executorStatus.getTaskStatus().get(taskIndex);
				if (ExecuteStatus.PENDING.equals(executeStatus)) {
					Pair<List<ClientInfo>, ExecutorTask> pair = finalTasks.get(taskIndex);
					scheduleOneTask(executor, pair, taskIndex, executorStatus);
					logger.info("scheduled task {}[{}] of executor {}", pair.getRight().getName(), taskIndex,
							executor.getName());
				} else if (executeStatus.isFailed()) {
					Pair<List<ClientInfo>, ExecutorTask> pair = finalTasks.get(taskIndex);
					ExecutorTask executorTask = pair.getRight();
					int maxRetry = executorTask.getMaxRetry();
					Integer retry = executorStatus.getTaskRetrys().get(taskIndex);
					if (retry >= maxRetry) {
						executorStatus.setStatus(ExecuteStatus.FAILED_MAX_RETRIED);
						logger.warn("Task {}[{}] of executor {} failed with max retry", pair.getRight().getName(),
								taskIndex, executor.getName());
						break;
					} else {
						retry = retry + 1;
						executorStatus.getTaskRetrys().set(taskIndex, retry);
						scheduleOneTask(executor, pair, taskIndex, executorStatus);
						logger.info("Rescheduled task {}[{}] of executor {}, retry {}", pair.getRight().getName(),
								taskIndex, executor.getName(), retry);
						break;
					}
				}
			}
		}
			break;
		}
		String executorName = executorStatus.getExecutor().getName();
		String scheduledId = executorStatus.getScheduledId();
		scheduleStatusCache.put(scheduleStatusKey(executorName, scheduledId), executorStatus);
	}

}
