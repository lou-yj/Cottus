package com.louyj.rhttptunnel.server.automation;

import static com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage.ExecuteStatus.SCHEDULED;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.louyj.rhttptunnel.model.bean.automate.AlarmInhibitor;
import com.louyj.rhttptunnel.model.bean.automate.AlarmMarker;
import com.louyj.rhttptunnel.model.bean.automate.Alarmer;
import com.louyj.rhttptunnel.model.bean.automate.Executor;
import com.louyj.rhttptunnel.model.bean.automate.ExecutorLog;
import com.louyj.rhttptunnel.model.bean.automate.ExecutorTask;
import com.louyj.rhttptunnel.model.bean.automate.ExecutorTaskRecord;
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
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsCollectType;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsType;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.ScriptContentType;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.TaskType;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.server.SystemClient;
import com.louyj.rhttptunnel.server.SystemClient.ISystemClientListener;
import com.louyj.rhttptunnel.server.automation.event.AlarmEvent;
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
public class AutomateManager implements ISystemClientListener, InitializingBean {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final String CONFIG_REPO = "config:repo";
	private static final String CONFIG_REPO_COMMITID = "config:repo:commitid";
	private static final String AUTOMATE_EXECUTOR = "automate:executor";
	private static final String AUTOMATE_ALARMER = "automate:alarmer";
	private static final String AUTOMATE_HANDLER = "automate:handler";
	private static final String AUTOMATE_ALARM_MARKER = "automate:alarmmarker";
	private static final String AUTOMATE_ALARM_INHIBITOR = "automate:alarminhibitor";
	public static final String EXEC_HOST = "HOST";
	public static final String EXEC_IP = "IP";

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

	private List<String> defaultAlarmGroupKeys = Lists.newArrayList();

	// repo
	private RepoConfig repoConfig;
	private String repoCommitId;

	// cache
	private IgniteCache<Object, Object> configCache;
	private IgniteCache<Object, Object> auditCache;
	private IgniteCache<Object, Object> scheduleStatusCache;
	private IgniteCache<Object, Object> alarmCache;

	private List<Executor> executors = Lists.newArrayList();
	private List<Alarmer> alarmers = Lists.newArrayList();
	private List<Handler> handlers = Lists.newArrayList();
	private List<AlarmMarker> alarmMarkers = Lists.newArrayList();
	private List<AlarmInhibitor> alarmInhibitors = Lists.newArrayList();
	private AlarmService alarmService;
	private HandlerService handlerService;
	private ThreadPoolTaskScheduler taskScheduler;
	private ObjectMapper jackson = JsonUtils.jackson();
	private IgniteAtomicLong indexCounter;

