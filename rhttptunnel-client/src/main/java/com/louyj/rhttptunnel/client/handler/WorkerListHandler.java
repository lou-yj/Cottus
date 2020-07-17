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
import com.louyj.rhttptunnel.model.bean.WorkerInfo;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.WorkerListMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class WorkerListHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Autowired
	@Lazy
	private Terminal terminal;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return WorkerListMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		WorkerListMessage listMessage = (WorkerListMessage) message;
		List<WorkerInfo> workers = listMessage.getWorkers();
		Object[][] data = new Object[workers.size() + 1][];
		data[0] = new Object[] { "INDEX", "WORKER ID", "HOST", "IP", "LABELS", "UPTIME" };
		int index = 1;
		for (WorkerInfo worker : workers) {
			ClientInfo clientInfo = worker.getClientInfo();
			data[index++] = new Object[] { index - 1, clientInfo.identify(), clientInfo.getHost(), clientInfo.getIp(),
					formatMap(worker.getLabels()), formatTime(clientInfo.getUptime()) };
		}
		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		tableBuilder.addHeaderBorder(BorderStyle.fancy_double);
		tableBuilder.addFullBorder(BorderStyle.fancy_light);
		writer.println(tableBuilder.build().render(terminal.getWidth()));
		writer.println("Found " + (index - 1) + " workes");
		session.setDiscoverWorkers(listMessage.getWorkers());
		throw new EndOfMessageException();
	}

}
