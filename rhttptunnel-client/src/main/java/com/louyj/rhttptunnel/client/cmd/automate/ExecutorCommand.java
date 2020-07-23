package com.louyj.rhttptunnel.client.cmd.automate;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_EXECUTOR;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.client.annotation.CommandGroups;
import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.DescribeExecutorMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorLogMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorRecordsMessage;
import com.louyj.rhttptunnel.model.message.automate.ListExecutorsMessage;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.TaskType;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class ExecutorCommand extends BaseCommand {

	@CommandGroups({ CORE_EXECUTOR })
	@ShellMethod(value = "list executors")
	@ShellMethodAvailability("serverContext")
	public String executors() {
		ListExecutorsMessage message = new ListExecutorsMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_EXECUTOR })
	@ShellMethod(value = "show executor details")
	@ShellMethodAvailability("serverContext")
	public String executorDescribe(@ShellOption(value = { "-n", "--name" }, help = "executor name") String name) {
		DescribeExecutorMessage message = new DescribeExecutorMessage(CLIENT);
		message.setName(name);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_EXECUTOR })
	@ShellMethod(value = "show executor task execute history record")
	@ShellMethodAvailability("serverContext")
	public String executorRecords(@ShellOption(value = { "-n", "--name" }, help = "executor name") String name,
			@ShellOption(value = { "-t", "--task" }, help = "task") String task) {
		ExecutorRecordsMessage message = new ExecutorRecordsMessage(CLIENT);
		message.setTaskType(TaskType.EXECUTOR);
		message.setExecutor(name);
		message.setTask(task);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_EXECUTOR })
	@ShellMethod(value = "show executor task execute log")
	@ShellMethodAvailability("serverContext")
	public String executorLogs(@ShellOption(value = { "-n", "--name" }, help = "executor name") String name,
			@ShellOption(value = { "-t", "--task" }, help = "task") String task,
			@ShellOption(value = { "-i", "--id" }, help = "schedule id") String id) {
		ExecutorLogMessage message = new ExecutorLogMessage(CLIENT);
		message.setTaskType(TaskType.EXECUTOR);
		message.setExecutor(name);
		message.setTask(task);
		message.setScheduleId(id);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
