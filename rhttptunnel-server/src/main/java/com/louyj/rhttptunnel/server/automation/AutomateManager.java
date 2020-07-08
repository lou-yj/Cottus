package com.louyj.rhttptunnel.server.automation;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.louyj.rhttptunnel.model.bean.automate.Alarmer;
import com.louyj.rhttptunnel.model.bean.automate.Executor;
import com.louyj.rhttptunnel.model.bean.automate.Handler;
import com.louyj.rhttptunnel.model.bean.automate.RepoConfig;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.server.TaskLogMessage;
import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage;
import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage.ExecuteStatus;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.ScriptContentType;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.server.SystemClient;
import com.louyj.rhttptunnel.server.SystemClient.SystemClientListener;
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

	private RepoConfig repoConfig;
	private IgniteCache<Object, Object> configCache;
	private IgniteCache<Object, Object> logCache;

	private String repoCommitId;
	private List<Executor> executors = Lists.newArrayList();
	private List<Alarmer> alarmers = Lists.newArrayList();
	private List<Handler> handlers = Lists.newArrayList();
	private ThreadPoolTaskScheduler taskScheduler;

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(10);
		configCache = ignite.getOrCreateCache("automate");
		logCache = ignite.getOrCreateCache(
				new CacheConfiguration<>().setName("automateLog").setIndexedTypes(String.class, LabelRule.class));
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
	}

	public void scheduleExecutorTask(Executor executor) {
		List<ClientInfo> toWorkers = workerSessionManager.filterWorkerClients(executor.getTargets(), Sets.newHashSet());
		List<String> workerText = Lists.newArrayList();
		toWorkers.forEach(e -> workerText.add(e.getHost()));
		logger.info("Start schedule executor task {}, matched workers {}", executor.getName(), workerText);
		TaskScheduleMessage taskMessage = new TaskScheduleMessage(systemClient.session().getClientInfo());
		taskMessage.setName(executor.getName());
		taskMessage.setCommitId(repoCommitId);
		taskMessage.setLanguage(executor.getLanguage());
		taskMessage.setParams(executor.getParams());
		taskMessage.setMetricsCollectType(executor.getMetricsCollectType());
		taskMessage.setMetricsType(executor.getMetricsType());
		taskMessage.setTimeout(executor.getTimeout());
		taskMessage.setCollectStdLog(executor.isCollectStdLog());
		if (StringUtils.isNotBlank(executor.getScript())) {
			taskMessage.setScript(executor.getScript());
			taskMessage.setScriptContentType(ScriptContentType.TEXT);
		}
		if (StringUtils.isNotBlank(executor.getScriptFile())) {
			taskMessage.setScript(executor.getScriptFile());
			taskMessage.setScriptContentType(ScriptContentType.FILE);
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
	}

	public void updateScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(10);
		for (Executor executor : executors) {
			String schedule = executor.getScheduleExpression();
			SamplerScheduleTask task = new SamplerScheduleTask(executor, this);
			taskScheduler.schedule(task, new CronTrigger(schedule));
		}
		this.taskScheduler = taskScheduler;
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
		return Arrays.asList(TaskMetricsMessage.class, TaskLogMessage.class);
	}

	@Override
	public void onSendMessage(BaseMessage message, List<ClientInfo> toWorkers) {
		if (message instanceof TaskScheduleMessage) {
			TaskScheduleMessage jobMessage = (TaskScheduleMessage) message;
			long millis = DateTime.now().getMillis();
			String serverMsgId = jobMessage.getServerMsgId();
			for (ClientInfo toWorker : toWorkers) {
				ExecutorAudit samplerAudit = new ExecutorAudit();
				samplerAudit.setName(jobMessage.getName());
				samplerAudit.setTime(millis);
				samplerAudit.setHost(toWorker.getHost());
				samplerAudit.setIp(toWorker.getIp());
				samplerAudit.setStatus(ExecuteStatus.SCHEDULED);
				String key = serverMsgId + ":" + toWorker.getHost() + ":" + toWorker.getIp();
				logCache.put(key, samplerAudit);
			}
		}
	}

	@Override
	public void onReceiveMessage(BaseMessage message) {
		if (message instanceof TaskMetricsMessage) {
			TaskMetricsMessage metricsMessage = (TaskMetricsMessage) message;
			ClientInfo toWorker = metricsMessage.getClient();
			String key = metricsMessage.getServerMsgId() + ":" + toWorker.getHost() + ":" + toWorker.getIp();
			ExecutorAudit samplerAudit = (ExecutorAudit) logCache.get(key);
			if (samplerAudit == null) {
				logger.warn("Executor audit is null for {}", JsonUtils.gson().toJson(message));
				return;
			}
			samplerAudit.setStatus(metricsMessage.getStatus());
			samplerAudit.getMetrics().add(metricsMessage.format());
			logCache.put(key, samplerAudit);
		} else if (message instanceof TaskLogMessage) {
			TaskLogMessage logMessage = (TaskLogMessage) message;
			ClientInfo toWorker = logMessage.getClient();
			String key = logMessage.getServerMsgId() + ":" + toWorker.getHost() + ":" + toWorker.getIp();
			ExecutorAudit samplerAudit = (ExecutorAudit) logCache.get(key);
			if (samplerAudit == null) {
				logger.warn("Executor audit is null for {}", JsonUtils.gson().toJson(message));
				return;
			}
			samplerAudit.setStdout(logMessage.getOut());
			samplerAudit.setStderr(logMessage.getErr());
			logCache.put(key, samplerAudit);
		}
	}

}
