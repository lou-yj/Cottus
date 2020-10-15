package com.louyj.cottus.client.cmd.user;

import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_SUPER_ADMIN;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_USER_MGR;

import com.louyj.cottus.client.ClientDetector;
import com.louyj.cottus.client.annotation.CommandGroups;
import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.google.common.collect.Sets;
import com.louyj.cottus.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.model.bean.User;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.user.UserAddMessage;
import com.louyj.rhttptunnel.model.message.user.UserDelMessage;
import com.louyj.rhttptunnel.model.message.user.UserGrantMessage;
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

	@CommandGroups({ CORE_USER_MGR })
	@ShellMethod(value = "List users")
	@ShellMethodAvailability("serverContext")
	public String userList() {
		UserListMessage message = new UserListMessage(ClientDetector.CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_USER_MGR })
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
		UserAddMessage message = new UserAddMessage(ClientDetector.CLIENT);
		message.setUser(user);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_USER_MGR })
	@ShellMethod(value = "grant user permissions")
	@ShellMethodAvailability("serverContext")
	public String userGrant(@ShellOption(value = { "-n", "--name" }, help = "user name") String userName,
			@ShellOption(value = { "-g", "--group" }, help = "groups", defaultValue = "CORE_NORMAL") String groups) {
		if (!(StringUtils.isNoneBlank(userName))) {
			return "User name is invalidate";
		}
		User user = new User();
		user.setName(userName);
		user.setGroups(Sets.newHashSet(groups.split(",")));
		user.getGroups().remove(CORE_SUPER_ADMIN.name());
		UserGrantMessage message = new UserGrantMessage(ClientDetector.CLIENT);
		message.setUser(user);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_USER_MGR })
	@ShellMethod(value = "delete user")
	@ShellMethodAvailability("serverContext")
	public String userDel(@ShellOption(value = { "-n", "--name" }, help = "user name") String userName) {
		UserDelMessage message = new UserDelMessage(ClientDetector.CLIENT);
		message.setUserName(userName);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
