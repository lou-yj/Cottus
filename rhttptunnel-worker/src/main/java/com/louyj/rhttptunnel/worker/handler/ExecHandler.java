package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ExecMessage;
import com.louyj.rhttptunnel.worker.shell.ShellHolder;
import com.louyj.rhttptunnel.worker.shell.ShellManager;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ExecHandler implements IMessageHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${work.directory}")
	private String workDirectory;

	@Autowired
	private ShellManager shellManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ExecMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		ExecMessage execMessage = (ExecMessage) message;
		String dir = execMessage.getWorkdir();
		if (StringUtils.isBlank(dir)) {
			dir = workDirectory;
		}
		String cmdLine = "cd '" + dir + "' && bash '" + execMessage.getPath() + "' " + execMessage.getArgs();
		logger.info("exec {}", cmdLine);
		ShellHolder shellHolder = shellManager.activeShell(message.getClient().identify());
		shellHolder.ensureRunning();
		String output = shellHolder.exec(cmdLine);
		return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()).withMessage(output));
	}

}
