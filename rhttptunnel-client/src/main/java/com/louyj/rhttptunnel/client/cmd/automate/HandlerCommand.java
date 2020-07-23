package com.louyj.rhttptunnel.client.cmd.automate;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_HANDLER;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.client.annotation.CommandGroups;
import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.DescribeHandlerMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorLogMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorRecordsMessage;
import com.louyj.rhttptunnel.model.message.automate.ListHandlersMessage;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.TaskType;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class HandlerCommand extends BaseCommand {

	@CommandGroups({ CORE_HANDLER })
	@ShellMethod(value = "list handlers")
	@ShellMethodAvailability("serverContext")
	public String handlers() {
		ListHandlersMessage message = new ListHandlersMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_HANDLER })
	@ShellMethod(value = "show handler details")
	@ShellMethodAvailability("serverContext")
	public String handlerDescribe(@ShellOption(value = { "-n", "--name" }, help = "handler name") String name) {
		DescribeHandlerMessage message = new DescribeHandlerMessage(CLIENT);
		message.setName(name);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_HANDLER })
	@ShellMethod(value = "show handler task execute history record")
	@ShellMethodAvailability("serverContext")
	public String handlerRecords(@ShellOption(value = { "-n", "--name" }, help = "handler name") String name) {
		ExecutorRecordsMessage message = new ExecutorRecordsMessage(CLIENT);
		message.setTaskType(TaskType.HANDLER);
		message.setTask(name);
		message.setExecutor("handler");
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_HANDLER })
	@ShellMethod(value = "show handler task execute log")
	@ShellMethodAvailability("serverContext")
	public String handlerLogs(@ShellOption(value = { "-n", "--name" }, help = "handler name") String name,
			@ShellOption(value = { "-i", "--id" }, help = "schedule id") String id) {
		ExecutorLogMessage message = new ExecutorLogMessage(CLIENT);
		message.setTaskType(TaskType.HANDLER);
		message.setExecutor("handler");
		message.setTask(name);
		message.setScheduleId(id);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
