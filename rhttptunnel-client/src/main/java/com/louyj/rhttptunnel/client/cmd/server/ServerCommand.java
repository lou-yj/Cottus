package com.louyj.rhttptunnel.client.cmd.server;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_ALLOW_ALL;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_CLIENT;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.client.ClientDetector;
import com.louyj.rhttptunnel.client.annotation.CommandGroups;
import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.client.cmd.worker.ControlCommand;
import com.louyj.rhttptunnel.client.util.LogUtils;
import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ConnectMessage;
import com.louyj.rhttptunnel.model.message.ExitMessage;
import com.louyj.rhttptunnel.model.message.ListServersMessage;
import com.louyj.rhttptunnel.model.message.RegistryMessage;
import com.louyj.rhttptunnel.model.message.RsaExchangeMessage;
import com.louyj.rhttptunnel.model.message.SecurityMessage;
import com.louyj.rhttptunnel.model.util.RsaUtils;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class ServerCommand extends BaseCommand {

	@Autowired
	private ControlCommand workerManageCommand;

	@Autowired
	private MessageExchanger messageExchanger;

	@CommandGroups({ CORE_CLIENT, CORE_ALLOW_ALL })
	@ShellMethod(value = "Connect to servers")
	@ShellMethodAvailability("clientContext")
	public String connect(@ShellOption(value = { "-s",
			"--server" }, help = "bootstrap server address", defaultValue = "http://localhost:18080") String address,
			@ShellOption(value = { "-u", "--user" }, help = "user name") String userName,
			@ShellOption(value = { "-p", "--password" }, help = "password") String password)
			throws NoSuchAlgorithmException {
		messageExchanger.setBootstrapAddress(address);
		{
			LogUtils.printMessage("Exchange security information with servers", System.out);
			Pair<Key, Key> keyPair = RsaUtils.genKeyPair();
			Pair<String, String> stringKeyPair = RsaUtils.stringKeyPair(keyPair);
			RegistryMessage registryMessage = new RegistryMessage(CLIENT);
			registryMessage.setRegistryClient(CLIENT);
			BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, registryMessage);
			String resp = messagePoller.pollExchangeMessage(response);
			if (StringUtils.isNotBlank(resp)) {
				return null;
			}
			RsaExchangeMessage rsaExchangeMessage = new RsaExchangeMessage(CLIENT);
			rsaExchangeMessage.setPublicKey(stringKeyPair.getRight());
			response = messageExchanger.jsonPost(CLIENT_EXCHANGE, rsaExchangeMessage);
			resp = messagePoller.pollExchangeMessage(response);
			if (StringUtils.isNotBlank(resp)) {
				return null;
			}
			messageExchanger.setPrivateKey(keyPair.getLeft());
			SecurityMessage securityMessage = new SecurityMessage(ClientDetector.CLIENT);
			response = messageExchanger.jsonPost(CLIENT_EXCHANGE, securityMessage);
			resp = messagePoller.pollExchangeMessage(response);
			if (StringUtils.isNotBlank(resp)) {
				return null;
			}
			LogUtils.printMessage("Security connection established", System.out);
		}
		ConnectMessage connectMessage = new ConnectMessage(CLIENT);
		connectMessage.setUser(userName);
		connectMessage.setPassword(password);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, connectMessage);
		String resp = messagePoller.pollExchangeMessage(response);
		if (StringUtils.isBlank(resp)) {
			session.setServerConnected(true);
			resp = workerManageCommand.discover("");
		}
		return resp;
	}

	@CommandGroups({ CORE_CLIENT, CORE_ALLOW_ALL })
	@ShellMethod(value = "Disconnect from servers")
	@ShellMethodAvailability("serverContext")
	public String disconnect() {
		if (messageExchanger.isServerConnected()) {
			ExitMessage message = new ExitMessage(ClientDetector.CLIENT);
			BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
			messagePoller.pollExchangeMessage(response);
		}
		session.setServerConnected(false);
		session.setWorkerConnected(false);
		session.setCwd("/");
		session.setSelectedWorkers(null);
		session.setDiscoverWorkerText(null);
		session.setDiscoverWorkers(Lists.newArrayList());
		return null;
	}

	@CommandGroups({ CORE_CLIENT })
	@ShellMethod(value = "list servers")
	@ShellMethodAvailability("serverContext")
	public String servers() {
		ListServersMessage listServersMessage = new ListServersMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, listServersMessage);
		return messagePoller.pollExchangeMessage(response);
	}

}
