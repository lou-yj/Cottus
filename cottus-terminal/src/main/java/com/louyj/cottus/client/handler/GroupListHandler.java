package com.louyj.cottus.client.handler;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import com.louyj.cottus.client.ClientSession;
import com.louyj.cottus.client.exception.EndOfMessageException;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.Group;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.user.GroupListMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class GroupListHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Autowired
	@Lazy
	private Terminal terminal;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return GroupListMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		GroupListMessage usersMessage = (GroupListMessage) message;
		List<Group> groups = usersMessage.getGroups();
		Collections.reverse(groups);
		Object[][] data = new Object[groups.size() + 1][];
		data[0] = new Object[] { "NAME", "SUB GROUPS", "SUB COMMANDS" };
		int index = 1;
		for (Group group : groups) {
			data[index++] = new Object[] { group.getName(), formatCollection(group.getGroups()),
					formatCollection(group.getCommands()) };
		}
		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		tableBuilder.addHeaderBorder(BorderStyle.fancy_double);
		tableBuilder.addFullBorder(BorderStyle.fancy_light);
		writer.println(tableBuilder.build().render(terminal.getWidth()));
		writer.println("Found " + groups.size() + " Groups");
		throw new EndOfMessageException();
	}

}
