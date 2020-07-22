package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;
import java.util.Collections;
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
import com.louyj.rhttptunnel.model.bean.User;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.user.UserListMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class UsersHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Autowired
	@Lazy
	private Terminal terminal;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return UserListMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		UserListMessage usersMessage = (UserListMessage) message;
		List<User> users = usersMessage.getUsers();
		Collections.reverse(users);
		Object[][] data = new Object[users.size() + 1][];
		data[0] = new Object[] { "NAME", "GROUPS" };
		int index = 1;
		for (User user : users) {
			data[index++] = new Object[] { user.getName(), formatCollection(user.getGroups()) };
		}
		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		tableBuilder.addHeaderBorder(BorderStyle.fancy_double);
		tableBuilder.addFullBorder(BorderStyle.fancy_light);
		writer.println(tableBuilder.build().render(terminal.getWidth()));
		writer.println("Found " + users.size() + " Users");
		throw new EndOfMessageException();
	}

}