	@Value("${alarmer.default.groupkeys:}")
	public void setDefaultAlarmGroupKey(String defaultAlarmGroup) {
		if (StringUtils.isBlank(defaultAlarmGroup) == false) {
			this.defaultAlarmGroupKeys = Arrays.asList(defaultAlarmGroup.split(","));
		}
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

	public List<Handler> getHandlers() {
		return handlers;
	}

	public List<Executor> getExecutors() {
		return executors;
	}

	public List<Alarmer> getAlarmers() {
		return alarmers;
	}

	public List<AlarmMarker> getAlarmMarkers() {
		return alarmMarkers;
	}

	public List<AlarmInhibitor> getAlarmInhibitors() {
		return alarmInhibitors;
	}

	public AlarmService getAlarmService() {
		return alarmService;
	}

	public Executor getExecutor(String name) {
		for (Executor executor : executors) {
			if (StringUtils.equals(executor.getName(), name)) {
				return executor;
			}
		}
		return null;
	}

	public Handler getHandler(String id) {
		for (Handler handler : handlers) {
			if (StringUtils.equals(handler.getName(), id)) {
				return handler;
			}
		}
		return null;
	}

	public IgniteCache<Object, Object> getAlarmCache() {
		return alarmCache;
	}

	public IgniteCache<Object, Object> getAuditCache() {
		return auditCache;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		indexCounter = ignite.atomicLong("indexCounter", 0, true);
		taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(10);
		configCache = ignite.getOrCreateCache("automate");
		auditCache = ignite.getOrCreateCache(new CacheConfiguration<>().setName("automateAudit")
				.setIndexedTypes(String.class, ScheduledTaskAudit.class)
				.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.DAYS, 10))));
		scheduleStatusCache = ignite.getOrCreateCache(new CacheConfiguration<>().setName("automateScheduleStatus")
				.setIndexedTypes(String.class, ExecutorStatus.class)
				.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.DAYS, 10))));
		alarmCache = ignite.getOrCreateCache(new CacheConfiguration<>().setName("alarmCache")
				.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.DAYS, 100)))
				.setIndexedTypes(String.class, AlarmEvent.class, String.class, AlarmHandlerInfo.class, String.class,
						AlarmSilencer.class));
		handlerService = new HandlerService(this, alarmCache);
		alarmService = new AlarmService(handlerService, this);
		this.repoConfig = (RepoConfig) configCache.get(CONFIG_REPO);
		this.executors = (List<Executor>) configCache.get(AUTOMATE_EXECUTOR);
		this.alarmers = (List<Alarmer>) configCache.get(AUTOMATE_ALARMER);
		this.handlers = (List<Handler>) configCache.get(AUTOMATE_HANDLER);
		this.alarmMarkers = (List<AlarmMarker>) configCache.get(AUTOMATE_ALARM_MARKER);
		this.alarmInhibitors = (List<AlarmInhibitor>) configCache.get(AUTOMATE_ALARM_INHIBITOR);
		this.repoCommitId = (String) configCache.get(CONFIG_REPO_COMMITID);
		updateRuleService();
	}

	public String nextIndex() {
		return String.valueOf(indexCounter.incrementAndGet());
	}

	public void updateRepoConfig(RepoConfig repoConfig) {
		setRepoConfig(repoConfig);
		configCache.put(CONFIG_REPO, repoConfig);
	}

	public void updateRules(List<Executor> executors, List<Alarmer> alarmers, List<Handler> handlers,
			List<AlarmMarker> alarmMarkers, List<AlarmInhibitor> alarmInhibitors) {
		this.executors = executors;
		this.alarmers = alarmers;
		this.handlers = handlers;
		this.alarmMarkers = alarmMarkers;
		this.alarmInhibitors = alarmInhibitors;
		configCache.put(AUTOMATE_EXECUTOR, executors);
		configCache.put(AUTOMATE_ALARMER, alarmers);
		configCache.put(AUTOMATE_HANDLER, handlers);
		configCache.put(AUTOMATE_ALARM_MARKER, alarmMarkers);
		configCache.put(AUTOMATE_ALARM_INHIBITOR, alarmInhibitors);
		configCache.put(CONFIG_REPO_COMMITID, repoCommitId);
		updateRuleService();
	}

	private void updateRuleService() {
		if (this.handlers == null) {
			this.handlers = Lists.newArrayList();
		}
		if (this.executors == null) {
			this.executors = Lists.newArrayList();
		}
		if (this.alarmers == null) {
			this.alarmers = Lists.newArrayList();
		}
		if (this.alarmMarkers == null) {
			this.alarmMarkers = Lists.newArrayList();
		}
		if (this.alarmInhibitors == null) {
			this.alarmInhibitors = Lists.newArrayList();
		}
		Collections.sort(handlers, new Comparator<Handler>() {
			@Override
			public int compare(Handler o1, Handler o2) {
				return o2.getOrder() - o1.getOrder();
			}
		});
		Collections.sort(alarmMarkers, new Comparator<AlarmMarker>() {
			@Override
			public int compare(AlarmMarker o1, AlarmMarker o2) {
				return o2.getOrder() - o1.getOrder();
			}
		});
		updateSchedulers();
		updateAlarmers();
	}

	public List<ExecutorTaskRecord> searchAuditRecords(TaskType type, String executor, String name, int limit) {
		SqlFieldsQuery sql = new SqlFieldsQuery(
				"SELECT scheduleId,time,params,sre,status,metrics,message FROM ScheduledTaskAudit info where type=? and executor=? and name = ? order by scheduleId desc limit ?")
						.setArgs(type, executor, name, limit);
		List<ExecutorTaskRecord> result = Lists.newArrayList();
		try (QueryCursor<List<?>> cursor = auditCache.query(sql)) {
			for (List<?> row : cursor) {
				ExecutorTaskRecord record = new ExecutorTaskRecord();
				record.setExecutor(executor);
				record.setName(name);
				int index = 0;
				record.setScheduleId(rowGet(row, index++));
				record.setTime(rowGet(row, index++));
				record.setParams(rowGet(row, index++));
				record.setSre(rowGet(row, index++));
				record.setStatus(rowGet(row, index++));
				record.setMetrics(rowGet(row, index++));
				record.setMessage(rowGet(row, index++));
				result.add(record);
			}
		}
		return result;
	}

	public List<ExecutorLog> searchAuditLogs(TaskType type, String executor, String name, String scheduleId) {
		SqlFieldsQuery sql = new SqlFieldsQuery(
				"SELECT stdout,stderr,sre FROM ScheduledTaskAudit info where type=? and executor=? and name = ? and scheduleId=?")
						.setArgs(type, executor, name, scheduleId);
		List<ExecutorLog> result = Lists.newArrayList();
		try (QueryCursor<List<?>> cursor = auditCache.query(sql)) {
			for (List<?> row : cursor) {
				ExecutorLog executorLog = new ExecutorLog();
				int index = 0;
				executorLog.setStdout(rowGet(row, index++));
				executorLog.setStderr(rowGet(row, index++));
				Map<String, String> sre = rowGet(row, index++);
				String host = MapUtils.getString(sre, EXEC_HOST);
				String ip = MapUtils.getString(sre, EXEC_IP);
				executorLog.setHost(host);
				executorLog.setIp(ip);
				result.add(executorLog);
			}
		}
		return result;
	}

	public void scheduleExecutorTask(Executor executor) throws JsonParseException, JsonMappingException, IOException {
		String scheduledId = DateTime.now().toString("yyMMddHHmmssSSS");
		logger.info("Start schedule executor {} task execute mode {} schedule id {}", executor.getName(),
				executor.getTaskExecuteMode(), scheduledId);
		List<Pair<List<ClientInfo>, ExecutorTask>> finalTasks = executor.parseFinalTasks(workerSessionManager);
		ExecutorStatus executorStatus = new ExecutorStatus(executor, finalTasks, scheduledId);
		logger.info("Total {} tasks for executor {}", finalTasks.size(), executor.getName());
		scheduleStatusCache.put(scheduleStatusKey(executor.getName(), scheduledId), executorStatus);
		scheduleNextTask(executorStatus);
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
				taskAudit.setType(taskMessage.getType());
				String key = auditTaskKey(taskMessage, toWorker);
				auditCache.put(key, taskAudit);
			}
		}
	}

	private void noWorkerAudit(TaskScheduleMessage taskMessage) {
		long millis = DateTime.now().getMillis();
		ScheduledTaskAudit taskAudit = new ScheduledTaskAudit();
		taskAudit.setExecutor(taskMessage.getExecutor());
		taskAudit.setName(taskMessage.getName());
		taskAudit.setTime(millis);
		taskAudit.setStatus(ExecuteStatus.NO_MATCHED_WORKER);
		taskAudit.setParams(taskMessage.getParams());
		taskAudit.setScheduleId(taskMessage.getScheduledId());
		taskAudit.setType(taskMessage.getType());
		String key = auditTaskKey(taskMessage, null);
		auditCache.put(key, taskAudit);
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
			if (TaskType.EXECUTOR.equals(taskAudit.getType())) {
				alarmService.sendEvent(ExecStatusEvent.make(taskAudit, ackMessage));
				String scheduleStatusKey = scheduleStatusKey(taskAudit.getExecutor(), taskAudit.getScheduleId());
				ExecutorStatus executorStatus = (ExecutorStatus) scheduleStatusCache.get(scheduleStatusKey);
				scheduleNextTask(executorStatus);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void scheduleHandler(Handler handler, AlarmEvent alarmEvent, List<AlarmEvent> alarmEvents,
			AlarmHandlerInfo alarmHandlerInfo, Map<String, String> targetMap) {
		TaskScheduleMessage taskMessage = new TaskScheduleMessage(systemClient.session().getClientInfo());
		taskMessage.setType(TaskType.HANDLER);
		taskMessage.setScheduledId(alarmHandlerInfo.getUuid());
		taskMessage.setExecutor("handler");
		taskMessage.setName(handler.getName());
		taskMessage.setCommitId(getRepoCommitId());
		taskMessage.setLanguage(handler.getLanguage());
		if (StringUtils.isNotBlank(handler.getScript())) {
			taskMessage.setScript(handler.getScript());
			taskMessage.setScriptContentType(ScriptContentType.TEXT);
		}
		if (StringUtils.isNotBlank(handler.getScriptFile())) {
			taskMessage.setScript(handler.getScriptFile());
			taskMessage.setScriptContentType(ScriptContentType.FILE);
		}
		Map<String, Object> params = Maps.newHashMap();
		if (MapUtils.isNotEmpty(handler.getParams())) {
			params.putAll(handler.getParams());
		}
		params.putAll(jackson.convertValue(alarmEvent, Map.class));
		taskMessage.setParams(params);
		taskMessage.setExpected(Collections.singletonMap("exitValue", 0));
		taskMessage.setMetricsCollectType(MetricsCollectType.EXITVALUE_WRAPPER);
		taskMessage.setMetricsType(MetricsType.STANDARD);
		taskMessage.setTimeout(handler.getTimeout());
		taskMessage.setCollectStdLog(true);

		List<Map<String, Object>> correlationParams = Lists.newArrayList();
		alarmEvents.forEach(e -> correlationParams.add(jackson.convertValue(e, Map.class)));
		taskMessage.setCorrelationParams(correlationParams);

		List<ClientInfo> toWorkers = workerSessionManager.filterWorkerClients(targetMap, Sets.newHashSet());
		alarmHandlerInfo.setTargetHosts(toWorkers);
		alarmHandlerInfo.setParams(params);
		alarmHandlerInfo.setScheduleId(alarmHandlerInfo.getUuid());

		List<String> correlationAlarmIds = Lists.newArrayList();
		alarmEvents.forEach(e -> correlationAlarmIds.add(e.getUuid()));
		alarmHandlerInfo.setCorrelationAlarmIds(correlationAlarmIds);

		if (CollectionUtils.isEmpty(toWorkers)) {
			noWorkerAudit(taskMessage);
			alarmHandlerInfo.setStatus(ExecuteStatus.NO_MATCHED_WORKER);
			logger.warn("No worker matched for handler {} ", handler.getName());
			return;
		}
		List<String> toWorkerText = Lists.newArrayList();
		toWorkers.forEach(e -> toWorkerText.add(e.getHost()));
		alarmHandlerInfo.setScheduledTime(System.currentTimeMillis());
		logger.info("Start schedule handler {}, matched workers {}", handler.getName(), toWorkerText);
		for (ClientInfo toWorker : toWorkers) {
			LabelRule labelRule = workerLabelManager.findRule(toWorker);
			if (labelRule != null) {
				taskMessage.setLabels(labelRule.getLabels());
			} else {
				taskMessage.setLabels(Maps.newHashMap());
			}
			systemClient.exchange(taskMessage, Arrays.asList(toWorker));
		}
		alarmHandlerInfo.setHandled(true);
		alarmHandlerInfo.setStatus(ExecuteStatus.SCHEDULED);
		if (CollectionUtils.isNotEmpty(alarmEvents)) {
			List<String> alarmIds = Lists.newArrayList();
			alarmEvents.forEach(e -> alarmIds.add(e.getUuid()));
			SqlFieldsQuery sql1 = new SqlFieldsQuery(
					"SELECT uuid FROM AlarmHandlerInfo info join table (alarmId varchar = ? ) t on t.alarmId=info.alarmId where handlerId=?")
							.setArgs(alarmIds.toArray(), handler.getName());
			try (QueryCursor<List<?>> cursor1 = alarmCache.query(sql1)) {
				for (List<?> row1 : cursor1) {
					String uuid = rowGet(row1, 0);
					AlarmHandlerInfo relInfo = (AlarmHandlerInfo) alarmCache.get(uuid);
					if (relInfo != null) {
						relInfo.setHandled(true);
						alarmCache.put(uuid, relInfo);
					}
				}
			}
		}
		alarmCache.put(alarmHandlerInfo.getUuid(), alarmHandlerInfo);
	}

	private void updateSchedulers() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(10);
		taskScheduler.initialize();
		for (Executor executor : executors) {
			String schedule = executor.getScheduleExpression();
			SamplerScheduleTask task = new SamplerScheduleTask(executor, this);
			taskScheduler.schedule(task, new CronTrigger(schedule));
		}
		ThreadPoolTaskScheduler taskSchedulerOld = this.taskScheduler;
		this.taskScheduler = taskScheduler;
		taskSchedulerOld.shutdown();
	}

	private void updateAlarmers() {
		if (CollectionUtils.isNotEmpty(defaultAlarmGroupKeys)) {
			for (Alarmer alarmer : alarmers) {
				if (CollectionUtils.isEmpty(alarmer.getGroupKeys())) {
					alarmer.setGroupKeys(defaultAlarmGroupKeys);
				}
			}
		}
		alarmService.resetEsper(alarmers);
	}

	private String auditTaskKey(ServerMessage message, ClientInfo toWorker) {
		if (toWorker == null) {
			return "audit:task:" + message.getServerMsgId() + ":" + nextIndex();
		} else
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
		if (CollectionUtils.isEmpty(toWorkers)) {
			noWorkerAudit(taskMessage);
			return;
		}
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

	@SuppressWarnings("unchecked")
	private <T> T rowGet(List<?> row, int index) {
		return (T) row.get(index);
	}

}
