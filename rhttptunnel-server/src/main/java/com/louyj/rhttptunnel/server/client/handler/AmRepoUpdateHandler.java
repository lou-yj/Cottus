package com.louyj.rhttptunnel.server.client.handler;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.bean.RepoConfig;
import com.louyj.rhttptunnel.model.bean.Sampler;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.FileDataMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.RepoUpdateMessage;
import com.louyj.rhttptunnel.model.message.UncompressFileMessage;
import com.louyj.rhttptunnel.model.util.CompressUtils;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.server.SystemClient;
import com.louyj.rhttptunnel.server.automation.AutomateManager;
import com.louyj.rhttptunnel.server.automation.AutomateRule;
import com.louyj.rhttptunnel.server.automation.AutomateRule.Handler;
import com.louyj.rhttptunnel.server.automation.AutomateRule.Rule;
import com.louyj.rhttptunnel.server.handler.IClientMessageHandler;
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
public class AmRepoUpdateHandler implements IClientMessageHandler {

	@Value("${transfer.data.maxsize:1048576}")
	private int transferMaxSize;
	@Autowired
	private AutomateManager automateManager;
	@Autowired
	private WorkerSessionManager workerSessionManager;
	@Autowired
	private SystemClient systemClient;

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
		Git git = Git.cloneRepository()
				.setCredentialsProvider(
						new UsernamePasswordCredentialsProvider(repoConfig.getUsername(), repoConfig.getPassword()))
				.setURI(repoConfig.getUrl()).setDirectory(repoDir).setBranch(repoConfig.getBranch()).call();
		String headCommitId = headCommitId(git);
		automateManager.setRepoCommitId(headCommitId);
		sendClientMessage(clientSession, exchangeId, "Current commit id is " + headCommitId);
		git.close();
		String repoCommitIdPath = dataDir + "/repository/" + headCommitId;
		File repoCidDir = new File(repoCommitIdPath);
		FileUtils.moveDirectory(repoDir, repoCidDir);

		{
			sendClientMessage(clientSession, exchangeId, "Start send repo file to all workers");
			File zipFile = new File(dataDir + "/repository", headCommitId + ".zip");
			CompressUtils.zipFolder(repoCidDir, zipFile);
			Collection<WorkerSession> workers = workerSessionManager.workers();
			List<ClientInfo> workerClientInfos = Lists.newArrayList();
			workers.forEach(e -> workerClientInfos.add(e.getClientInfo()));
			send(clientSession, exchangeId, zipFile.getAbsolutePath(), "repository/" + zipFile.getName(),
					workerClientInfos);
			UncompressFileMessage unzipMessage = new UncompressFileMessage(systemClient.session().getClientInfo());
			unzipMessage.setSource("repository/" + zipFile.getName());
			unzipMessage.setTarget("repository/" + headCommitId);
			unzipMessage.setType("zip");
			systemClient.exchange(unzipMessage, workerClientInfos);
		}

		sendClientMessage(clientSession, exchangeId,
				"Start parser automate rules using rule directory " + repoConfig.getRuleDirectory());
		File ruleDir = new File(repoCidDir, repoConfig.getRuleDirectory());
		Collection<File> ruleFiles = FileUtils.listFiles(ruleDir, new String[] { "yaml" }, true);
		Yaml yaml = new Yaml();
		ObjectMapper jackson = JsonUtils.jackson();
		List<Sampler> samplers = Lists.newArrayList();
		List<Rule> rules = Lists.newArrayList();
		List<Handler> handlers = Lists.newArrayList();
		for (File ruleFile : ruleFiles) {
			Object load = yaml.load(new FileInputStream(ruleFile));
			AutomateRule automateRule = jackson.convertValue(load, AutomateRule.class);
			if (automateRule.getSampler() != null) {
				Sampler sampler = automateRule.getSampler();
				if (StringUtils.isAnyBlank(sampler.getName(), sampler.getSchedule(), sampler.getScript())) {
					return RejectMessage.creason(message.getClient(), exchangeId, String
							.format("Bad format for sample %s in file %s", sampler.getName(), ruleFile.getName()));
				}
				File scriptFile = new File(repoCommitIdPath, sampler.getScript());
				if (scriptFile.exists() == false) {
					return RejectMessage.creason(message.getClient(), exchangeId, String.format(
							"Script file not exists for sample %s in file %s", sampler.getName(), ruleFile.getName()));
				}
				samplers.add(sampler);
			}
			if (automateRule.getRule() != null) {
				Rule rule = automateRule.getRule();
				if (StringUtils.isAnyBlank(rule.getName(), rule.getExpression())) {
					return RejectMessage.creason(message.getClient(), exchangeId,
							String.format("Bad format for rule %s in file %s", rule.getName(), ruleFile.getName()));
				}
				rules.add(automateRule.getRule());
			}
			if (automateRule.getHandler() != null) {
				Handler handler = automateRule.getHandler();
				if (StringUtils.isAnyBlank(handler.getRuleName(), handler.getScript())) {
					return RejectMessage.creason(message.getClient(), exchangeId, String
							.format("Bad format for handler %s in file %s", handler.getRuleName(), ruleFile.getName()));
				}
				File scriptFile = new File(repoCommitIdPath, handler.getScript());
				if (scriptFile.exists() == false) {
					return RejectMessage.creason(message.getClient(), exchangeId,
							String.format("Script file not exists for handler %s in file %s", handler.getRuleName(),
									ruleFile.getName()));
				}
				handlers.add(automateRule.getHandler());
			}
		}
		sendClientMessage(clientSession, exchangeId,
				String.format("Parsed %d samplers %d rule %d handler", samplers.size(), rules.size(), handlers.size()));
		automateManager.updateRules(samplers, rules, handlers);
		automateManager.updateScheduler();
		sendClientMessage(clientSession, exchangeId, "Scheduler updated");
		return AckMessage.sack(exchangeId);
	}

	private String headCommitId(Git git) {
		Ref ref = git.getRepository().getAllRefs().get("HEAD");
		return ref.getObjectId().getName();
	}

	private void sendClientMessage(ClientSession clientSession, String exchangeId, String content)
			throws InterruptedException {
		if (clientSession == null) {
			return;
		}
		RepoUpdateMessage amMessage = new RepoUpdateMessage(SERVER, exchangeId);
		amMessage.setMessage(content);
		clientSession.getMessageQueue().put(amMessage);
	}

	private void sendClientMessage(ClientSession clientSession, String exchangeId, BaseMessage message)
			throws InterruptedException {
		if (clientSession == null) {
			return;
		}
		clientSession.getMessageQueue().put(message);
	}

	public boolean send(ClientSession clientSession, String clientExchangeId, String path, String target,
			List<ClientInfo> toWorkers) throws Exception {
		String exchangeId = UUID.randomUUID().toString();
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
			FileDataMessage fileDataMessage = new FileDataMessage(systemClient.session().getClientInfo(), targetName,
					start, end, data, md5Hex);
			fileDataMessage.setExchangeId(exchangeId);
			fileDataMessage.setSize(totalSize, currentSize);
			fileDataMessage.setToWorkers(toWorkers);
			BaseMessage responseMessage = systemClient.exchange(fileDataMessage, toWorkers);
			if (responseMessage instanceof RejectMessage) {
				sendClientMessage(clientSession, clientExchangeId, responseMessage);
				fis.close();
				return false;
			} else {
				System.out.println("Send package success, total size " + fileDataMessage.getTotalSize()
						+ " current received " + fileDataMessage.getCurrentSize());
			}
			if (end) {
				return true;
			}
			if (start) {
				start = false;
			}
		}
	}

}
