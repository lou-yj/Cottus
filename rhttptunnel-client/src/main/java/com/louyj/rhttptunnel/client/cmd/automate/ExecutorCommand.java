package com.louyj.rhttptunnel.client.cmd.automate;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.DescribeExecutorMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorLogShowMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorRecordsListMessage;
import com.louyj.rhttptunnel.model.message.automate.ListExecutorsMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class ExecutorCommand extends BaseCommand {

	@ShellMethod(value = "show executor list")
	@ShellMethodAvailability("serverContext")
	public String executorList() {
		ListExecutorsMessage message = new ListExecutorsMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "show executor details")
	@ShellMethodAvailability("serverContext")
	public String executorDescribe(@ShellOption(value = { "-n", "--name" }, help = "executor name") String name) {
		DescribeExecutorMessage message = new DescribeExecutorMessage(CLIENT);
		message.setName(name);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "show executor task execute history record")
	@ShellMethodAvailability("serverContext")
	public String executorRecords(@ShellOption(value = { "-n", "--name" }, help = "executor name") String name,
			@ShellOption(value = { "-t", "--task" }, help = "task") String task) {
		ExecutorRecordsListMessage message = new ExecutorRecordsListMessage(CLIENT);
		message.setExecutor(name);
		message.setTask(task);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "show executor task execute log")
	@ShellMethodAvailability("serverContext")
	public String executorLogs(@ShellOption(value = { "-n", "--name" }, help = "executor name") String name,
			@ShellOption(value = { "-t", "--task" }, help = "task") String task,
			@ShellOption(value = { "-i", "--id" }, help = "schedule id") String id) {
		ExecutorLogShowMessage message = new ExecutorLogShowMessage(CLIENT);
		message.setExecutor(name);
		message.setTask(task);
		message.setScheduleId(id);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
