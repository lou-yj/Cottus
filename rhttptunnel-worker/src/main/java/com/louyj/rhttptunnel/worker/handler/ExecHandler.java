package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ExecMessage;

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

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ExecMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		ExecMessage execMessage = (ExecMessage) message;
		String argLine = "";
		for (String arg : execMessage.getArgs()) {
			argLine = "'" + arg + "'";
		}
		String cmdLine = "sh \"" + execMessage.getPath() + "\" " + argLine;
		logger.info("exec {}", cmdLine);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		CommandLine commandline = CommandLine.parse(cmdLine);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(execMessage.getTimeout());

		DefaultExecutor executor = new DefaultExecutor();
		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
		executor.setStreamHandler(streamHandler);
		executor.setWatchdog(watchdog);
		executor.execute(commandline);
		return Lists
				.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()).withMessage(outputStream.toString()));
	}

}
