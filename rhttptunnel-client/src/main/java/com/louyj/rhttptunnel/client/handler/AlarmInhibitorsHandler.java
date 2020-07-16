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
import com.louyj.rhttptunnel.model.bean.automate.AlarmInhibitor;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmInhibitorsMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class AlarmInhibitorsHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Autowired
	@Lazy
	private Terminal terminal;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return AlarmInhibitorsMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		AlarmInhibitorsMessage itemsMessage = (AlarmInhibitorsMessage) message;
		List<AlarmInhibitor> inhibitors = itemsMessage.getInhibitors();
		Object[][] data = new Object[inhibitors.size() + 1][];
		data[0] = new Object[] { "NAME", "MATCHED", "WINDOW MATCHED", "REGEX MATCH", "TIME WINDOW SIZE" };
		int index = 1;
		for (AlarmInhibitor inhibitor : inhibitors) {
			data[index++] = new Object[] { inhibitor.getName(), formatMap(inhibitor.getMatched()),
					formatMap(inhibitor.getWindowMatched()), inhibitor.isRegexMatch(), inhibitor.getTimeWindowSize() };
		}
		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		tableBuilder.addHeaderBorder(BorderStyle.fancy_double);
		tableBuilder.addFullBorder(BorderStyle.fancy_light);
		writer.println(tableBuilder.build().render(terminal.getWidth()));
		writer.println("Found " + inhibitors.size() + " inhibitors");
		throw new EndOfMessageException();
	}

}
