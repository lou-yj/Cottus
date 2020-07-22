package com.louyj.rhttptunnel.client.cmd.user;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_ADMIN;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_USER_MGR;

import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.google.common.collect.Sets;
import com.louyj.rhttptunnel.client.annotation.CommandGroups;
import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.model.bean.User;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.user.UserAddMessage;
import com.louyj.rhttptunnel.model.message.user.UserDelMessage;
import com.louyj.rhttptunnel.model.message.user.UserListMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class UserCommand extends BaseCommand {

	@CommandGroups({ CORE_USER_MGR, CORE_ADMIN })
	@ShellMethod(value = "List users")
	@ShellMethodAvailability("serverContext")
	public String userList() {
		UserListMessage message = new UserListMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_USER_MGR, CORE_ADMIN })
	@ShellMethod(value = "add user")
	@ShellMethodAvailability("serverContext")
	public String userAdd(@ShellOption(value = { "-n", "--name" }, help = "user name") String userName,
			@ShellOption(value = { "-p", "--password" }, help = "password") String password,
			@ShellOption(value = { "-g", "--group" }, help = "groups", defaultValue = "CORE_NORMAL") String groups) {
		if (!(StringUtils.isNoneBlank(userName) && StringUtils.isNotBlank(password))) {
			return "User name or password is invalidate";
		}
		User user = new User();
		user.setName(userName);
		user.setPassword(password);
		user.setGroups(Sets.newHashSet(groups.split(",")));
		UserAddMessage message = new UserAddMessage(CLIENT);
		message.setUser(user);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_USER_MGR, CORE_ADMIN })
	@ShellMethod(value = "delete user")
	@ShellMethodAvailability("serverContext")
	public String userDel(@ShellOption(value = { "-n", "--name" }, help = "user name") String userName) {
		UserDelMessage message = new UserDelMessage(CLIENT);
		message.setUserName(userName);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
