package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jline.terminal.Terminal;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.model.bean.automate.Executor;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.ListExecutorsMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class ListExecutorsHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Autowired
	@Lazy
	private Terminal terminal;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ListExecutorsMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		ListExecutorsMessage itemsMessage = (ListExecutorsMessage) message;
		List<Executor> executors = itemsMessage.getExecutors();
		Object[][] data = new Object[executors.size() + 1][];
		data[0] = new Object[] { "NAME", "SCHEDULE", "EXECUTE MODE", "TASKS", "UPDATE TIME" };
		int index = 1;
		for (Executor executor : executors) {
			List<String> taskNames = Lists.newArrayList();
			executor.getTasks().forEach(e -> taskNames.add(e.getName()));
			data[index++] = new Object[] { executor.getName(), executor.getScheduleExpression(),
					executor.getTaskExecuteMode(), StringUtils.join(taskNames, "\n"),
					new DateTime(executor.getUpdateTime()).toString("yyyy-MM-dd HH:mm:ss") };
		}
		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		tableBuilder.addHeaderBorder(BorderStyle.fancy_double);
		tableBuilder.addFullBorder(BorderStyle.fancy_light);
		writer.println(tableBuilder.build().render(terminal.getWidth()));
		writer.println("Found " + itemsMessage.getExecutors().size() + " executors");
		throw new EndOfMessageException();
	}

}
