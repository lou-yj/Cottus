package com.louyj.cottus.client.cmd.automate;

import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_ALARMER;

import com.louyj.cottus.client.ClientDetector;
import com.louyj.cottus.client.annotation.CommandGroups;
import com.louyj.cottus.client.cmd.BaseCommand;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmInhibitorsMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmMarkersMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmSilencersMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmerRecordsMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmerTraceMessage;
import com.louyj.rhttptunnel.model.message.automate.ListAlarmersMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class AlarmerCommand extends BaseCommand {

	@CommandGroups({ CORE_ALARMER })
	@ShellMethod(value = "list alarmers")
	@ShellMethodAvailability("serverContext")
	public String alarmers() {
		ListAlarmersMessage message = new ListAlarmersMessage(ClientDetector.CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_ALARMER })
	@ShellMethod(value = "show alarm history records")
	@ShellMethodAvailability("serverContext")
	public String alarmRecords(@ShellOption(value = { "-n", "--name" }, help = "alarmer name") String name) {
		AlarmerRecordsMessage message = new AlarmerRecordsMessage(ClientDetector.CLIENT);
		message.setName(name);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_ALARMER })
	@ShellMethod(value = "trace alarm event")
	@ShellMethodAvailability("serverContext")
	public String alarmTrace(@ShellOption(value = { "-i", "--id" }, help = "event id") String id) {
		AlarmerTraceMessage message = new AlarmerTraceMessage(ClientDetector.CLIENT);
		message.setUuid(id);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_ALARMER })
	@ShellMethod(value = "list alarm markers")
	@ShellMethodAvailability("serverContext")
	public String alarmMarkers() {
		AlarmMarkersMessage message = new AlarmMarkersMessage(ClientDetector.CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_ALARMER })
	@ShellMethod(value = "list alarm silencers")
	@ShellMethodAvailability("serverContext")
	public String alarmSilencers() {
		AlarmSilencersMessage message = new AlarmSilencersMessage(ClientDetector.CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_ALARMER })
	@ShellMethod(value = "list alarm inhibitors")
	@ShellMethodAvailability("serverContext")
	public String alarmInhibitors() {
		AlarmInhibitorsMessage message = new AlarmInhibitorsMessage(ClientDetector.CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
