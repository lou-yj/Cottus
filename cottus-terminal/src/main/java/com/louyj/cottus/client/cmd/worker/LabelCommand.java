package com.louyj.cottus.client.cmd.worker;

import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_WORKER_MGR;

import com.louyj.cottus.client.ClientDetector;
import com.louyj.cottus.client.annotation.CommandGroups;
import com.louyj.cottus.client.cmd.BaseCommand;
import com.louyj.cottus.client.consts.Status;
import com.louyj.cottus.client.util.LogUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.label.UpdateLabelMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
@ShellCommandGroup("Worker Manage Commands")
public class LabelCommand extends BaseCommand {

	@CommandGroups({ CORE_WORKER_MGR })
	@ShellMethod(value = "update worker labels")
	@ShellMethodAvailability("workerContext")
	public String labelSet(@ShellOption(value = { "-l", "--labels" }, help = "labels to be update") String labelStr) {
		String[] labelKvs = labelStr.split("\\s*,\\s*");
		UpdateLabelMessage message = new UpdateLabelMessage(ClientDetector.CLIENT);
		for (String labelKv : labelKvs) {
			String[] label = labelKv.split("\\s*=\\s*");
			if (label.length == 2) {
				message.getSetLabels().put(label[0], label[1]);
			} else if (label.length == 1) {
				message.getDelLabels().add(label[0]);
			} else {
				LogUtils.printMessage("bad format " + labelKv, System.out);
				return Status.FAILED;
			}
		}
		if (CollectionUtils.isEmpty(message.getDelLabels()) && MapUtils.isEmpty(message.getSetLabels())) {
			LogUtils.printMessage("No labels seted", System.out);
			return Status.FAILED;
		}
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
