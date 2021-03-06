package com.louyj.cottus.client.cmd.user;

import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
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
import com.louyj.rhttptunnel.model.bean.Group;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.user.GroupDelMessage;
import com.louyj.rhttptunnel.model.message.user.GroupListMessage;
import com.louyj.rhttptunnel.model.message.user.GroupUpsertMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class GroupCommand extends BaseCommand {

	@CommandGroups({ CORE_USER_MGR })
	@ShellMethod(value = "List groups")
	@ShellMethodAvailability("serverContext")
	public String groupList() {
		GroupListMessage message = new GroupListMessage(ClientDetector.CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_USER_MGR })
	@ShellMethod(value = "add or update group")
	@ShellMethodAvailability("serverContext")
	public String groupUpsert(@ShellOption(value = { "-n", "--name" }, help = "group name") String groupName,
			@ShellOption(value = { "-g", "--group" }, help = "sub groups", defaultValue = "CORE_NORMAL") String groups,
			@ShellOption(value = { "-c", "--commands" }, help = "sub commands") String commands) {
		if (!(StringUtils.isNoneBlank(groupName))) {
			return "Group name is invalidate";
		}
		Group group = new Group();
		group.setName(groupName);
		group.setGroups(Sets.newHashSet(groups.split(",")));
		group.setCommands(Sets.newHashSet(commands.split(",")));
		GroupUpsertMessage message = new GroupUpsertMessage(ClientDetector.CLIENT);
		message.setGroup(group);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_USER_MGR })
	@ShellMethod(value = "delete group")
	@ShellMethodAvailability("serverContext")
	public String groupDel(@ShellOption(value = { "-n", "--name" }, help = "group name") String groupName) {
		GroupDelMessage message = new GroupDelMessage(ClientDetector.CLIENT);
		message.setGroupName(groupName);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
