package com.louyj.cottus.client.handler;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import com.louyj.cottus.client.ClientSession;
import com.louyj.cottus.client.exception.EndOfMessageException;
import org.jline.terminal.Terminal;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.automate.AlarmTriggeredRecord;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmerRecordsMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class AlarmerRecordsHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Autowired
	@Lazy
	private Terminal terminal;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return AlarmerRecordsMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		AlarmerRecordsMessage itemsMessage = (AlarmerRecordsMessage) message;
		List<AlarmTriggeredRecord> records = itemsMessage.getRecords();
		Collections.reverse(records);
		Object[][] data = new Object[records.size() + 1][];
		data[0] = new Object[] { "ID", "ALARM TIME", "ALARM GROUP", "FIELDS" };
		int index = 1;
		for (AlarmTriggeredRecord record : records) {
			data[index++] = new Object[] { record.getUuid(),
					new DateTime(record.getAlarmTime()).toString("yyyy-MM-dd HH:mm:ss"), record.getAlarmGroup(),
					formatMap(record.getFields()) };
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
