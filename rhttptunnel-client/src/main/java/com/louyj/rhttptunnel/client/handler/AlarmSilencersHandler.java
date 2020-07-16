package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;
import java.util.List;

import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.model.bean.automate.AlarmSilencer;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmSilencersMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class AlarmSilencersHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Autowired
	@Lazy
	private Terminal terminal;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return AlarmSilencersMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		AlarmSilencersMessage itemsMessage = (AlarmSilencersMessage) message;
		List<AlarmSilencer> silencers = itemsMessage.getSilencers();
		Object[][] data = new Object[silencers.size() + 1][];
		data[0] = new Object[] { "MATCHED", "REGEX MATCH", "START TIME", "END TIME" };
		int index = 1;
		for (AlarmSilencer silencer : silencers) {
			data[index++] = new Object[] { formatMap(silencer.getMatched()), silencer.isRegexMatch(),
					formatTime(silencer.getStartTime()), formatTime(silencer.getEndTime()) };
		}
		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		tableBuilder.addHeaderBorder(BorderStyle.fancy_double);
		tableBuilder.addFullBorder(BorderStyle.fancy_light);
		writer.println(tableBuilder.build().render(terminal.getWidth()));
		writer.println("Found " + silencers.size() + " silencers");
		throw new EndOfMessageException();
	}

}
