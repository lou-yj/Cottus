package com.louyj.rhttptunnel.client.cmd.user;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_SUPER_ADMIN;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.louyj.rhttptunnel.client.annotation.CommandGroups;
import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.consts.CommandGroupType;
import com.louyj.rhttptunnel.model.message.user.InitPermissionMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class PermissionCommand extends BaseCommand implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@CommandGroups({ CORE_SUPER_ADMIN })
	@ShellMethod(value = "init server permission")
	@ShellMethodAvailability("serverContext")
	public String initPermission() {
		Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ShellComponent.class);
		Map<String, Set<String>> commandGroups = Maps.newHashMap();
		for (Object bean : beans.values()) {
			Method[] methods = bean.getClass().getMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(ShellMethod.class)) {
					ShellMethod shellMethod = method.getAnnotation(ShellMethod.class);
					String[] keys = shellMethod.key();
					if (keys == null) {
						keys = new String[] { cmdName(method.getName()) };
					}
					Set<String> groups = Sets.newHashSet();
					CommandGroups cgroups = method.getAnnotation(CommandGroups.class);
					if (cgroups == null) {
						groups.add("CORE_UNDEFINED");
					} else {
						CommandGroupType[] groupTypes = cgroups.value();
						for (CommandGroupType type : groupTypes) {
							groups.add(type.name());
						}
					}
					for (String group : groups) {
						Set<String> keySet = commandGroups.get(group);
						if (keySet == null) {
							keySet = Sets.newHashSet();
							commandGroups.put(group, keySet);
						}
						for (String key : keys) {
							keySet.add(key);
						}
					}
				}
			}
		}
		InitPermissionMessage message = new InitPermissionMessage(CLIENT);
		message.setCommandGroups(commandGroups);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	private String cmdName(String methodName) {
		return methodName;
	}

}
