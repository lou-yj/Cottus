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

import com.louyj.rhttptunnel.model.bean.ServerInfo;
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
		List<ServerInfo> servers = listMessage.getServers();
		Object[][] data = new Object[servers.size() + 1][];
		data[0] = new Object[] { "SERVER ID", "HOST", "IP", "ROLE", "UPTIME", "URL" };
		int index = 1;
		for (ServerInfo server : servers) {
			ClientInfo clientInfo = server.getClientInfo();
			String role = StringUtils.equals(listMessage.getMaster(), clientInfo.identify()) ? "MASTER" : "SLAVE";
			data[index++] = new Object[] { clientInfo.identify(), clientInfo.getHost(), clientInfo.getIp(), role,
					formatTime(clientInfo.getUptime()), server.getUrl() };
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
