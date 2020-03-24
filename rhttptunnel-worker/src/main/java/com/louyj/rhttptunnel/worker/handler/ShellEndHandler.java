package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ShellEndMessage;
import com.louyj.rhttptunnel.model.message.ShellMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ShellEndHandler implements IMessageHandler, IClientCloseable {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${work.directory}")
	private String workDirectory;

	@Autowired
	private ShellHandler shellHandler;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ShellEndMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		ShellMessage shellMessage = new ShellMessage(message.getClient(), message.getExchangeId());
		shellMessage.setMessage("exit");
		try {
			shellHandler.handle(shellMessage);
		} catch (Exception e) {
			logger.error("", e);
		}
		File infile = new File(workDirectory, "temp/" + message.getClient().identify() + ".in");
		File outfile = new File(workDirectory, "temp/" + message.getClient().identify() + ".out");
		File resfile = new File(workDirectory, "temp/" + message.getClient().identify() + ".res");
		File scriptfile = new File(workDirectory, "temp/" + message.getClient().identify() + ".sc");
		infile.delete();
		outfile.delete();
		resfile.delete();
		scriptfile.delete();
		return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()));
	}

	@Override
	public void close(String clientId) throws IOException {
		File infile = new File(workDirectory, "temp/" + clientId + ".in");
		if (infile.exists()) {
			FileUtils.writeStringToFile(infile, "exit", true);
		}
	}

}
