package com.louyj.rhttptunnel.server.automation;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

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
import com.google.common.collect.Sets;
import com.louyj.rhttptunnel.model.bean.RepoConfig;
import com.louyj.rhttptunnel.model.bean.Sampler;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.server.SamplerJobAckMessage;
import com.louyj.rhttptunnel.model.message.server.SamplerJobMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.server.SystemClient;
import com.louyj.rhttptunnel.server.SystemClient.SystemClientListener;
import com.louyj.rhttptunnel.server.automation.AutomateRule.Handler;
import com.louyj.rhttptunnel.server.automation.AutomateRule.Rule;
import com.louyj.rhttptunnel.server.automation.SamplerLog.JobStatus;
import com.louyj.rhttptunnel.server.session.WorkerSessionManager;
import com.louyj.rhttptunnel.server.workerlabel.LabelRule;

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
	private static final String AUTOMATE_SAMPLE = "automate:sample";
	private static final String AUTOMATE_RULE = "automate:rule";
	private static final String AUTOMATE_HANDLER = "automate:handler";

	@Value("${data.dir:/data}")
	private String dataDir;
	@Autowired
	private Ignite ignite;
	@Autowired
	private SystemClient systemClient;
	@Autowired
	private WorkerSessionManager workerSessionManager;

	private RepoConfig repoConfig;
	private IgniteCache<Object, Object> configCache;
	private IgniteCache<Object, Object> logCache;

	private String repoCommitId;
	private List<Sampler> samplers = Lists.newArrayList();
	private List<Rule> rules = Lists.newArrayList();
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
		this.samplers = (List<Sampler>) configCache.get(AUTOMATE_SAMPLE);
		this.rules = (List<Rule>) configCache.get(AUTOMATE_RULE);
		this.handlers = (List<Handler>) configCache.get(AUTOMATE_HANDLER);
	}

	public void updateRepoConfig(RepoConfig repoConfig) {
		setRepoConfig(repoConfig);
		configCache.put(CONFIG_REPO, repoConfig);
	}

	public void updateRules(List<Sampler> samplers, List<Rule> rules, List<Handler> handlers) {
		this.samplers = samplers;
		this.rules = rules;
		this.handlers = handlers;
		configCache.put(AUTOMATE_SAMPLE, samplers);
		configCache.put(AUTOMATE_RULE, rules);
		configCache.put(AUTOMATE_HANDLER, handlers);
	}

	public void scheduleSample(Sampler sampler) {
		List<ClientInfo> toWorkers = workerSessionManager.filterWorkerClients(sampler.getTargets(), Sets.newHashSet());
		List<String> workerText = Lists.newArrayList();
		toWorkers.forEach(e -> workerText.add(e.getHost()));
		logger.info("Start schedule sampler task {}, matched workers {}", sampler.getName(), workerText);
		SamplerJobMessage jobMessage = new SamplerJobMessage(systemClient.session().getClientInfo());
		systemClient.exchange(jobMessage, toWorkers);
	}

	public void updateScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(10);
		for (Sampler sampler : samplers) {
			String schedule = sampler.getSchedule();
			SamplerScheduleTask task = new SamplerScheduleTask(sampler, this);
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
		return Arrays.asList(SamplerJobMessage.class);
	}

	@Override
	public List<Class<? extends BaseMessage>> listenReceiveMessages() {
		return Arrays.asList(SamplerJobAckMessage.class);
	}

	@Override
	public void onSendMessage(BaseMessage message, List<ClientInfo> toWorkers) {
		if (message instanceof SamplerJobMessage) {
			SamplerJobMessage jobMessage = (SamplerJobMessage) message;
			long millis = DateTime.now().getMillis();
			String serverMsgId = jobMessage.getServerMsgId();
			for (ClientInfo toWorker : toWorkers) {
				SamplerLog samplerLog = new SamplerLog();
				samplerLog.setName(jobMessage.getSampler().getName());
				samplerLog.setTime(millis);
				samplerLog.setHost(toWorker.getHost());
				samplerLog.setIp(toWorker.getIp());
				samplerLog.setStatus(JobStatus.SCHEDULED);
				String key = serverMsgId + ":" + toWorker.getHost() + ":" + toWorker.getIp();
				logCache.put(key, samplerLog);
			}
		}
	}

	@Override
	public void onReceiveMessage(BaseMessage message) {
		if (message instanceof SamplerJobAckMessage) {
			SamplerJobAckMessage ackMessage = (SamplerJobAckMessage) message;
			ClientInfo toWorker = ackMessage.getClient();
			String key = ackMessage.getServerMsgId() + ":" + toWorker.getHost() + ":" + toWorker.getIp();
			SamplerLog samplerLog = (SamplerLog) logCache.get(key);
			if (samplerLog == null) {
				logger.warn("Sample log is null for {}", JsonUtils.gson().toJson(message));
				return;
			}
			samplerLog.setStatus(JobStatus.FINISHED);
			samplerLog.setMessage(ackMessage.getMessage());
			logCache.put(key, samplerLog);
		}
	}

}
