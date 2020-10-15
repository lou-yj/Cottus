package com.louyj.cottus.client.handler;

import java.io.PrintStream;
import java.util.List;

import com.louyj.cottus.client.ClientSession;
import com.louyj.cottus.client.exception.EndOfMessageException;
import org.apache.commons.lang3.StringUtils;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.automate.Handler;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.ListHandlersMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class DescribeHandlerHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Autowired
	@Lazy
	private Terminal terminal;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ListHandlersMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		ListHandlersMessage itemsMessage = (ListHandlersMessage) message;
		List<Handler> handlers = itemsMessage.getHandlers();
		Object[][] data = new Object[handlers.size() + 1][];
		data[0] = new Object[] { "NAME", "LANGUAGE", "MATCHED", "EXECUTE TARGETS", "ACTION WAIT COUNT",
				"ACTION AGGR TIME", "ORDER", "PREVENT HANDLERS" };
		int index = 1;
		for (Handler handler : handlers) {
			data[index++] = new Object[] { handler.getName(), handler.getLanguage(), formatMap(handler.getMatched()),
					formatMap(handler.getTargets()), handler.getActionWaitCount(), handler.getActionAggrTime(),
					handler.getOrder(), StringUtils.join(handler.getPreventHandlers(), "\n") };
		}
		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		tableBuilder.addHeaderBorder(BorderStyle.fancy_double);
		tableBuilder.addFullBorder(BorderStyle.fancy_light);
		writer.println(tableBuilder.build().render(terminal.getWidth()));
		writer.println("Found " + handlers.size() + " handlers");
		throw new EndOfMessageException();
	}

}
