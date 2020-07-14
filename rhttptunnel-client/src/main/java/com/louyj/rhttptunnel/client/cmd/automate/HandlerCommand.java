package com.louyj.rhttptunnel.client.cmd.automate;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.DescribeHandlerMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorLogShowMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorRecordsListMessage;
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

	@ShellMethod(value = "show handler list")
	@ShellMethodAvailability("serverContext")
	public String handlerList() {
		ListHandlersMessage message = new ListHandlersMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "show handler details")
	@ShellMethodAvailability("serverContext")
	public String handlerDescribe(@ShellOption(value = { "-n", "--name" }, help = "handler name") String name) {
		DescribeHandlerMessage message = new DescribeHandlerMessage(CLIENT);
		message.setName(name);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "show handler task execute history record")
	@ShellMethodAvailability("serverContext")
	public String handlerRecords(@ShellOption(value = { "-n", "--name" }, help = "handler name") String name) {
		ExecutorRecordsListMessage message = new ExecutorRecordsListMessage(CLIENT);
		message.setTaskType(TaskType.HANDLER);
		message.setTask(name);
		message.setExecutor("handler");
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "show handler task execute log")
	@ShellMethodAvailability("serverContext")
	public String handlerLogs(@ShellOption(value = { "-n", "--name" }, help = "handler name") String name,
			@ShellOption(value = { "-i", "--id" }, help = "schedule id") String id) {
		ExecutorLogShowMessage message = new ExecutorLogShowMessage(CLIENT);
		message.setTaskType(TaskType.HANDLER);
		message.setExecutor("handler");
		message.setTask(name);
		message.setScheduleId(id);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
