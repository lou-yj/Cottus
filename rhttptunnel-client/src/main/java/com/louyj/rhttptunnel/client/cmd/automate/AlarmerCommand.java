package com.louyj.rhttptunnel.client.cmd.automate;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmerRecordsListMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmerTraceListMessage;
import com.louyj.rhttptunnel.model.message.automate.ListAlarmInhibitorMessage;
import com.louyj.rhttptunnel.model.message.automate.ListAlarmMarkerMessage;
import com.louyj.rhttptunnel.model.message.automate.ListAlarmSilencerMessage;
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

	@ShellMethod(value = "list alarmers")
	@ShellMethodAvailability("serverContext")
	public String alarmers() {
		ListAlarmersMessage message = new ListAlarmersMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "show alarm history records")
	@ShellMethodAvailability("serverContext")
	public String alarmRecords(@ShellOption(value = { "-n", "--name" }, help = "alarmer name") String name) {
		AlarmerRecordsListMessage message = new AlarmerRecordsListMessage(CLIENT);
		message.setName(name);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "trace alarm event")
	@ShellMethodAvailability("serverContext")
	public String alarmTrace(@ShellOption(value = { "-i", "--id" }, help = "event id") String id) {
		AlarmerTraceListMessage message = new AlarmerTraceListMessage(CLIENT);
		message.setUuid(id);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "list alarm markers")
	@ShellMethodAvailability("serverContext")
	public String alarmMarkers() {
		ListAlarmMarkerMessage message = new ListAlarmMarkerMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "list alarm silencers")
	@ShellMethodAvailability("serverContext")
	public String alarmSilencers() {
		ListAlarmSilencerMessage message = new ListAlarmSilencerMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "list alarm inhibitors")
	@ShellMethodAvailability("serverContext")
	public String alarmInhibitors() {
		ListAlarmInhibitorMessage message = new ListAlarmInhibitorMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
