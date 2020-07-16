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
import com.louyj.rhttptunnel.model.bean.automate.AlarmMarker;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmMarkersMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class AlarmMarkersHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Autowired
	@Lazy
	private Terminal terminal;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return AlarmMarkersMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		AlarmMarkersMessage itemsMessage = (AlarmMarkersMessage) message;
		List<AlarmMarker> markers = itemsMessage.getMarkers();
		Object[][] data = new Object[markers.size() + 1][];
		data[0] = new Object[] { "NAME", "ORDER", "MATCHED", "REGEX MATCH", "ADD TAGS", "WITH PROPERTIES" };
		int index = 1;
		for (AlarmMarker marker : markers) {
			data[index++] = new Object[] { marker.getName(), marker.getOrder(), formatMap(marker.getMatched()),
					marker.isRegexMatch(), formatMap(marker.getTags()), formatMap(marker.getProperties()) };
		}
		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		tableBuilder.addHeaderBorder(BorderStyle.fancy_double);
		tableBuilder.addFullBorder(BorderStyle.fancy_light);
		writer.println(tableBuilder.build().render(terminal.getWidth()));
		writer.println("Found " + markers.size() + " markers");
		throw new EndOfMessageException();
	}

}
