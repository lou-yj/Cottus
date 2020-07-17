package com.louyj.rhttptunnel.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;
import static org.apache.commons.collections4.CollectionUtils.intersection;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.louyj.rhttptunnel.model.bean.automate.AlarmInhibitor;
import com.louyj.rhttptunnel.model.bean.automate.AlarmMarker;
import com.louyj.rhttptunnel.model.bean.automate.Alarmer;
import com.louyj.rhttptunnel.model.bean.automate.AutomateRule;
import com.louyj.rhttptunnel.model.bean.automate.Executor;
import com.louyj.rhttptunnel.model.bean.automate.Handler;
import com.louyj.rhttptunnel.model.bean.automate.RepoConfig;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.file.FileDataMessage;
import com.louyj.rhttptunnel.model.message.file.UncompressFileMessage;
import com.louyj.rhttptunnel.model.message.repo.RepoUpdateMessage;
import com.louyj.rhttptunnel.model.util.CompressUtils;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.server.SystemClient;
import com.louyj.rhttptunnel.server.SystemClient.ISystemClientListener;
import com.louyj.rhttptunnel.server.automation.AutomateManager;
import com.louyj.rhttptunnel.server.handler.IClientMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientInfoManager;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.WorkerSession;
import com.louyj.rhttptunnel.server.session.WorkerSessionManager;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class RepoUpdateHandler implements IClientMessageHandler, ISystemClientListener {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${transfer.data.maxsize:1048576}")
	private int transferMaxSize;
	@Autowired
	private AutomateManager automateManager;
	@Autowired
	private WorkerSessionManager workerSessionManager;
	@Autowired
	private SystemClient systemClient;
	@Autowired
	private ClientInfoManager clientInfoManager;

	private Set<String> exchangeIds = Sets.newHashSet();
	private Map<String, BaseMessage> exchangeResult = Maps.newHashMap();

	@Override
	public Class<? extends BaseMessage> supportType() {
		return RepoUpdateMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return true;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		Set<String> tempExchangeIds = Sets.newHashSet();
		try {
			String exchangeId = message.getExchangeId();
			sendClientMessage(clientSession, exchangeId, "Start update automation repository");
			RepoConfig repoConfig = automateManager.getRepoConfig();
			String dataDir = automateManager.getDataDir();
			sendClientMessage(clientSession, exchangeId,
					String.format("Clone repository from %s branch %s", repoConfig.getUrl(), repoConfig.getBranch()));
			String repoDirPath = dataDir + "/repository/source";
			File repoDir = new File(repoDirPath);
			repoDir.getParentFile().mkdirs();
			if (repoDir.exists()) {
				FileUtils.deleteDirectory(repoDir);
			}
			String headCommitId = null;

			try {
				Git git = Git.cloneRepository()
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(repoConfig.getUsername(),
								repoConfig.getPassword()))
						.setURI(repoConfig.getUrl()).setDirectory(repoDir).setBranch(repoConfig.getBranch()).call();
				headCommitId = headCommitId(git);
				automateManager.setRepoCommitId(headCommitId);
				sendClientMessage(clientSession, exchangeId, "Current commit id is " + headCommitId);
				git.close();
			} catch (Exception e2) {
				throw new RuntimeException(String.format("Clone git from %s failed", repoConfig.getUrl()), e2);
			}
			String repoCommitIdPath = dataDir + "/repository/" + headCommitId;
			File repoCidDir = new File(repoCommitIdPath);
			if (repoCidDir.exists()) {
				FileUtils.deleteDirectory(repoCidDir);
			}
			FileUtils.moveDirectory(repoDir, repoCidDir);

			{
				sendClientMessage(clientSession, exchangeId, "Start send repo zipfile to all workers");
				File zipFile = new File(dataDir + "/repository", headCommitId + ".zip");
				CompressUtils.zipFolder(repoCidDir, zipFile);
				Collection<WorkerSession> workers = workerSessionManager.workers();
				if (CollectionUtils.isEmpty(workers)) {
					throw new RuntimeException("No workers online");
				}
				List<String> workerClientIds = Lists.newArrayList();
				workers.forEach(e -> workerClientIds.add(e.getClientId()));
				send(tempExchangeIds, clientSession, exchangeId, zipFile.getAbsolutePath(),
						"repository/" + zipFile.getName(), workerClientIds);
				waitAllWorkerAcked(tempExchangeIds, clientSession, exchangeId);
				cleanExchangeIds(tempExchangeIds);
				sendClientMessage(clientSession, exchangeId, "Start send uncompress command all workers");
				UncompressFileMessage unzipMessage = new UncompressFileMessage(systemClient.clientInfo());
				unzipMessage.setSource("repository/" + zipFile.getName());
				unzipMessage.setTarget("repository/" + headCommitId);
				unzipMessage.setType("zip");
				unzipMessage.setDeleteSource(true);
				for (String toWorkerId : workerClientIds) {
					String uuid = UUID.randomUUID().toString();
					unzipMessage.setExchangeId(uuid);
					tempExchangeIds.add(uuid);
					exchangeIds.add(uuid);
					systemClient.exchange(unzipMessage, Arrays.asList(toWorkerId));
				}
				waitAllWorkerAcked(tempExchangeIds, clientSession, exchangeId);
				cleanExchangeIds(tempExchangeIds);
			}

			sendClientMessage(clientSession, exchangeId,
					"Start parser automate rules using rule directory " + repoConfig.getRuleDirectory());
			File ruleDir = new File(repoCidDir, repoConfig.getRuleDirectory());
			Collection<File> ruleFiles = FileUtils.listFiles(ruleDir, new String[] { "yaml" }, true);
			Yaml yaml = new Yaml();
			ObjectMapper jackson = JsonUtils.jackson();
			List<Executor> executors = Lists.newArrayList();
			List<Alarmer> alarmers = Lists.newArrayList();
			List<Handler> handlers = Lists.newArrayList();
			List<AlarmMarker> alarmMarkers = Lists.newArrayList();
			List<AlarmInhibitor> alarmInhibitors = Lists.newArrayList();

			Set<String> names = Sets.newHashSet();
			try {
				for (File ruleFile : ruleFiles) {
					Object load = yaml.load(new FileInputStream(ruleFile));
					AutomateRule automateRule = jackson.convertValue(load, AutomateRule.class);
					if (automateRule.getExecutor() != null) {
						Executor executor = automateRule.getExecutor();
						executor.check(ruleFile, repoCommitIdPath);
						executors.add(executor);
						checkNames(names, "executor", executor.getName());
						executor.getTasks().forEach(t -> checkNames(names, "executor task", t.getName()));
					}
					if (automateRule.getAlarmers() != null) {
						automateRule.getAlarmers().forEach(alarmer -> {
							alarmer.check(ruleFile);
							alarmers.add(alarmer);
							checkNames(names, "alarmer", alarmer.getName());
						});
					}
					if (automateRule.getHandlers() != null) {
						automateRule.getHandlers().forEach(handler -> {
							handler.check(ruleFile, repoCommitIdPath);
							handlers.add(handler);
							checkNames(names, "handler", handler.getName());
						});
					}
					if (automateRule.getMarkers() != null) {
						automateRule.getMarkers().forEach(marker -> {
							marker.check(ruleFile);
							alarmMarkers.add(marker);
							checkNames(names, "marker", marker.getName());
						});
					}
					if (automateRule.getInhibitors() != null) {
						automateRule.getInhibitors().forEach(inhibitor -> {
							inhibitor.check(ruleFile, repoCommitIdPath);
							alarmInhibitors.add(inhibitor);
							checkNames(names, "inhibitor", inhibitor.getName());
						});
					}
				}
				sendClientMessage(clientSession, exchangeId,
						String.format(
								"Parsed %d executors %d alarmers %d handlers %d alarm markers %s alarm inhibitors",
								executors.size(), alarmers.size(), handlers.size(), alarmMarkers.size(),
								alarmInhibitors.size()));
				automateManager.updateRules(executors, alarmers, handlers, alarmMarkers, alarmInhibitors);
				sendClientMessage(clientSession, exchangeId, "Scheduler updated");
				return AckMessage.sack(exchangeId);
			} catch (Exception e2) {
				logger.error("", e2);
				return RejectMessage.creason(SERVER, exchangeId,
						String.format("Exception %s reason %s", e2.getClass().getName(), e2.getMessage()));
			}
		} finally {
			cleanExchangeIds(tempExchangeIds);
		}
	}

	private void checkNames(Set<String> names, String category, String name) {
		String key = category + ":" + name;
		if (names.contains(key)) {
			throw new RuntimeException(String.format("Name duplicate, %s: %s", category, name));
		}
		names.add(key);
	}

	private void waitAllWorkerAcked(Set<String> tempExchangeIds, ClientSession clientSession, String exchangeId)
			throws InterruptedException {
		sendClientMessage(clientSession, exchangeId, "Wait for all workers acked");
		long start = System.currentTimeMillis();
		logger.info("Current exchange ids {}", tempExchangeIds);
		while (true) {
			if (System.currentTimeMillis() - start > TimeUnit.MINUTES.toMillis(10)) {
				throw new RuntimeException("Timeout wait for workers acked");
			}
			for (String id : tempExchangeIds) {
				BaseMessage baseMessage = exchangeResult.remove(id);
				if (baseMessage instanceof AckMessage) {
					ClientInfo client = clientInfoManager.findClientInfo(baseMessage.getClientId());
					sendClientMessage(clientSession, exchangeId,
							String.format("Worker %s[%s] acked", client.getHost(), client.getIp()));
				} else if (baseMessage instanceof RejectMessage) {
					ClientInfo client = clientInfoManager.findClientInfo(baseMessage.getClientId());
					throw new RuntimeException(String.format("Worker reject %s[%s] reason %s", client.getHost(),
							client.getIp(), ((RejectMessage) baseMessage).getReason()));
				}
			}
			Collection<String> waitIds = intersection(exchangeIds, tempExchangeIds);
			if (isEmpty(waitIds)) {
				sendClientMessage(clientSession, exchangeId, "All workers acked");
				return;
			}
			TimeUnit.SECONDS.sleep(1);
		}
	}

	private void cleanExchangeIds(Set<String> tempExchangeIds) {
		exchangeIds.removeAll(tempExchangeIds);
		for (String key : tempExchangeIds) {
			exchangeResult.remove(key);
		}
		tempExchangeIds.clear();
	}

	private String headCommitId(Git git) {
		Ref ref = git.getRepository().getAllRefs().get("HEAD");
		return ref.getObjectId().getName();
	}

	private void sendClientMessage(ClientSession clientSession, String exchangeId, String content)
			throws InterruptedException {
		if (clientSession == null) {
			logger.warn("client session is null");
			return;
		}
		logger.info(content);
		RepoUpdateMessage amMessage = new RepoUpdateMessage(SERVER, exchangeId);
		amMessage.setMessage(content);
		clientSession.getMessageQueue().put(amMessage);
	}

	private void sendClientMessage(ClientSession clientSession, String exchangeId, BaseMessage message)
			throws InterruptedException {
		if (clientSession == null) {
			return;
		}
		message.setExchangeId(exchangeId);
		clientSession.getMessageQueue().put(message);
	}

	public boolean send(Set<String> tempExchangeIds, ClientSession clientSession, String clientExchangeId, String path,
			String target, List<String> toWorkerIds) throws Exception {
		File file = new File(path);
		String targetName = isBlank(target) ? file.getName() : target;
		long totalSize = file.length();
		long currentSize = 0;
		FileInputStream fis = new FileInputStream(file);
		String md5Hex = DigestUtils.md5Hex(fis);
		fis.close();
		fis = new FileInputStream(file);
		byte[] buffer = new byte[transferMaxSize];
		boolean start = true;
		boolean end = false;
		while (true) {
			int read = fis.read(buffer);
			byte[] data = buffer;
			if (read == -1) {
				data = new byte[0];
				end = true;
				fis.close();
			} else if (read != buffer.length) {
				data = new byte[read];
				System.arraycopy(buffer, 0, data, 0, read);
				currentSize += read;
			} else {
				currentSize += read;
			}
			for (String toWorkerId : toWorkerIds) {
				FileDataMessage fileDataMessage = new FileDataMessage(systemClient.clientInfo(), targetName, start, end,
						data, md5Hex);
				fileDataMessage.setSize(totalSize, currentSize);
				if (end) {
					tempExchangeIds.add(fileDataMessage.getExchangeId());
					exchangeIds.add(fileDataMessage.getExchangeId());
				}
				BaseMessage responseMessage = systemClient.exchange(fileDataMessage, Arrays.asList(toWorkerId));
				if (responseMessage instanceof RejectMessage) {
					sendClientMessage(clientSession, clientExchangeId, responseMessage);
					fis.close();
					return false;
				} else {
					logger.info("Send package success, total size " + fileDataMessage.getTotalSize()
							+ " current received " + fileDataMessage.getCurrentSize());
				}
			}
			if (end) {
				return true;
			}
			if (start) {
				start = false;
			}
		}
	}

	@Override
	public List<Class<? extends BaseMessage>> listenSendMessages() {
		return null;
	}

	@Override
	public List<Class<? extends BaseMessage>> listenReceiveMessages() {
		return null;
	}

	@Override
	public void onSendMessage(BaseMessage message, List<String> toWorkers) {

	}

	@Override
	public void onReceiveMessage(BaseMessage message) {
		try {
			if (exchangeIds.remove(message.getExchangeId())) {
				exchangeResult.put(message.getExchangeId(), message);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
