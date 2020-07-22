package com.louyj.rhttptunnel.server.auth;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.IgniteCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.bean.Group;
import com.louyj.rhttptunnel.model.bean.Permission;
import com.louyj.rhttptunnel.model.bean.User;
import com.louyj.rhttptunnel.server.IgniteRegistry;

/**
 *
 * Create at 2020年7月21日
 *
 * @author Louyj
 *
 */
@Component
public class UserPermissionManager {

	@Autowired
	private IgniteRegistry igniteRegistry;

	private IgniteCache<Object, Object> userPermissionCache;

	@PostConstruct
	public void init() {
		userPermissionCache = igniteRegistry.getOrCreateCache("userPermissionCache");
	}

	public Permission verify(String user, String password) {
		User userInfo = (User) userPermissionCache.get("user:" + user);
		if (userInfo == null) {
			return null;
		}
		if (StringUtils.equals(password, userInfo.getPassword())) {
			return permission(userInfo);
		}
		return null;
	}

	public Permission permission(User user) {
		Permission permission = new Permission();
		Set<String> groups = user.getGroups();
		for (String groupId : groups) {
			Group group = (Group) userPermissionCache.get("group:" + groupId);
			permission(permission, group);
		}
		return permission;
	}

	public void permission(Permission permision, Group groupInput) {
		if (groupInput == null) {
			return;
		}
		Set<String> commands = groupInput.getCommands();
		if (commands != null) {
			permision.getCommands().addAll(commands);
		}
		Set<String> groupIds = groupInput.getGroups();
		if (groupIds != null) {
			for (String groupId : groupIds) {
				Group group = (Group) userPermissionCache.get("group:" + groupId);
				permission(permision, group);
			}
		}
	}

	public void upsertGroup(Group group) {
		userPermissionCache.put("group:" + group.getName(), group);
	}

	public void removeGroup(String name) {
		userPermissionCache.remove("group:" + name);
	}

	public void removeUser(String name) {
		userPermissionCache.remove("user:" + name);
	}

	public void upsertUser(User user) {
		userPermissionCache.put("user:" + user.getName(), user);
	}

	public List<Group> groups() {
		List<Group> groups = Lists.newArrayList();
		userPermissionCache.forEach(e -> {
			if (e.getValue() instanceof Group) {
				groups.add((Group) e.getValue());
			}
		});
		return groups;
	}

	public List<User> users() {
		List<User> groups = Lists.newArrayList();
		userPermissionCache.forEach(e -> {
			if (e.getValue() instanceof User) {
				groups.add((User) e.getValue());
			}
		});
		return groups;
	}

}
