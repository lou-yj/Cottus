package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;
import java.util.Collections;
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

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.model.bean.automate.ExecutorTaskRecord;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorRecordsMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class ExecutorRecordsHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Autowired
	@Lazy
	private Terminal terminal;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ExecutorRecordsMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		ExecutorRecordsMessage itemsMessage = (ExecutorRecordsMessage) message;
		List<ExecutorTaskRecord> records = itemsMessage.getRecords();
		Collections.reverse(records);
		Object[][] data = new Object[records.size() + 1][];
		data[0] = new Object[] { "SCHEDULEID", "TIME", "PARAMS", "RUNTIME ENV", "METRICS", "STATUS", "MESSAGE" };
		int index = 1;
		for (ExecutorTaskRecord record : records) {
			data[index++] = new Object[] { record.getScheduleId(),
					new DateTime(record.getTime()).toString("yyyy-MM-dd HH:mm:ss"), formatMap(record.getParams()),
					formatMap(record.getSre()), StringUtils.join(record.getMetrics(), "\n"), record.getStatus(),
					record.getMessage() };
		}
		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		tableBuilder.addHeaderBorder(BorderStyle.fancy_double);
		tableBuilder.addFullBorder(BorderStyle.fancy_light);
		writer.println(tableBuilder.build().render(terminal.getWidth()));
		writer.println("Found " + records.size() + " Records");
		throw new EndOfMessageException();
	}

}
