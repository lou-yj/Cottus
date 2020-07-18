package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.ServersMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class ServersHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Autowired
	@Lazy
	private Terminal terminal;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ServersMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		ServersMessage listMessage = (ServersMessage) message;
		List<ClientInfo> servers = listMessage.getServers();
		Object[][] data = new Object[servers.size() + 1][];
		data[0] = new Object[] { "SERVER ID", "HOST", "IP", "ROLE", "UPTIME" };
		int index = 1;
		for (ClientInfo server : servers) {
			String role = StringUtils.equals(listMessage.getMaster(), server.identify()) ? "MASTER" : "SLAVE";
			data[index++] = new Object[] { server.identify(), server.getHost(), server.getIp(), role,
					formatTime(server.getUptime()) };
		}
		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		tableBuilder.addHeaderBorder(BorderStyle.fancy_double);
		tableBuilder.addFullBorder(BorderStyle.fancy_light);
		writer.println(tableBuilder.build().render(terminal.getWidth()));
		writer.println("Found " + (index - 1) + " servers");
		throw new EndOfMessageException();
	}

}
