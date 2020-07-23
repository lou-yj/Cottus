package com.louyj.rhttptunnel.model.http;

import java.util.Map;

import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.message.consts.CommandGroupType;

/**
 *
 * Create at 2020年7月23日
 *
 * @author Louyj
 *
 */
public class ExchangeContext {

	private String clientId;

	private String command;

	private String className;

	private String methodName;

	private Object[] args;

	private CommandGroupType[] commandGroups = new CommandGroupType[0];

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public boolean isAllowAll() {
		for (CommandGroupType cgt : commandGroups) {
			if (cgt.equals(CommandGroupType.CORE_ALLOW_ALL)) {
				return true;
			}
		}
		return false;
	}

	public Map<String, String> httpHeaders() {
		Map<String, String> headers = Maps.newHashMap();
		headers.put("X-Command", command);
		return headers;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public CommandGroupType[] getCommandGroups() {
		return commandGroups;
	}

	public void setCommandGroups(CommandGroupType[] commandGroups) {
		this.commandGroups = commandGroups;
	}

}
